package com.shivani.notepad.config;

import com.shivani.notepad.entity.Reminder;
import com.shivani.notepad.entity.Todo;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.repository.ReminderRepository;
import com.shivani.notepad.repository.TodoRepository;
import com.shivani.notepad.repository.UserRepository;
import com.shivani.notepad.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Sends every user a "Good Morning" email summarizing their pending
 * tasks and today's reminders, once per day.
 *
 * Uses a cron expression (rather than fixedRate like the reminder
 * scheduler) since this needs to run at a specific time of day, not
 * on a repeating interval from app startup.
 */
@Component
public class DailySummaryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DailySummaryScheduler.class);

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final ReminderRepository reminderRepository;
    private final EmailService emailService;

    @Autowired
    public DailySummaryScheduler(UserRepository userRepository, TodoRepository todoRepository,
                                 ReminderRepository reminderRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.todoRepository = todoRepository;
        this.reminderRepository = reminderRepository;
        this.emailService = emailService;
    }

    /**
     * Cron: second, minute, hour, day-of-month, month, day-of-week.
     * Runs at 7:00 AM server time every day.
     *
     * NOTE: this uses the server's local timezone, which is a real
     * simplification — a multi-timezone-aware app would need to store
     * each user's preferred timezone and schedule per-user. Fine for a
     * single-region portfolio deployment, worth flagging if asked.
     */
    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Kolkata")
    public void sendDailySummaries() {
        List<User> allUsers = userRepository.findAll();
        LocalDate today = LocalDate.now();

        for (User user : allUsers) {
            if (!user.isDailySummaryEnabled()) {
                continue;
            }
            List<Todo> pendingTasks = todoRepository.findIncompleteByUser(user);
            List<Reminder> todaysReminders = reminderRepository.findByUserAndDate(user, today);

            String body = buildSummaryBody(user, pendingTasks, todaysReminders);
            emailService.sendEmail(user.getEmail(), "Good Morning! Your Daily Summary", body);
        }

        logger.info("Daily summary emails sent to {} user(s)", allUsers.size());
    }

    private String buildSummaryBody(User user, List<Todo> pendingTasks, List<Reminder> reminders) {
        StringBuilder body = new StringBuilder();
        body.append("Good Morning, ").append(user.getUsername()).append("!\n\n");

        if (pendingTasks.isEmpty() && reminders.isEmpty()) {
            body.append("No work scheduled today. Enjoy your day!\n");
        } else {
            if (!pendingTasks.isEmpty()) {
                body.append("Pending Tasks:\n");
                for (Todo task : pendingTasks) {
                    body.append("- ").append(task.getTask()).append("\n");
                }
                body.append("\n");
            }

            if (!reminders.isEmpty()) {
                body.append("Today's Reminders:\n");
                for (Reminder reminder : reminders) {
                    body.append("- ").append(reminder.getTitle())
                            .append(" at ").append(reminder.getTime()).append("\n");
                }
                body.append("\n");
            }

            body.append("Have a productive day!\n");
        }

        return body.toString();
    }
}