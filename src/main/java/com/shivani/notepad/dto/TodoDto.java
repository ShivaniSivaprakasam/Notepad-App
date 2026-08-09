package com.shivani.notepad.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TodoDto {
    private Integer id;
    private Integer noteId; // optional now
    @NotBlank(message = "Task description is required")
    private String task;
    private boolean completed;
}