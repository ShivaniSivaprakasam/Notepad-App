package com.shivani.notepad.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;/**
 * Represents a user-defined category used to organize notes
 * (e.g., "Work", "Personal", "Ideas").
 *
 * Each Category belongs to exactly one User — categories are not
 * shared across users, so two different users can each have their
 * own "Work" category without conflict.
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String name;
    /**
     * Many categories belong to one user. LAZY fetch for the same reason
     * as in Note — we don't want to load the full User object every time
     * we just need category data.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
