package com.shivani.notepad.controller;

import com.shivani.notepad.dto.UserRegistrationDto;
import com.shivani.notepad.exception.UserAlreadyExistsException;
import com.shivani.notepad.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handles all user-facing authentication routes: login page display,
 * registration page display, and registration form submission.
 *
 * Note: the actual login POST is intercepted and handled by Spring
 * Security's form login mechanism (configured in SecurityConfig), so
 * this controller does NOT need a @PostMapping("/login") — only the GET
 * to display the page.
 */
@Controller
public class UserController {
    private final UserService userService;
    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }
    /**
     * Displays the login page.
     * Spring Security handles the actual authentication logic on POST /login.
     */
    @GetMapping("/login")
    public String showLoginPage(){
        return "login";
    }
    /**
     * Displays the registration page with an empty DTO bound to the form.
     */
    @GetMapping("/register")
    public String showRegistrationPage(Model model){
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }
    /**
     * Processes the registration form submission.
     *
     * @Valid triggers the Bean Validation annotations on UserRegistrationDto
     * (e.g., @NotBlank, @Email, @Size). BindingResult captures any validation
     * failures so we can re-render the form with error messages instead of
     * throwing an exception.
     */
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") UserRegistrationDto registrationDto, BindingResult result, Model model
    ){
        // Re-display the form with field-level validation errors if any exist.
        if(result.hasErrors()){
            return "register";
        }
        // Confirm password matches — this is a cross-field check that Bean
        // Validation annotations can't easily express, so we do it manually.
        if(!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())){
            model.addAttribute("passwordError", "Passwords do not match");
            return "register";
        }
        try{
            userService.registerUser(registrationDto);
        }
        catch(UserAlreadyExistsException ex){
            // Show a friendly error instead of a raw stack trace.
            model.addAttribute("registrationError", ex.getMessage());
            return "register";
        }
        // Redirect to login with a success flag so we can show a
        // "registration successful" message on the login page.
        return "redirect:/login?registered";
    }
    /** Handles the email verification link clicked from the registration email. */
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, Model model) {
        boolean verified = userService.verifyEmail(token);
        model.addAttribute("verified", verified);
        return "verify-email-result";
    }

    /** Displays the "enter your email" forgot-password form. */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    /** Processes the forgot-password form submission. */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        userService.initiatePasswordReset(email);
        // Same message regardless of whether the email exists — see
        // UserService.initiatePasswordReset() javadoc for the reasoning.
        model.addAttribute("message",
                "If an account with that email exists, a password reset link has been sent.");
        return "forgot-password";
    }

    /** Displays the "set a new password" form, given a valid token in the URL. */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    /** Processes the reset-password form submission. */
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Passwords do not match.");
            return "reset-password";
        }

        if (!com.shivani.notepad.util.PasswordValidator.isStrong(newPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", com.shivani.notepad.util.PasswordValidator.REQUIREMENTS_MESSAGE);
            return "reset-password";
        }

        return "redirect:/login?reset";
    }
}
