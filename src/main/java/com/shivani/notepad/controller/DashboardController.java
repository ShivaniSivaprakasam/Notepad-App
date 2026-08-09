package com.shivani.notepad.controller;

import com.shivani.notepad.dto.DashboardStats;
import com.shivani.notepad.entity.NoteStatus;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.repository.CategoryRepository;
import com.shivani.notepad.repository.NoteRepository;
import com.shivani.notepad.repository.TodoRepository;
import com.shivani.notepad.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;
    private final NoteRepository noteRepository;
    private final CategoryRepository categoryRepository;
    private final TodoRepository todoRepository;

    @Autowired
    public DashboardController(UserService userService, NoteRepository noteRepository,
                               CategoryRepository categoryRepository, TodoRepository todoRepository) {
        this.userService = userService;
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
        this.todoRepository = todoRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        User user = userService.getUserByUsername(authentication.getName());

        long totalNotes = noteRepository.countByUserAndStatus(user, NoteStatus.ACTIVE);
        long totalCategories = categoryRepository.countByUser(user);

        // Uses the trashed-note-excluding counts, so a task belonging to
        // a note currently in Trash no longer inflates these numbers —
        // matches exactly what appears on the /todos page.
        long totalTodos = todoRepository.countVisibleByUser(user);
        long completedTodos = todoRepository.countVisibleByUserAndCompleted(user, true);

        int productivityScore = (totalTodos == 0)
                ? 0
                : (int) Math.round((completedTodos * 100.0) / totalTodos);

        DashboardStats stats = new DashboardStats(
                totalNotes, totalCategories, totalTodos, completedTodos, productivityScore);

        model.addAttribute("stats", stats);
        return "dashboard";
    }
}