package com.shivani.notepad.service;

import com.shivani.notepad.dto.UserRegistrationDto;
import com.shivani.notepad.entity.User;

/**
 * Business logic contract for user-related operations: registration,
 * profile retrieval, and password management.
 *
 * Authentication itself (login) is delegated to Spring Security, which
 * uses UserDetailsService under the hood (configured separately) — this
 * service focuses on registration and profile-related use cases.
 */
public interface UserService {

    /**
     * Registers a new user after validating uniqueness of username/email
     * and hashing the raw password before persisting.
     *
     * @param registrationDto the incoming registration form data
     * @return the persisted User entity
     */
    User registerUser(UserRegistrationDto registrationDto);

    /**
     * Retrieves a user by their username.
     *
     * @param username the username to search for
     * @return the matching User entity
     */
    User getUserByUsername(String username);
    /** Sends (or re-sends) the email verification link to a user. */
    void sendVerificationEmail(User user);

    /** Verifies a user's email using the token from the verification link. */
    boolean verifyEmail(String token);

    /**
     * Initiates a password reset: generates a token, stores it with an
     * expiry, and emails the reset link. Always "succeeds" from the
     * caller's perspective even if the email doesn't exist — this
     * prevents leaking which emails are registered (a standard security
     * practice for forgot-password flows).
     */
    void initiatePasswordReset(String email);

    /**
     * Completes a password reset using the token from the reset link.
     * Returns false if the token is invalid or expired.
     */
    boolean resetPassword(String token, String newPassword);

    void updateNotificationPreferences(User user, boolean emailRemindersEnabled, boolean dailySummaryEnabled);

    void changePassword(User user, String newRawPassword);

    void deleteAccount(User user);
}
