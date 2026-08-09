package com.shivani.notepad.service;

import com.shivani.notepad.entity.Category;
import com.shivani.notepad.entity.User;

import java.util.List;

/**
 * Category management is now read-only from the user's perspective —
 * every account is seeded with a fixed set of default categories, and
 * there is no user-facing create/edit/delete flow for categories
 * anymore (per product decision to remove custom categories).
 */
public interface CategoryService {

    /**
     * Retrieves a user's categories, auto-seeding the default set if
     * the user currently has none (covers accounts created before
     * seeding existed).
     */
    List<Category> getCategories(User owner);

    /** Retrieves a single category by ID, scoped to the owning user. */
    Category findCategory(Integer categoryId, User owner);
}