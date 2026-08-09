package com.shivani.notepad.service;

import com.shivani.notepad.dto.ReminderDto;
import com.shivani.notepad.entity.Reminder;
import com.shivani.notepad.entity.User;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface ReminderService {

    Reminder createReminder(ReminderDto dto, User owner);

    Reminder updateReminder(ReminderDto dto, User owner);

    void deleteReminder(Integer reminderId, User owner);

    Reminder getReminder(Integer reminderId, User owner);

    /** All reminders in the given month — powers the calendar grid. */
    List<Reminder> getRemindersForMonth(YearMonth month, User owner);

    /** All reminders on one specific date. */
    List<Reminder> getRemindersForDate(LocalDate date, User owner);

    /** Today's reminders for a user — used for the in-app notification digest. */
    List<Reminder> getTodaysReminders(User owner);

    /** All reminders for a user, unfiltered by date — used for the grouped-by-repeat-type view. */
    List<Reminder> getAllReminders(User owner);
}
