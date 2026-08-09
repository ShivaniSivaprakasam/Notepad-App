package com.shivani.notepad.repository;

import com.shivani.notepad.entity.Note;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.entity.NoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
/**
 * Repository for Note entities.
 *
 * Spring Data JPA generates the implementation for these methods
 * automatically based on their method names (derived query methods) —
 * no manual SQL or JPQL required for these simple cases.
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Integer> {
    /**
     * Retrieves all notes belonging to a specific user.
     * Used to populate the dashboard/notes list view.
     */
    List<Note> findByUser(User user);
    /**
     * Retrieves a single note by its ID, scoped to the owning user.
     * Using this (rather than plain findById) prevents one user from
     * accessing another user's note by simply guessing/changing the ID
     * in the URL — a common security oversight (IDOR vulnerability).
     */
    Optional<Note> findByIdAndUser(Integer id, User user);
    /**
     * Native SQL query (rather than JPQL) is required here specifically
     * because Hibernate 7's JPQL validator rejects applying LOWER() to
     * a CLOB/LONGTEXT-mapped field (Note.content) at query-validation
     * time, even though the underlying database (MySQL) supports this
     * operation natively without any issue. Falling back to native SQL
     * bypasses Hibernate's JPQL-level type checking for this one query.
     */
    @Query(value = "SELECT * FROM notes n WHERE n.user_id = :userId AND n.status = 'ACTIVE' AND " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))",
            nativeQuery = true)
    List<Note> searchByUserAndKeyword(@Param("userId") Integer userId, @Param("keyword") String keyword);
    // NOTE: findByCategory() and findByUserAndCategory() will be added
    // in Step 7 once the Category entity and its relationship to Note exist.
    /**
     * Retrieves all notes with a given status for a user — used to power
     * the main list (ACTIVE), Trash view (TRASHED), and Archive view
     * (ARCHIVED) from a single repository method.
     */
    List<Note> findByUserAndStatus(User user, NoteStatus status);
    /**
     * Flexible advanced search — every parameter is optional (pass null
     * to skip that filter). Combines keyword, category, tag, and date
     * range filtering in a single query rather than building separate
     * methods for every possible combination of filters.
     *
     * Native SQL is used here for the same reason as searchByUserAndKeyword
     * above — Hibernate 7's JPQL validator rejects LOWER() on the
     * CLOB-mapped content column. The tag filter uses a subquery against
     * the note_tags join table rather than a JOIN, since a note can have
     * multiple tags and a JOIN would risk duplicate rows.
     */
    @Query(value = "SELECT * FROM notes n WHERE n.user_id = :userId AND n.status = 'ACTIVE' " +
            "AND (:keyword IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR n.category_id = :categoryId) " +
            "AND (:tagName IS NULL OR n.id IN (" +
            "     SELECT nt.note_id FROM note_tags nt JOIN tags t ON t.id = nt.tag_id " +
            "     WHERE t.user_id = :userId AND LOWER(t.name) = LOWER(:tagName))) " +
            "AND (:startDate IS NULL OR n.created_at >= :startDate) " +
            "AND (:endDate IS NULL OR n.created_at <= :endDate) " +
            "ORDER BY n.updated_at DESC",
            nativeQuery = true)
    List<Note> advancedSearch(
            @Param("userId") Integer userId,
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("tagName") String tagName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    /** Counts all notes ever created by a user, across all statuses. */
    long countByUser(User user);

    /** Counts only ACTIVE notes — used by the Home dashboard so trashed
     notes never inflate the "Notes Created" figure. */
    long countByUserAndStatus(User user, NoteStatus status);

}
