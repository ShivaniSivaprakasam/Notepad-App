package com.shivani.notepad.repository;

import com.shivani.notepad.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
/**
 * Repository interface for managing {@link User} entities.
 *
 * Provides CRUD operations through {@link JpaRepository} and
 * custom query methods for user authentication and registration
 * workflows.
 */
@Repository
public interface UserRepository extends JpaRepository<User,Integer>{
    /**
     * Retrieves a user by email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByEmail(String email);
    /**
     * Retrieves a user by username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByUsername(String username);
    /**
     * Checks whether a user with the specified email already exists.
     *
     * @param email the email address to check
     * @return true if a user exists, otherwise false
     */
    boolean existsByEmail(String email);
    /**
     * Checks whether a user with the specified username already exists.
     *
     * @param username the username to check
     * @return true if a user exists, otherwise false
     */
    boolean existsByUsername(String username);
    Optional<User> findByVerificationToken(String verificationToken);
    Optional<User> findByResetToken(String resetToken);
}
