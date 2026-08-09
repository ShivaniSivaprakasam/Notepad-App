package com.shivani.notepad.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
/**
 * Data Transfer Object used to capture user credentials during login.
 * Kept separate from UserRegistrationDto since login only requires
 * identifier + password, not the full registration field set.
 */
@Data
public class LoginDto {
    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Password is required")
    private String password;
}
