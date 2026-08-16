package com.pagemind.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    private Long id;
    private String name;
    private String email;
    private String dob;
    private String phoneNumber;
    private String gender;
    private String preferredLanguage;
    private String profilePicUrl;
}
