package com.generic.rest.main.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for Google OAuth authentication
 */
public record GoogleAuthRequest(
        @NotBlank(message = "Google token is required")
        String googleToken
) {
}
