package com.shivani.notepad.repository;

import com.shivani.notepad.entity.Tag;
import com.shivani.notepad.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
/**
 * Repository for Tag entities.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Integer>{
    /** Retrieves all tags belonging to a specific user (used for autocomplete/filter UI). */
    List<Tag> findByUser(User user);
    /** Looks up a specific tag by name, scoped to the user — used for find-or-create logic. */
    Optional<Tag> findByNameIgnoreCaseAndUser(String name, User user);
}
