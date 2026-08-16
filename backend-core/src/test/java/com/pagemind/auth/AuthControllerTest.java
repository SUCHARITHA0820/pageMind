package com.pagemind.auth;

import com.pagemind.auth.dto.AuthResponse;
import com.pagemind.auth.dto.SignupRequest;
import com.pagemind.config.JwtTokenProvider;
import com.pagemind.user.AuthProvider;
import com.pagemind.user.User;
import com.pagemind.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetCodeRepository resetCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthController authController;

    private SignupRequest signupRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setName("Jane Doe");
        signupRequest.setEmail("jane@example.com");
        signupRequest.setPassword("securepass123");
        signupRequest.setPreferredLanguage("en");

        savedUser = User.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .passwordHash("encoded_password")
                .authProvider(AuthProvider.local)
                .preferredLanguage("en")
                .build();
    }

    @Test
    void registerUser_firstTimeSignup_sendsWelcomeEmailAndReturnsCreated() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("securepass123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateToken("jane@example.com")).thenReturn("mocked_jwt_token");

        ResponseEntity<?> response = authController.registerUser(signupRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof AuthResponse);

        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("mocked_jwt_token", authResponse.getToken());
        assertEquals("jane@example.com", authResponse.getEmail());
        assertEquals("Jane Doe", authResponse.getName());

        // Verify that welcome notification email was dispatched to first-time signup user
        verify(emailService, times(1)).sendWelcomeEmail("jane@example.com", "Jane Doe");
    }

    @Test
    void registerUser_existingEmail_returnsConflictAndDoesNotSendEmail() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signupRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(emailService, never()).sendWelcomeEmail(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
