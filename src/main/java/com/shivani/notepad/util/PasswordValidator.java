package com.shivani.notepad.util;

/**
 * Shared password strength rule, used by both the registration form
 * (via a JSR-380 annotation) and the reset-password / protect-note
 * flows (via manual checks, since those aren't bound through a
 * validated DTO).
 *
 * Rule: at least 8 characters, containing at least one lowercase
 * letter, one uppercase letter, and one digit.
 */
public class PasswordValidator {

    public static final String STRONG_PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

    public static final String REQUIREMENTS_MESSAGE =
            "Password must be at least 8 characters and include an uppercase letter, a lowercase letter, and a number.";

    public static boolean isStrong(String password) {
        return password != null && password.matches(STRONG_PASSWORD_REGEX);
    }
}