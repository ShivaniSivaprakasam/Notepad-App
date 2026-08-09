package com.shivani.notepad.entity;

/**
 * How often a reminder recurs. NONE means a one-time reminder.
 *
 * NOTE: recurrence generation itself (creating the next occurrence after
 * one fires) is intentionally simple in this implementation — see the
 * comment in ReminderServiceImpl for the honest scope of what's covered.
 */
public enum RepeatType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}