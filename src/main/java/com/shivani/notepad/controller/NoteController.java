package com.shivani.notepad.controller;

import com.shivani.notepad.dto.NoteDto;
import com.shivani.notepad.dto.TodoDto;
import com.shivani.notepad.entity.Note;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.entity.Tag;
import com.shivani.notepad.repository.CategoryRepository;
import com.shivani.notepad.service.CategoryService;
import com.shivani.notepad.service.NoteService;
import com.shivani.notepad.service.UserService;
import com.shivani.notepad.util.PasswordValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.shivani.notepad.dto.NoteSearchCriteria;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all note-related routes: listing, creating, viewing, editing,
 * deleting, and searching notes.
 *
 * Every method resolves the currently authenticated user via Spring
 * Security's Authentication object, then passes that User into the
 * service layer so ownership checks are enforced consistently.
 */
@Controller
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    @Autowired
    public NoteController(NoteService noteService, UserService userService,
                          CategoryRepository categoryRepository, CategoryService categoryService) {
        this.noteService = noteService;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }
    private static final String UNLOCKED_SESSION_KEY = "unlockedNoteIds";

    @SuppressWarnings("unchecked")
    private Set<Integer> getUnlockedSet(HttpSession session) {
        Set<Integer> unlocked = (Set<Integer>) session.getAttribute(UNLOCKED_SESSION_KEY);
        if (unlocked == null) {
            unlocked = new HashSet<>();
            session.setAttribute(UNLOCKED_SESSION_KEY, unlocked);
        }
        return unlocked;
    }

    /**
     * Helper to resolve the full User entity from the Authentication
     * principal's username. Used at the top of every method below to
     * avoid repeating this lookup logic five times.
     */
    private User getCurrentUser(Authentication authentication) {
        return userService.getUserByUsername(authentication.getName());
    }

    /** Displays the list of all notes belonging to the logged-in user. */
    @GetMapping
    public String listNotes(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        List<Note> notes = noteService.getAllNotes(user);

        // Group notes by tag so notes sharing a tag are shown together.
        // A note with multiple tags appears once per tag group; notes
        // with no tags go in a separate "Untagged" bucket.
        Map<String, List<Note>> notesByTag = new TreeMap<>();
        List<Note> untaggedNotes = new ArrayList<>();
        for (Note note : notes) {
            if (note.getTags() == null || note.getTags().isEmpty()) {
                untaggedNotes.add(note);
            } else {
                for (Tag tag : note.getTags()) {
                    notesByTag.computeIfAbsent(tag.getName(), k -> new ArrayList<>()).add(note);
                }
            }
        }

        model.addAttribute("notesByTag", notesByTag);
        model.addAttribute("untaggedNotes", untaggedNotes);
        return "notes";
    }

    /** Displays the empty form for creating a new note. */
    @GetMapping("/new")
    public String showCreateForm(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("note", new NoteDto());
        model.addAttribute("categories", categoryService.getCategories(user)); // was: categoryRepository.findByUser(user)
        return "create-note";
    }

    /** Processes the new-note form submission. */
    @PostMapping
    public String createNote(@Valid @ModelAttribute("note") NoteDto noteDto,
                             BindingResult result,
                             Authentication authentication,
                             Model model) {
        User user = getCurrentUser(authentication);

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findByUser(user));
            return "create-note";
        }

        Note saved = noteService.createNote(noteDto, user);
        return "redirect:/notes/" + saved.getId();
    }

    /** Displays a single note in read-only view mode. */
    @GetMapping("/{id}")
    public String viewNote(@PathVariable Integer id, Authentication authentication, Model model, HttpSession session) {
        User user = getCurrentUser(authentication);
        Note note = noteService.getAccessibleNote(id, user);

        if (note.isPasswordProtected() && !getUnlockedSet(session).contains(id)) {
            return "redirect:/notes/unlock/" + id;
        }

        boolean isOwner = note.getUser().getId().equals(user.getId());
        model.addAttribute("note", note);
        model.addAttribute("newTodo", new TodoDto());
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("canEdit", noteService.canEdit(note, user));
        return "view-note";
    }

    /** Displays the edit form pre-filled with the note's current data. */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        Note note = noteService.getEditableNote(id, user);
        HttpSession session = null;
        if (note.isPasswordProtected() && !getUnlockedSet(session).contains(id)) {
            return "redirect:/notes/unlock/" + id;
        }
        NoteDto dto = new NoteDto();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        if (note.getCategory() != null) dto.setCategoryId(note.getCategory().getId());
        dto.setTagsInput(note.getTags().stream().map(Tag::getName)
                .collect(Collectors.joining(", ")));

        boolean isOwner = note.getUser().getId().equals(user.getId());
        model.addAttribute("note", dto);
        model.addAttribute("categories", isOwner ? categoryService.getCategories(user) : Collections.emptyList()); // was: categoryRepository.findByUser(user)
        model.addAttribute("isOwner", isOwner);
        return "edit-note";
    }

    /** Processes the edit-note form submission. */
    @PostMapping("/update")
    public String updateNote(@Valid @ModelAttribute("note") NoteDto noteDto,
                             BindingResult result,
                             Authentication authentication,
                             Model model,
                             HttpSession session) {
        User user = getCurrentUser(authentication);
        Note existing = noteService.getEditableNote(noteDto.getId(), user);

        if (existing.isPasswordProtected() && !getUnlockedSet(session).contains(existing.getId())) {
            return "redirect:/notes/unlock/" + existing.getId();
        }

        if (result.hasErrors()) {
            boolean isOwner = existing.getUser().getId().equals(user.getId());
            model.addAttribute("categories", isOwner ? categoryRepository.findByUser(user) : Collections.emptyList());
            model.addAttribute("isOwner", isOwner);
            return "edit-note";
        }

        Note updated = noteService.updateNote(noteDto, user);
        return "redirect:/notes/" + updated.getId();
    }

    /**
     * Moves a note to the trash (soft delete) and redirects back to the
     * notes list. This replaces permanent deletion — see /notes/trash/delete
     * for the actual irreversible deletion, triggered only from the Trash view.
     */
    @GetMapping("/delete/{id}")
    public String deleteNote(@PathVariable Integer id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        noteService.moveToTrash(id, user);
        return "redirect:/notes";
    }

    /** Toggles a note's pinned state and redirects back to the notes list. */
    @GetMapping("/pin/{id}")
    public String togglePin(@PathVariable Integer id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        noteService.togglePin(id, user);
        return "redirect:/notes";
    }

    /** Toggles a note's favorite state and redirects back to the notes list. */
    @GetMapping("/favorite/{id}")
    public String toggleFavorite(@PathVariable Integer id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        noteService.toggleFavorite(id, user);
        return "redirect:/notes";
    }

    /** Displays only the user's favorited notes. */
    @GetMapping("/favorites")
    public String listFavorites(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("notes", noteService.getFavoriteNotes(user));
        model.addAttribute("viewTitle", "Favorite Notes");
        return "notes";
    }

    /** Archives a note and redirects back to the notes list. */
    @GetMapping("/archive/{id}")
    public String archiveNote(@PathVariable Integer id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        noteService.archiveNote(id, user);
        return "redirect:/notes";
    }

    /** Displays all archived notes with an option to unarchive (restore) each one. */
    @GetMapping("/archived")
    public String listArchived(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("notes", noteService.getArchivedNotes(user));
        return "archived";
    }

    /** Displays all trashed notes. */
    @GetMapping("/trash")
    public String listTrash(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("notes", noteService.getTrashedNotes(user));
        model.addAttribute("viewTitle", "Trash");
        return "trash";
    }

    /** Restores a trashed or archived note back to active status. */
    @GetMapping("/restore/{id}")
    public String restoreNote(@PathVariable Integer id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        noteService.restoreNote(id, user);
        return "redirect:/notes/trash";
    }

    /**
     * Permanently deletes a trashed note. This is irreversible and only
     * reachable from the Trash view — never from the main notes list.
     */
    @GetMapping("/trash/delete/{id}")
    public String deletePermanently(@PathVariable Integer id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        noteService.deletePermanently(id, user);
        return "redirect:/notes/trash";
    }
    /** Displays the advanced search form. */
    @GetMapping("/search/advanced")
    public String showAdvancedSearchForm(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("criteria", new NoteSearchCriteria());
        model.addAttribute("categories", categoryRepository.findByUser(user));
        return "advanced-search";
    }

    /** Processes the advanced search form submission. */
    @GetMapping("/search/advanced/results")
    public String runAdvancedSearch(NoteSearchCriteria criteria, Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("notes", noteService.advancedSearch(criteria, user));
        model.addAttribute("categories", categoryRepository.findByUser(user));
        model.addAttribute("criteria", criteria);
        model.addAttribute("viewTitle", "Advanced Search Results");
        return "advanced-search";
    }
    /**
     * Auto-save endpoint used by the editor's periodic save timer (see
     * autosave.js). Unlike the regular /notes/update endpoint, this
     * returns a small JSON response instead of a redirect, since it's
     * called silently in the background via fetch() rather than a full
     * form submission.
     */
    @PostMapping("/autosave")
    @ResponseBody
    public Map<String, Object> autoSave(@ModelAttribute NoteDto noteDto, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Map<String, Object> response = new HashMap<>();

        try {
            Note updated = noteService.updateNote(noteDto, user);
            response.put("status", "success");
            response.put("savedAt", updated.getUpdatedAt().toString());
        } catch (Exception ex) {
            // Auto-save failures should never interrupt the user's typing —
            // just report failure back to the JS so it can show "Save failed"
            // instead of throwing an error page.
            response.put("status", "error");
            response.put("message", ex.getMessage());
        }

        return response;
    }
    /** Displays the form to set a password on a note. Owner-only. */
    @GetMapping("/protect/{id}")
    public String showProtectForm(@PathVariable Integer id, Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        noteService.getNote(id, user); // owner-only check, throws if not owner
        model.addAttribute("noteId", id);
        return "protect-note";
    }

    /** Processes setting a note's password. Owner-only. */
    @PostMapping("/protect/{id}")
    public String processProtect(@PathVariable Integer id,
                                 @RequestParam String password,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);

        if (!password.equals(confirmPassword)) {
            model.addAttribute("noteId", id);
            model.addAttribute("error", "Passwords do not match.");
            return "protect-note";
        }

        if (!PasswordValidator.isStrong(password)) {
            model.addAttribute("noteId", id);
            model.addAttribute("error", PasswordValidator.REQUIREMENTS_MESSAGE);
            return "protect-note";
        }

        noteService.protectNote(id, password, user);
        return "redirect:/notes/" + id;
    }

    /** Removes a note's password protection. Owner-only, requires the current password. */
    @PostMapping("/unprotect/{id}")
    public String processUnprotect(@PathVariable Integer id,
                                   @RequestParam String password,
                                   Authentication authentication, HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(authentication);

        try {
            noteService.removeProtection(id, password, user);
            getUnlockedSet(session).remove(id);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/notes/" + id;
    }

    /** Displays the "enter password" screen for a locked note. */
    @GetMapping("/unlock/{id}")
    public String showUnlockForm(@PathVariable Integer id, Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        noteService.getAccessibleNote(id, user); // confirms access before showing the unlock form at all
        model.addAttribute("noteId", id);
        return "unlock-note";
    }

    /** Processes the unlock password submission. */
    @PostMapping("/unlock/{id}")
    public String processUnlock(@PathVariable Integer id,
                                @RequestParam String password,
                                Authentication authentication, HttpSession session, Model model) {
        User user = getCurrentUser(authentication);

        if (!noteService.verifyNotePassword(id, password, user)) {
            model.addAttribute("noteId", id);
            model.addAttribute("error", "Incorrect password.");
            return "unlock-note";
        }

        getUnlockedSet(session).add(id);
        return "redirect:/notes/" + id;
    }
}