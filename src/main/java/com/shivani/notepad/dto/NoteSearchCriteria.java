package com.shivani.notepad.dto;

import lombok.Data;
/**
 * Captures the optional filters for the advanced search form. Every
 * field is nullable/blank-able — an empty form submits all nulls,
 * which the repository query treats as "no filter applied."
 */
@Data
public class NoteSearchCriteria {
    private String keyword;
    private Integer categoryId;
    private String tagName;
    private String startDate; // bound as a raw string from an HTML date input, parsed in the service
    private String endDate;
}
