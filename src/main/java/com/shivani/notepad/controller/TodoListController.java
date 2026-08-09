package com.shivani.notepad.controller;

import com.shivani.notepad.dto.TodoDto;
import com.shivani.notepad.entity.NoteStatus;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.repository.NoteRepository;
import com.shivani.notepad.service.TodoService;
import com.shivani.notepad.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/todos")
public class TodoListController {

    private final TodoService todoService;
    private final NoteRepository noteRepository;
    private final UserService userService;

    @Autowired
    public TodoListController(TodoService todoService, NoteRepository noteRepository, UserService userService) {
        this.todoService = todoService;
        this.noteRepository = noteRepository;
        this.userService = userService;
    }

    @GetMapping
    public String listAllTodos(Authentication authentication, Model model) {
        User user = userService.getUserByUsername(authentication.getName());
        model.addAttribute("todos", todoService.getAllTasks(user));
        model.addAttribute("notes", noteRepository.findByUserAndStatus(user, NoteStatus.ACTIVE));
        model.addAttribute("newTodo", new TodoDto());
        return "todo-list";
    }
}