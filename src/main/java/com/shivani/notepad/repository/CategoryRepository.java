package com.shivani.notepad.repository;

import com.shivani.notepad.entity.Category;
import com.shivani.notepad.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
/**
 * Repository for Category entities.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer>{
    /**
     * Retrieves all categories belonging to a specific user.
     * Used to populate the category dropdown/list on the dashboard.
     */
    List<Category> findByUser(User user);
    /**
     * Retrieves a single category by name, scoped to a specific user.
     * Scoping by user prevents one user's category name lookup from
     * accidentally matching another user's category with the same name.
     */
    Optional<Category> findByNameAndUser(String name, User user);
    /**
     * Checks whether a category with this name already exists for this
     * user — used in CategoryService to enforce "unique name per user"
     * at the application level (see note in Category entity above).
     */
    boolean existsByNameAndUser(String name, User user);
    /** Counts how many categories a user has created. */
    long countByUser(User user);
}
