package com.pagemind.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pagemind.auth.dto.AuthResponse;
import com.pagemind.user.AuthProvider;
import com.pagemind.user.User;
import com.pagemind.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.pagemind.auth.EmailService;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("[OAuth2SuccessHandler] Step 1: Processing successful Google OAuth2 authentication callback...");
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        log.info("[OAuth2SuccessHandler] Step 2: Extracted OAuth2User attributes — Email: '{}', Name: '{}'", email, name);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            log.info("[OAuth2SuccessHandler] Step 3a: No existing user found for email '{}'. Registering new Google user...", email);
            User newUser = User.builder()
                    .name(name != null ? name : (email != null ? email.split("@")[0] : "Google User"))
                    .email(email)
                    .passwordHash(null)
                    .authProvider(AuthProvider.google)
                    .preferredLanguage("en")
                    .build();
            User saved = userRepository.save(newUser);
            log.info("[OAuth2SuccessHandler] Step 3b: Successfully saved new Google user with ID: {}", saved.getId());
            
            // Send welcome notification email to first-time OAuth2 user
            try {
                emailService.sendWelcomeEmail(saved.getEmail(), saved.getName());
            } catch (Exception e) {
                log.warn("[OAuth2SuccessHandler] Failed to send welcome email to {}: {}", saved.getEmail(), e.getMessage());
            }

            return saved;
        });

        log.info("[OAuth2SuccessHandler] Step 4: User verified in database — ID: {}, Email: {}", user.getId(), user.getEmail());

        String token = tokenProvider.generateToken(user.getEmail());
        log.info("[OAuth2SuccessHandler] Step 5: Generated JWT token successfully for user '{}'", user.getEmail());

        boolean isXmlHttpRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (isXmlHttpRequest) {
            log.info("[OAuth2SuccessHandler] Step 6: Handling programmatic XMLHttpRequest. Returning JSON payload.");
            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .preferredLanguage(user.getPreferredLanguage())
                    .build();
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), authResponse);
        } else {
            String redirectUrl = String.format(
                "http://localhost:5173/oauth2/redirect?token=%s&id=%d&email=%s&name=%s",
                token,
                user.getId(),
                URLEncoder.encode(user.getEmail() != null ? user.getEmail() : "", StandardCharsets.UTF_8),
                URLEncoder.encode(user.getName() != null ? user.getName() : "", StandardCharsets.UTF_8)
            );
            log.info("[OAuth2SuccessHandler] Step 6: Redirecting browser to frontend callback URL: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
        }
    }
}
