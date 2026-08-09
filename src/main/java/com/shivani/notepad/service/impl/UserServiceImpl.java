package com.shivani.notepad.service.impl;

import com.shivani.notepad.dto.UserRegistrationDto;
import com.shivani.notepad.entity.Category;
import com.shivani.notepad.entity.Note;
import com.shivani.notepad.entity.NoteShare;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.exception.UserAlreadyExistsException;
import com.shivani.notepad.repository.*;
import com.shivani.notepad.service.EmailService;
import com.shivani.notepad.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of UserService.
 *
 * Handles the business logic for user registration, including:
 *  - Enforcing unique username/email constraints at the application level
 *    (in addition to the DB-level unique constraints, for a cleaner error
 *    message instead of a raw DataIntegrityViolationException).
 *  - Hashing passwords with BCrypt before persistence.
 */
@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final NoteRepository noteRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ReminderRepository reminderRepository;
    private final NoteShareRepository noteShareRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           EmailService emailService, NoteRepository noteRepository,
                           CategoryRepository categoryRepository,
                           TagRepository tagRepository, ReminderRepository reminderRepository,
                           NoteShareRepository noteShareRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.reminderRepository = reminderRepository;
        this.noteShareRepository = noteShareRepository;
    }
    @Value("${app.base-url}")
    private String baseUrl;
    @Override
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken: " + dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + dto.getEmail());
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmailVerified(false);
        user.setVerificationToken(java.util.UUID.randomUUID().toString());

        User saved = userRepository.save(user);
        createDefaultCategories(saved);
        sendVerificationEmail(saved);
        return saved;
    }
    @Override
    public User getUserByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " +username));
    }
    @Override
    public void sendVerificationEmail(User user) {
        // NOTE: hardcoding localhost:8080 here is a known simplification —
        // a production deployment would read the base URL from
        // application.properties (e.g. app.base-url) so this works
        // correctly once deployed to AWS in a later step. Flagged here
        // rather than left unexplained; we'll fix this properly during
        // the AWS deployment step.
        String link = baseUrl + "/verify-email?token=" + user.getVerificationToken();
        String body = "Hi " + user.getUsername() + ",\n\n"
                + "Please verify your email by clicking the link below:\n"
                + link + "\n\n"
                + "If you did not create this account, you can ignore this email.\n\n"
                + "Regards,\nNotepad App";

        emailService.sendEmail(user.getEmail(), "Verify your Notepad account", body);
    }

    @Override
    public boolean verifyEmail(String token) {
        return userRepository.findByVerificationToken(token)
                .map(user -> {
                    user.setEmailVerified(true);
                    user.setVerificationToken(null);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setResetToken(java.util.UUID.randomUUID().toString());
            user.setResetTokenExpiry(java.time.LocalDateTime.now().plusMinutes(30));
            userRepository.save(user);

            String link = "http://localhost:8080/reset-password?token=" + user.getResetToken();
            String body = "Hi " + user.getUsername() + ",\n\n"
                    + "You requested a password reset. Click the link below to set a new password "
                    + "(this link expires in 30 minutes):\n"
                    + link + "\n\n"
                    + "If you did not request this, you can safely ignore this email.\n\n"
                    + "Regards,\nNotepad App";

            emailService.sendEmail(user.getEmail(), "Reset your Notepad password", body);
        });
        // Deliberately no action (and no exception) if the email isn't
        // found — see interface javadoc for why.
    }

    @Override
    public boolean resetPassword(String token, String newPassword) {
        return userRepository.findByResetToken(token)
                .filter(user -> user.getResetTokenExpiry() != null
                        && user.getResetTokenExpiry().isAfter(java.time.LocalDateTime.now()))
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user.setResetToken(null);
                    user.setResetTokenExpiry(null);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }
    /**
     * Seeds every new account with a small set of default categories,
     * so a first-time user doesn't land on an empty Categories page.
     * These are ordinary Category rows owned by the user — fully
     * editable/deletable like any category they create themselves.
     */
    private void createDefaultCategories(User user) {
        String[] defaults = {"Work", "Personal", "Finance", "Health", "Education",
                "Travel", "Shopping", "Legal & Admin", "Projects", "Miscellaneous"};
        for (String name : defaults) {
            Category category = new Category();
            category.setName(name);
            category.setUser(user);
            categoryRepository.save(category);
        }
    }
    @Override
    public void updateNotificationPreferences(User user, boolean emailRemindersEnabled, boolean dailySummaryEnabled) {
        user.setEmailRemindersEnabled(emailRemindersEnabled);
        user.setDailySummaryEnabled(dailySummaryEnabled);
        userRepository.save(user);
    }

    @Override
    public void changePassword(User user, String newRawPassword) {
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
    }

    /**
     * Permanently deletes a user account and everything they own.
     *
     * Order matters here to avoid foreign key constraint violations:
     * shares must go before the notes/users they reference, notes must
     * go before categories/tags (though in this schema notes don't
     * strictly require that, it's the safer order), and the user row
     * must go last since almost everything else has a foreign key back
     * to it.
     *
     * @Transactional ensures this entire sequence either fully succeeds
     * or fully rolls back — we never want a half-deleted account.
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteAccount(User user) {
        // 1. Shares this user RECEIVED from other users' notes.
        List<NoteShare> receivedShares = noteShareRepository.findBySharedWithUser(user);
        noteShareRepository.deleteAll(receivedShares);

        // 2. Shares granted on THIS user's own notes, then the notes themselves
        //    (todos cascade automatically via Note's orphanRemoval; note_tags
        //    join rows are removed automatically by Hibernate since Note owns
        //    that relationship).
        List<Note> ownedNotes = noteRepository.findByUser(user);
        for (Note note : ownedNotes) {
            noteShareRepository.deleteAll(noteShareRepository.findByNote(note));
        }
        noteRepository.deleteAll(ownedNotes);

        // 3. Reminders.
        reminderRepository.deleteAll(reminderRepository.findByUser(user));



        // 5. Categories.
        categoryRepository.deleteAll(categoryRepository.findByUser(user));

        // 6. Tags.
        tagRepository.deleteAll(tagRepository.findByUser(user));

        // 7. Finally, the user account itself.
        userRepository.delete(user);
    }
}
