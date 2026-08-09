package com.shivani.notepad.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.shivani.notepad.entity.NoteStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"tags", "todos"})
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Todo> todos = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "note_tags",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    /**
     * Pinned notes are shown at the top of the notes list, regardless of
     * their last-updated timestamp. A simple boolean flag rather than a
     * separate ordering field — sufficient for this app's scale.
     */
    @Column(nullable = false)
    private boolean pinned = false;
    /**
     * Marks a note as a favorite, allowing the user to filter the notes
     * list down to only favorited notes.
     */
    @Column(nullable = false)
    private boolean favorite = false;
    /**
     * Tracks whether a note is active, trashed, or archived. Using a
     * single enum for both Trash and Archive (rather than two separate
     * boolean flags or two separate tables) reflects that both features
     * are the same underlying mechanism — "hide this note from the main
     * view, but keep it recoverable" — just surfaced as two different
     * UI sections to the user.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoteStatus status = NoteStatus.ACTIVE;
    @Column(nullable = false)
    private boolean passwordProtected = false;
    @Column(nullable = true)
    private String notePasswordHash;
}