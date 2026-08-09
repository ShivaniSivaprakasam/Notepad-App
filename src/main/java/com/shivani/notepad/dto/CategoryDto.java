package com.shivani.notepad.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
/**
 * DTO for creating/editing a category via the Thymeleaf form.
 */
@Data
public class CategoryDto {
    private Integer id; // null when creating, populated when editing
    @NotBlank(message = "Category name is required")
    private String name;
}
