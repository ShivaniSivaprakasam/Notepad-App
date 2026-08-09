package com.shivani.notepad.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
/**
 * Data Transfer Object used to capture user input during registration.
 *
 * Using a DTO instead of binding directly to the User entity keeps the
 * entity's persistence concerns (JPA annotations, DB constraints) separate
 * from form validation concerns, and avoids exposing internal fields
 * (like the hashed password or timestamps) to the presentation layer.
 */
@Data
public class UserRegistrationDto {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    @NotBlank(message = "Password is required")
    @jakarta.validation.constraints.Pattern(
            regexp = com.shivani.notepad.util.PasswordValidator.STRONG_PASSWORD_REGEX,
            message = com.shivani.notepad.util.PasswordValidator.REQUIREMENTS_MESSAGE)
    private String password;
    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;
}
