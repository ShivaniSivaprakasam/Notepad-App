package com.shivani.notepad.entity;

/**
 * Represents the lifecycle state of a Note.
 *
 * ACTIVE   - normal, visible in the main notes list.
 * TRASHED  - soft-deleted; hidden from the main list, shown in Trash,
 *            recoverable via "Restore," permanently deletable via
 *            "Delete Forever."
 * ARCHIVED - deliberately hidden by the user to declutter the dashboard,
 *            shown in a separate Archive view, restorable back to ACTIVE.
 *
 * Using EnumType.STRING (not ORDINAL) when persisting this, so the
 * database stores readable values like "TRASHED" rather than fragile
 * integer indexes that would break if this enum's order ever changed.
 */
public enum NoteStatus {
    ACTIVE,
    TRASHED,
    ARCHIVED
}
