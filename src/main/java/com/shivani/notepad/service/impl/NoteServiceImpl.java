package com.shivani.notepad.service.impl;

import com.shivani.notepad.dto.NoteDto;
import com.shivani.notepad.entity.*;
import com.shivani.notepad.exception.NoteNotFoundException;
import com.shivani.notepad.repository.*;
import com.shivani.notepad.service.NoteService;
import com.shivani.notepad.dto.NoteSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of NoteService.
 *
 * Every method that touches an existing note first verifies ownership
 * via NoteRepository.findByIdAndUser() — this is the core defense
 * against IDOR (Insecure Direct Object Reference) vulnerabilities,
 * ensuring User A can never read, edit, or delete User B's notes simply
 * by guessing/changing a note ID in the URL.
 *
 * NOTE: This class currently depends directly on CategoryRepository
 * rather than CategoryService, since CategoryService doesn't exist yet
 * (built in Step 10). Once it exists, we could optionally refactor this
 * to go through CategoryService instead for stricter layering — but
 * reading a Category by ID is simple enough that using the repository
 * directly here is a reasonable, common pattern too.
 */
@Service
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final NoteShareRepository noteShareRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final ReminderRepository reminderRepository;
    private final TodoRepository todoRepository;

    @Autowired
    public NoteServiceImpl(NoteRepository noteRepository, CategoryRepository categoryRepository,
                           TagRepository tagRepository, NoteShareRepository noteShareRepository,
                           org.springframework.security.crypto.password.PasswordEncoder passwordEncoder, ReminderRepository reminderRepository, TodoRepository todoRepository) {
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.noteShareRepository = noteShareRepository;
        this.passwordEncoder = passwordEncoder;
        this.reminderRepository = reminderRepository;
        this.todoRepository = todoRepository;
    }

    @Override
    public Note createNote(NoteDto dto, User owner) {
        Note note = new Note();
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setUser(owner);

        applyCategoryIfPresent(dto, note);
        note.setTags(resolveTags(dto.getTagsInput(), owner));

        return noteRepository.save(note);
    }

    @Override
    public Note updateNote(NoteDto dto, User requestingUser) {
        // getEditableNote allows either the owner OR a shared EDIT user —
        // ownership of the note itself never changes here.
        Note note = getEditableNote(dto.getId(), requestingUser);
        User owner = note.getUser();

        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());

        // Category and tags remain owner-managed: a shared editor can
        // change what a note says, but not how the OWNER organizes it in
        // their own categories/tags. Only the owner can touch these.
        if (requestingUser.getId().equals(owner.getId())) {
            applyCategoryIfPresent(dto, note);
            note.setTags(resolveTags(dto.getTagsInput(), owner));
        }

        return noteRepository.save(note);
    }

    @Override
    public void deleteNote(Integer noteId, User owner) {
        Note note = getNote(noteId, owner); // ownership check
        noteRepository.delete(note);
    }

    @Override
    public List<Note> getAllNotes(User owner) {
        // Only ACTIVE notes appear in the main list — trashed/archived
        // notes are surfaced through their own dedicated views instead.
        return noteRepository.findByUserAndStatus(owner, NoteStatus.ACTIVE).stream()
                .sorted(Comparator
                        // Pinned notes first (true sorts before false when reversed)...
                        .comparing(Note::isPinned).reversed()
                        // ...then by most recently updated within each group.
                        .thenComparing(Comparator.comparing(Note::getUpdatedAt).reversed()))
                .collect(Collectors.toList());
    }

    @Override
    public Note getNote(Integer noteId, User owner) {
        return noteRepository.findByIdAndUser(noteId, owner)
                .orElseThrow(() -> new NoteNotFoundException(
                        "Note not found or you do not have access to it: " + noteId));
    }

    @Override
    public List<Note> searchNotes(String keyword, User owner) {
        return noteRepository.searchByUserAndKeyword(owner.getId(), keyword);
    }

    /**
     * Applies the selected category to a note, if one was chosen in the
     * form. A null/blank categoryId means "uncategorized" and is valid.
     */
    private void applyCategoryIfPresent(NoteDto dto, Note note) {
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElse(null); // silently ignore an invalid category ID rather than failing the whole save
            note.setCategory(category);
        } else {
            note.setCategory(null);
        }
    }
    /**
     * Parses a comma-separated string of tag names into a Set of Tag
     * entities, creating any tags that don't already exist for this user.
     * This "find or create" pattern is what allows users to type free-text
     * tags without needing a separate tag management page.
     *
     * @param tagsInput comma-separated tag names (e.g., "java, spring, interview")
     * @param owner the current user, used to scope tag lookup/creation
     * @return a Set of resolved Tag entities ready to attach to a Note
     */
    private Set<Tag> resolveTags(String tagsInput, User owner) {
        Set<Tag> resolvedTags = new HashSet<>();

        if (tagsInput == null || tagsInput.isBlank()) {
            return resolvedTags; // no tags entered — valid, empty set
        }

        // Split on commas, trim whitespace, drop empty entries (handles
        // trailing commas or double commas gracefully), and de-duplicate
        // case-insensitively so "Java" and "java" aren't treated as two
        // different tags.
        String[] rawNames = tagsInput.split(",");

        for (String rawName : rawNames) {
            String trimmedName = rawName.trim();
            if (trimmedName.isEmpty()) {
                continue;
            }

            Tag tag = tagRepository.findByNameIgnoreCaseAndUser(trimmedName, owner)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(trimmedName);
                        newTag.setUser(owner);
                        return tagRepository.save(newTag);
                    });

            resolvedTags.add(tag);
        }

        return resolvedTags;
    }
    @Override
    public Note togglePin(Integer noteId, User owner) {
        Note note = getNote(noteId, owner);
        note.setPinned(!note.isPinned());
        return noteRepository.save(note);
    }

    @Override
    public Note toggleFavorite(Integer noteId, User owner) {
        Note note = getNote(noteId, owner);
        note.setFavorite(!note.isFavorite());
        return noteRepository.save(note);
    }

    @Override
    public void moveToTrash(Integer noteId, User owner) {
        Note note = getNote(noteId, owner);
        note.setStatus(NoteStatus.TRASHED);
        noteRepository.save(note);
    }

    @Override
    public Note restoreNote(Integer noteId, User owner) {
        // NOTE: getNote() currently only looks up ACTIVE-scoped notes indirectly
        // via findByIdAndUser (no status filter there), so this still works for
        // trashed/archived notes — see note below on findByIdAndUser.
        Note note = getNote(noteId, owner);
        note.setStatus(NoteStatus.ACTIVE);
        return noteRepository.save(note);
    }

    @Override
    public void deletePermanently(Integer noteId, User owner) {
        Note note = getNote(noteId, owner);

        noteShareRepository.deleteAll(noteShareRepository.findByNote(note));

        for (Reminder reminder : reminderRepository.findByNote(note)) {
            reminder.setNote(null);
            reminderRepository.save(reminder);
        }

        todoRepository.deleteAll(todoRepository.findByNote(note));

        noteRepository.delete(note);
    }

    @Override
    public void archiveNote(Integer noteId, User owner) {
        Note note = getNote(noteId, owner);
        note.setStatus(NoteStatus.ARCHIVED);
        noteRepository.save(note);
    }

    @Override
    public List<Note> getTrashedNotes(User owner) {
        return noteRepository.findByUserAndStatus(owner, NoteStatus.TRASHED).stream()
                .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> getArchivedNotes(User owner) {
        return noteRepository.findByUserAndStatus(owner, NoteStatus.ARCHIVED).stream()
                .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> getFavoriteNotes(User owner) {
        return noteRepository.findByUserAndStatus(owner, NoteStatus.ACTIVE).stream()
                .filter(Note::isFavorite)
                .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }
    @Override
    public List<Note> advancedSearch(NoteSearchCriteria criteria, User owner) {
        // Blank strings are treated the same as null — an empty form field
        // should mean "don't filter on this," not "match empty string."
        String keyword = (criteria.getKeyword() != null && !criteria.getKeyword().isBlank())
                ? criteria.getKeyword() : null;
        String tagName = (criteria.getTagName() != null && !criteria.getTagName().isBlank())
                ? criteria.getTagName() : null;

        // HTML <input type="date"> submits "yyyy-MM-dd" — parse to the
        // start/end of that day so the date range is inclusive.
        LocalDateTime startDate = (criteria.getStartDate() != null && !criteria.getStartDate().isBlank())
                ? LocalDate.parse(criteria.getStartDate()).atStartOfDay()
                : null;
        LocalDateTime endDate = (criteria.getEndDate() != null && !criteria.getEndDate().isBlank())
                ? LocalDate.parse(criteria.getEndDate()).atTime(23, 59, 59)
                : null;

        return noteRepository.advancedSearch(
                owner.getId(), keyword, criteria.getCategoryId(), tagName, startDate, endDate);
    }
    @Override
    public Note getAccessibleNote(Integer noteId, User user) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found: " + noteId));

        boolean isOwner = note.getUser().getId().equals(user.getId());
        boolean hasShare = isOwner || noteShareRepository.findByNoteAndSharedWithUser(note, user).isPresent();

        if (!isOwner && !hasShare) {
            throw new NoteNotFoundException("Note not found or you do not have access to it: " + noteId);
        }
        return note;
    }

    @Override
    public Note getEditableNote(Integer noteId, User user) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found: " + noteId));

        if (!canEdit(note, user)) {
            throw new NoteNotFoundException("Note not found or you do not have edit access: " + noteId);
        }
        return note;
    }

    @Override
    public boolean canEdit(Note note, User user) {
        if (note.getUser().getId().equals(user.getId())) {
            return true;
        }
        return noteShareRepository.findByNoteAndSharedWithUser(note, user)
                .map(share -> share.getPermission() == SharePermission.EDIT)
                .orElse(false);
    }

    @Override
    public void protectNote(Integer noteId, String rawPassword, User owner) {
        Note note = getNote(noteId, owner); // owner-only, existing method
        note.setPasswordProtected(true);
        note.setNotePasswordHash(passwordEncoder.encode(rawPassword));
        noteRepository.save(note);
    }

    @Override
    public void removeProtection(Integer noteId, String rawPassword, User owner) {
        Note note = getNote(noteId, owner); // owner-only

        if (!note.isPasswordProtected() || note.getNotePasswordHash() == null
                || !passwordEncoder.matches(rawPassword, note.getNotePasswordHash())) {
            throw new IllegalArgumentException("Incorrect password.");
        }

        note.setPasswordProtected(false);
        note.setNotePasswordHash(null);
        noteRepository.save(note);
    }

    @Override
    public boolean verifyNotePassword(Integer noteId, String rawPassword, User user) {
        Note note = getAccessibleNote(noteId, user); // owner or any share level can attempt to unlock
        if (!note.isPasswordProtected() || note.getNotePasswordHash() == null) {
            return true; // nothing to verify
        }
        return passwordEncoder.matches(rawPassword, note.getNotePasswordHash());
    }
}