package com.pagemind.auth;

import com.pagemind.auth.dto.*;
import com.pagemind.config.JwtTokenProvider;
import com.pagemind.user.AuthProvider;
import com.pagemind.user.User;
import com.pagemind.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping({"/api/auth", "/v1/auth"})
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordResetCodeRepository resetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private static final SecureRandom random = new SecureRandom();

    @PostMapping({"/signup", "/register"})
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "Email address is already in use."));
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .authProvider(AuthProvider.local)
                .preferredLanguage(request.getPreferredLanguage() != null ? request.getPreferredLanguage() : "en")
                .build();

        User savedUser = userRepository.save(user);
        
        // Send welcome notification email to first-time user
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());
        } catch (Exception e) {
            // Non-blocking catch to ensure registration always succeeds
        }

        String token = tokenProvider.generateToken(savedUser.getEmail());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .dob(savedUser.getDob())
                .phoneNumber(savedUser.getPhoneNumber())
                .gender(savedUser.getGender())
                .preferredLanguage(savedUser.getPreferredLanguage())
                .profilePicUrl(savedUser.getProfilePicUrl())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid email or password."));
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        String token = tokenProvider.generateToken(user.getEmail());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .dob(user.getDob())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .preferredLanguage(user.getPreferredLanguage())
                .profilePicUrl(user.getProfilePicUrl())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "No user found with the provided email address."));
        }

        User user = userOpt.get();

        // Delete previous reset codes for this user
        resetCodeRepository.deleteByUserId(user.getId());

        // Generate 6-digit random numeric code
        String code = String.format("%06d", random.nextInt(1000000));

        PasswordResetCode resetCode = PasswordResetCode.builder()
                .user(user)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        resetCodeRepository.save(resetCode);
        boolean emailSent = emailService.sendPasswordResetEmail(user.getEmail(), code);
        String devFallbackCode = emailSent ? null : code;

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Password reset code sent to your email.")
                .devFallbackCode(devFallbackCode)
                .build());
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid email or code."));
        }

        User user = userOpt.get();
        Optional<PasswordResetCode> resetCodeOpt = resetCodeRepository
                .findTopByUserIdAndCodeOrderByCreatedAtDesc(user.getId(), request.getCode());

        if (resetCodeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid code."));
        }

        PasswordResetCode resetCode = resetCodeOpt.get();
        if (resetCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Reset code has expired."));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Code verified successfully."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid email or code."));
        }

        User user = userOpt.get();
        Optional<PasswordResetCode> resetCodeOpt = resetCodeRepository
                .findTopByUserIdAndCodeOrderByCreatedAtDesc(user.getId(), request.getCode());

        if (resetCodeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid code."));
        }

        PasswordResetCode resetCode = resetCodeOpt.get();
        if (resetCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Reset code has expired."));
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetCodeRepository.deleteByUserId(user.getId());

        return ResponseEntity.ok(new ApiResponse(true, "Password has been reset successfully."));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        String email = request.getEmail();
        String name = request.getName() != null ? request.getName() : email;
        boolean isNewUser = !userRepository.existsByEmail(email);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .name(name)
                    .email(email)
                    .passwordHash(null)
                    .authProvider(AuthProvider.google)
                    .preferredLanguage("en")
                    .build();
            return userRepository.save(newUser);
        });

        if (isNewUser) {
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getName());
            } catch (Exception e) {
                // Non-blocking catch
            }
        }

        String token = tokenProvider.generateToken(user.getEmail());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .dob(user.getDob())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .preferredLanguage(user.getPreferredLanguage())
                .profilePicUrl(user.getProfilePicUrl())
                .build();

        return ResponseEntity.ok(response);
    }
}
