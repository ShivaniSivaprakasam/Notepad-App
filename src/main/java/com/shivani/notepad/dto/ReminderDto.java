package com.shivani.notepad.dto;

import com.shivani.notepad.entity.RepeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReminderDto {
    private Integer id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Date is required")
    private String date; // bound from <input type="date">, parsed in the service

    @NotNull(message = "Time is required")
    private String time; // bound from <input type="time">, parsed in the service

    private RepeatType repeatType = RepeatType.NONE;

    private Integer noteId; // optional link to a note
}