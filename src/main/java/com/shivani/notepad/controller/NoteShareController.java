package com.shivani.notepad.controller;

import com.shivani.notepad.dto.NoteShareDto;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.service.NoteShareService;
import com.shivani.notepad.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notes")
public class NoteShareController {

    private final NoteShareService noteShareService;
    private final UserService userService;

    @Autowired
    public NoteShareController(NoteShareService noteShareService, UserService userService) {
        this.noteShareService = noteShareService;
        this.userService = userService;
    }

    private User getCurrentUser(Authentication authentication) {
        return userService.getUserByUsername(authentication.getName());
    }

    /** Displays sharing management for a note: current shares + a form to add a new one. Owner-only. */
    @GetMapping("/{id}/share")
    public String showShareManagement(@PathVariable Integer id, Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("noteId", id);
        model.addAttribute("shares", noteShareService.getSharesForNote(id, user));
        model.addAttribute("shareDto", new NoteShareDto());
        return "note-share";
    }

    /** Processes adding (or updating) a share on a note. Owner-only. */
    @PostMapping("/{id}/share")
    public String addShare(@PathVariable Integer id, @ModelAttribute NoteShareDto dto,
                           Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);

        try {
            noteShareService.shareNote(id, user, dto.getEmail(), dto.getPermission());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("noteId", id);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("shares", noteShareService.getSharesForNote(id, user));
            model.addAttribute("shareDto", dto);
            return "note-share";
        }

        return "redirect:/notes/" + id + "/share";
    }

    /** Revokes a share. Owner-only. */
    @GetMapping("/share/delete/{shareId}")
    public String removeShare(@PathVariable Integer shareId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Integer noteId = noteShareService.unshareNote(shareId, user);
        return "redirect:/notes/" + noteId + "/share";
    }

    /** Lists every note shared with the current user by other people. */
    @GetMapping("/shared-with-me")
    public String showSharedWithMe(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("shares", noteShareService.getNotesSharedWithMe(user));
        return "shared-with-me";
    }
    /** Unified view: notes I've shared with others, and notes others have shared with me. */
    @GetMapping("/shared")
    public String showAllShared(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("sharedByMe", noteShareService.getSharesGrantedByUser(user));
        model.addAttribute("sharedWithMe", noteShareService.getNotesSharedWithMe(user));
        return "shared-notes";
    }
}