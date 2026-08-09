package com.shivani.notepad.repository;

import com.shivani.notepad.entity.Note;
import com.shivani.notepad.entity.Todo;
import com.shivani.notepad.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Integer> {

    List<Todo> findByNote(Note note);

    List<Todo> findByNoteAndCompleted(Note note, boolean completed);

    /** Every todo owned by a user, EXCLUDING todos attached to a TRASHED
     note. Standalone todos (note IS NULL) are always included. */
    @Query("SELECT t FROM Todo t WHERE t.user = :user " +
            "AND (t.note IS NULL OR t.note.status <> 'TRASHED') " +
            "ORDER BY t.completed ASC, t.id DESC")
    List<Todo> findAllByUser(@Param("user") User user);

    @Query("SELECT t FROM Todo t WHERE t.user = :user AND t.completed = false " +
            "AND (t.note IS NULL OR t.note.status <> 'TRASHED')")
    List<Todo> findIncompleteByUser(@Param("user") User user);

    /** Dashboard stat: total task count, excluding tasks attached to a
     trashed note — matches what the user actually sees on /todos. */
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.user = :user " +
            "AND (t.note IS NULL OR t.note.status <> 'TRASHED')")
    long countVisibleByUser(@Param("user") User user);

    /** Same visibility rule, restricted to completed tasks. */
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.user = :user AND t.completed = :completed " +
            "AND (t.note IS NULL OR t.note.status <> 'TRASHED')")
    long countVisibleByUserAndCompleted(@Param("user") User user, @Param("completed") boolean completed);

    // Kept for anything else in the codebase still referencing the
    // unfiltered counts (e.g. account-deletion cleanup, which correctly
    // wants ALL todos regardless of note status).
    long countByUser(User user);
    long countByUserAndCompleted(User user, boolean completed);

    boolean existsByUserAndNoteAndTaskIgnoreCase(User owner, Note note, String taskText);

    boolean existsByUserAndNoteIsNullAndTaskIgnoreCase(User owner, String taskText);
}