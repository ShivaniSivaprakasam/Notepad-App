package com.shivani.notepad.controller;

import com.shivani.notepad.entity.User;
import com.shivani.notepad.service.UserService;
import com.shivani.notepad.util.PasswordValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ProfileController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    private User getCurrentUser(Authentication authentication) {
        return userService.getUserByUsername(authentication.getName());
    }

    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        model.addAttribute("user", getCurrentUser(authentication));
        return "profile";
    }

    @PostMapping("/notifications")
    public String updateNotificationPreferences(
            @RequestParam(required = false) Boolean emailRemindersEnabled,
            @RequestParam(required = false) Boolean dailySummaryEnabled,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        // Unchecked checkboxes simply don't submit a parameter at all, so
        // a null here correctly means "the box was unticked."
        userService.updateNotificationPreferences(
                user, emailRemindersEnabled != null, dailySummaryEnabled != null);
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("user", user);
            model.addAttribute("passwordError", "Current password is incorrect.");
            return "profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("user", user);
            model.addAttribute("passwordError", "New passwords do not match.");
            return "profile";
        }
        if (!PasswordValidator.isStrong(newPassword)) {
            model.addAttribute("user", user);
            model.addAttribute("passwordError", PasswordValidator.REQUIREMENTS_MESSAGE);
            return "profile";
        }

        userService.changePassword(user, newPassword);
        model.addAttribute("user", user);
        model.addAttribute("passwordSuccess", "Password updated successfully.");
        return "profile";
    }

    @PostMapping("/delete")
    public String deleteAccount(@RequestParam String password,
                                Authentication authentication,
                                HttpServletRequest request,
                                Model model) {
        User user = getCurrentUser(authentication);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            model.addAttribute("user", user);
            model.addAttribute("deleteError", "Incorrect password. Account not deleted.");
            return "profile";
        }

        userService.deleteAccount(user);

        try {
            request.logout(); // cleanly ends the session/security context
        } catch (jakarta.servlet.ServletException ignored) {
            // Session is being torn down anyway — nothing meaningful to
            // recover from here.
        }

        return "redirect:/login?accountDeleted";
    }
}