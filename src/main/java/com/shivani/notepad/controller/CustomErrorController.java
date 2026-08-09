package com.shivani.notepad.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles errors that occur outside normal controller method execution
 * (e.g., a 404 for a completely unmapped URL, or a 405 for an
 * unsupported HTTP method on a valid route). Spring Boot routes all
 * such errors to "/error" by default; implementing ErrorController lets
 * us intercept that and show our own styled error page instead of the
 * default Whitelabel Error Page.
 */
@Controller
public class CustomErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = (status != null) ? Integer.parseInt(status.toString()) : 500;

        String title;
        String message;

        if (statusCode == 404) {
            title = "Page Not Found";
            message = "The page you're looking for doesn't exist.";
        } else if (statusCode == 403) {
            title = "Access Denied";
            message = "You don't have permission to access this page.";
        } else {
            title = "Something Went Wrong";
            message = "An unexpected error occurred. Please try again.";
        }

        model.addAttribute("errorTitle", title);
        model.addAttribute("errorMessage", message);
        model.addAttribute("statusCode", statusCode);
        return "error";
    }
}
