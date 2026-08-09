package com.shivani.notepad.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
/**
 * Data Transfer Object used for creating and editing notes via the
 * Thymeleaf form. Keeps the form binding decoupled from the Note entity
 * itself (e.g., we don't want createdAt/updatedAt/user bound from a form).
 */
@Data
public class NoteDto {
    private Integer id; // null when creating, populated when editing
    @NotBlank(message = "Title is required")
    private String title;
    private String content; // can be blank initially (empty note)
    /**
     * Nullable — a note can be created without a category ("uncategorized").
     * Holds the selected category's ID from the dropdown in the form.
     */
    private Integer categoryId;
    /**
     * Comma-separated tag names as typed by the user (e.g., "java, spring, interview").
     * Parsed and converted into Tag entities (finding existing ones or
     * creating new ones) in NoteServiceImpl. Kept as a raw String here
     * rather than a List/Set, since HTML forms naturally submit this as
     * plain text from a single input field.
     */
    private String tagsInput;
}
