package com.shivani.notepad.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a scheduled reminder/event, shown on the calendar dashboard
 * and optionally linked to a specific Note.
 *
 * date and time are stored as separate columns (rather than a single
 * LocalDateTime) to match typical calendar-app UX, where a user picks a
 * date and a time independently, and so date-only queries (e.g., "all
 * reminders on this calendar day") don't need to worry about time-of-day
 * boundaries.
 *
 * Uses explicit equals/hashCode on id only (not Lombok @Data) for the
 * same reason established with Note and Tag — avoiding relationship
 * fields in hashCode prevents subtle Hibernate lazy-loading bugs.
 */
@Entity
@Table(name = "reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"user", "note"})
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepeatType repeatType = RepeatType.NONE;

    /**
     * Tracks whether the email notification for this reminder has already
     * been sent, so the scheduled job doesn't send duplicate emails every
     * time it runs. This is independent of whether the in-app notification
     * digest has shown it, since in-app notifications are recomputed fresh
     * on every dashboard load rather than tracked with a persisted flag.
     */
    @Column(nullable = false)
    private boolean notificationSent = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Optional link to a Note — a reminder can exist standalone (e.g.,
     * "Doctor appointment") or be attached to a specific note (e.g.,
     * "Follow up on Sprint Planning notes").
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = true)
    private Note note;
}
