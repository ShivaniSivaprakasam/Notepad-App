package com.shivani.notepad.controller;

import com.shivani.notepad.dto.TodoDto;
import com.shivani.notepad.entity.Todo;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.service.TodoService;
import com.shivani.notepad.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/todo")
public class TodoController {

    private final TodoService todoService;
    private final UserService userService;

    @Autowired
    public TodoController(TodoService todoService, UserService userService) {
        this.todoService = todoService;
        this.userService = userService;
    }

    /**
     * Registers a converter that treats an empty submitted value as null
     * for any Integer-typed field bound from a form (here: TodoDto.noteId).
     * Without this, an empty "noteId" from the "-- No note --" dropdown
     * option fails Spring's default String-to-Integer conversion BEFORE
     * the controller method body runs, producing an uncaught runtime
     * error that no try/catch inside the method can intercept.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
    }

    private User getCurrentUser(Authentication authentication) {
        return userService.getUserByUsername(authentication.getName());
    }

    @PostMapping
    public String createTask(@ModelAttribute TodoDto dto, Authentication authentication,
                             @RequestParam(required = false) String returnTo,
                             org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(authentication);
        try {
            Todo saved = todoService.createTask(dto, user);
            if (saved.getNote() != null && !"todos".equals(returnTo)) {
                return "redirect:/notes/" + saved.getNote().getId();
            }
            return "redirect:/todos";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("todoError", ex.getMessage());
            if (dto.getNoteId() != null && !"todos".equals(returnTo)) {
                return "redirect:/notes/" + dto.getNoteId();
            }
            return "redirect:/todos";
        }
    }

    @PostMapping("/update")
    public String updateTask(@RequestParam Integer todoId,
                             @RequestParam(required = false) Integer noteId,
                             @RequestParam(required = false) Boolean completed,
                             Authentication authentication,
                             @RequestParam(required = false) String returnTo) {
        User user = getCurrentUser(authentication);
        boolean shouldComplete = completed != null && completed;
        if (shouldComplete) todoService.markComplete(todoId, user);
        else todoService.markIncomplete(todoId, user);

        if (noteId != null && !"todos".equals(returnTo)) return "redirect:/notes/" + noteId;
        return "redirect:/todos";
    }

    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Integer id,
                             @RequestParam(required = false) Integer noteId,
                             Authentication authentication,
                             @RequestParam(required = false) String returnTo) {
        User user = getCurrentUser(authentication);
        todoService.deleteTask(id, user);
        if (noteId != null && !"todos".equals(returnTo)) return "redirect:/notes/" + noteId;
        return "redirect:/todos";
    }
}