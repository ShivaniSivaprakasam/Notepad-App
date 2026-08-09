package com.shivani.notepad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Aggregates the small set of numbers shown on the dashboard. Kept as a
 * plain DTO (not an entity) since these are computed, not persisted.
 */
@Data
@AllArgsConstructor
public class DashboardStats {
    private long totalNotesCreated;
    private long totalCategories;
    private long totalTodos;
    private long completedTodos;
    private int productivityScore; // 0-100, rounded percentage of completedTodos/totalTodos
}