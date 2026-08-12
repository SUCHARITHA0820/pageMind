package com.pagemind.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse {

    private boolean success;
    private String message;
    private String devFallbackCode;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.devFallbackCode = null;
    }
}
