package com.pagemind.user;

import com.pagemind.user.dto.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping({"/api/user", "/v1/user"})
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDto dto = UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .dob(user.getDob())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .preferredLanguage(user.getPreferredLanguage())
                .profilePicUrl(user.getProfilePicUrl())
                .build();

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Principal principal, @RequestBody UserProfileDto request) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        if (request.getDob() != null) user.setDob(request.getDob());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getPreferredLanguage() != null) user.setPreferredLanguage(request.getPreferredLanguage());
        if (request.getProfilePicUrl() != null) user.setProfilePicUrl(request.getProfilePicUrl());

        User updatedUser = userRepository.save(user);

        UserProfileDto dto = UserProfileDto.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .dob(updatedUser.getDob())
                .phoneNumber(updatedUser.getPhoneNumber())
                .gender(updatedUser.getGender())
                .preferredLanguage(updatedUser.getPreferredLanguage())
                .profilePicUrl(updatedUser.getProfilePicUrl())
                .build();

        return ResponseEntity.ok(dto);
    }
}
