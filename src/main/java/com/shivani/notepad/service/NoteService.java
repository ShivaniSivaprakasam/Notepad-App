package com.shivani.notepad.service;

import com.shivani.notepad.dto.NoteDto;
import com.shivani.notepad.dto.NoteSearchCriteria;
import com.shivani.notepad.entity.Note;
import com.shivani.notepad.entity.User;
import java.util.List;
/**
 * Business logic contract for note management: creation, editing,
 * deletion, retrieval, and search — all scoped to the owning user.
 */
public interface NoteService {
    /** Creates a new note owned by the given user. */
    Note createNote(NoteDto noteDto, User owner);
    /** Updates an existing note, verifying ownership before applying changes. */
    Note updateNote(NoteDto noteDto, User owner);
    /** Deletes a note, verifying ownership first. */
    void deleteNote(Integer noteId, User owner);
    /** Retrieves all notes belonging to the given user, newest first. */
    List<Note> getAllNotes(User owner);
    /** Retrieves a single note by ID, scoped to the owning user. */
    Note getNote(Integer noteId, User owner);
    /** Searches the given user's notes by title or content keyword. */
    List<Note> searchNotes(String keyword, User owner);
    /** Toggles a note's pinned state. */
    Note togglePin(Integer noteId, User owner);

    /** Toggles a note's favorite state. */
    Note toggleFavorite(Integer noteId, User owner);

    /** Moves a note to the trash (soft delete). */
    void moveToTrash(Integer noteId, User owner);

    /** Restores a trashed or archived note back to active. */
    Note restoreNote(Integer noteId, User owner);

    /** Permanently deletes a trashed note — irreversible. */
    void deletePermanently(Integer noteId, User owner);

    /** Moves a note to the archive. */
    void archiveNote(Integer noteId, User owner);

    /** Retrieves all trashed notes for a user. */
    List<Note> getTrashedNotes(User owner);

    /** Retrieves all archived notes for a user. */
    List<Note> getArchivedNotes(User owner);

    /** Retrieves only the user's favorited (and active) notes. */
    List<Note> getFavoriteNotes(User owner);

    /** Performs a multi-filter advanced search, scoped to the owning user. */
    List<Note> advancedSearch(NoteSearchCriteria criteria, User owner);

    /**
     * Fetches a note if the given user can VIEW it — either as owner or
     * via any NoteShare (VIEW or EDIT). Throws NoteNotFoundException
     * otherwise, deliberately not distinguishing "doesn't exist" from
     * "not shared with you," consistent with the IDOR-prevention pattern
     * used throughout this app.
     */
    Note getAccessibleNote(Integer noteId, User user);

    /** Same as getAccessibleNote, but requires owner or an EDIT-level share. */
    Note getEditableNote(Integer noteId, User user);

    /** Convenience check used by templates to conditionally show Edit controls. */
    boolean canEdit(Note note, User user);

    /** Password-protects a note. Owner-only action. */
    void protectNote(Integer noteId, String rawPassword, User owner);

    /** Removes password protection, requiring the current password as confirmation. Owner-only. */
    void removeProtection(Integer noteId, String rawPassword, User owner);

    /** Verifies a submitted password against a note's stored hash. */
    boolean verifyNotePassword(Integer noteId, String rawPassword, User user);

}
