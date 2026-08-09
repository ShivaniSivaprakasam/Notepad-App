package com.shivani.notepad.controller;

import com.shivani.notepad.entity.Reminder;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.repository.ReminderRepository;
import com.shivani.notepad.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class NotificationController {

    private final ReminderRepository reminderRepository;
    private final UserService userService;

    @Autowired
    public NotificationController(ReminderRepository reminderRepository, UserService userService) {
        this.reminderRepository = reminderRepository;
        this.userService = userService;
    }

    @GetMapping("/notifications")
    public String showNotifications(Authentication authentication, Model model) {
        User user = userService.getUserByUsername(authentication.getName());
        LocalDate today = LocalDate.now();

        List<Reminder> upcoming = reminderRepository
                .findByUserAndDateBetweenOrderByDateAscTimeAsc(user, today, today.plusDays(30));

        List<Reminder> recent = reminderRepository
                .findByUserAndDateBetweenOrderByDateAscTimeAsc(user, today.minusDays(7), today)
                .stream()
                .filter(Reminder::isNotificationSent)
                .collect(Collectors.toList());

        model.addAttribute("upcoming", upcoming);
        model.addAttribute("recent", recent);
        return "notifications";
    }
}