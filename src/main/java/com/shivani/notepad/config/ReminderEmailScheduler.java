package com.shivani.notepad.config;

import com.shivani.notepad.entity.Reminder;
import com.shivani.notepad.repository.ReminderRepository;
import com.shivani.notepad.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Fires every 60 seconds while the application is running. Checks for
 * any reminder whose date+time has already passed and whose email has
 * not yet been sent, then sends it.
 *
 * IMPORTANT: this only runs while the Java process is alive. Stopping
 * the app (or, later, stopping its Docker container) stops this
 * scheduler entirely — there is no way around that without the process
 * running continuously somewhere (this is what the eventual AWS
 * deployment step provides).
 */
@Component
public class ReminderEmailScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReminderEmailScheduler.class);

    private final ReminderRepository reminderRepository;
    private final EmailService emailService;

    @Autowired
    public ReminderEmailScheduler(ReminderRepository reminderRepository, EmailService emailService) {
        this.reminderRepository = reminderRepository;
        this.emailService = emailService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendDueReminderEmails() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        logger.info("[ReminderScheduler] tick at {}", now);

        List<Reminder> candidates = reminderRepository.findDueForEmail(today);
        logger.info("[ReminderScheduler] {} candidate reminder(s) with date <= today and notificationSent=false", candidates.size());

        for (Reminder reminder : candidates) {
            LocalDateTime reminderDateTime = LocalDateTime.of(reminder.getDate(), reminder.getTime());
            boolean isDue = !reminderDateTime.isAfter(now);

            logger.info("[ReminderScheduler] reminder id={} title='{}' scheduledFor={} isDue={}",
                    reminder.getId(), reminder.getTitle(), reminderDateTime, isDue);

            if (!isDue) {
                continue;
            }

            boolean emailEnabled = reminder.getUser().isEmailRemindersEnabled();
            logger.info("[ReminderScheduler] reminder id={} user={} emailRemindersEnabled={}",
                    reminder.getId(), reminder.getUser().getUsername(), emailEnabled);

            if (emailEnabled) {
                try {
                    String userEmail = reminder.getUser().getEmail();
                    emailService.sendEmail(userEmail, "Reminder: " + reminder.getTitle(), buildEmailBody(reminder));
                    logger.info("[ReminderScheduler] email dispatched OK for reminder id={} to {}", reminder.getId(), userEmail);
                } catch (Exception emailEx) {
                    // Caught here so ONE bad email never stops the loop
                    // from processing the rest of the due reminders.
                    logger.error("[ReminderScheduler] email FAILED for reminder id={}", reminder.getId(), emailEx);
                }
            } else {
                logger.info("[ReminderScheduler] skipping email for reminder id={} — disabled by user preference", reminder.getId());
            }

            reminder.setNotificationSent(true);
            reminderRepository.save(reminder);
        }
    }

    private String buildEmailBody(Reminder reminder) {
        StringBuilder body = new StringBuilder();
        body.append("Hi ").append(reminder.getUser().getUsername()).append(",\n\n");
        body.append("This is a reminder for: ").append(reminder.getTitle()).append("\n");
        body.append("Scheduled for: ").append(reminder.getDate()).append(" at ").append(reminder.getTime()).append("\n");
        if (reminder.getDescription() != null && !reminder.getDescription().isBlank()) {
            body.append("\nDetails: ").append(reminder.getDescription()).append("\n");
        }
        body.append("\nRegards,\nNotepad App");
        return body.toString();
    }
}