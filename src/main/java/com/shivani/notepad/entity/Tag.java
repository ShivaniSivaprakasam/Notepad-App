package com.shivani.notepad.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user-defined tag used to label notes.
 *
 * IMPORTANT: equals()/hashCode() are based ONLY on the entity id, and the
 * `notes` collection is excluded from toString() as well. Including
 * relationship collections in equals()/hashCode() (as Lombok's @Data would
 * do by default) causes Hibernate to lazily initialize those collections
 * whenever this entity is placed in a HashSet/HashMap — which can trigger
 * ConcurrentModificationException when it happens during another
 * collection's own loading process (exactly what was happening here).
 */
@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "notes")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(mappedBy = "tags")
    private Set<Note> notes = new HashSet<>();
}