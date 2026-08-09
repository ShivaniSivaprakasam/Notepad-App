package com.shivani.notepad.dto;

import com.shivani.notepad.entity.SharePermission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoteShareDto {
    @NotBlank(message = "An email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    private SharePermission permission = SharePermission.VIEW;
}
