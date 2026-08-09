package com.shivani.notepad.service.impl;

import com.shivani.notepad.dto.ReminderDto;
import com.shivani.notepad.entity.Note;
import com.shivani.notepad.entity.Reminder;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.exception.ReminderNotFoundException;
import com.shivani.notepad.repository.NoteRepository;
import com.shivani.notepad.repository.ReminderRepository;
import com.shivani.notepad.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Implementation of ReminderService.
 *
 * SCOPE NOTE on recurrence (repeatType): this implementation stores the
 * repeatType on each Reminder and displays it in the UI, but does NOT
 * automatically generate future occurrences (e.g., a WEEKLY reminder
 * does not spawn a new row every week). Implementing true recurring-event
 * generation (with edit/delete semantics for "this occurrence" vs "all
 * future occurrences") is a substantially larger feature — the kind
 * calendar apps like Google Calendar dedicate significant engineering to.
 * Being upfront: if you want real recurrence, that's a good "Future
 * Improvements" item for your README, not something to claim as fully
 * working right now.
 */
@Service
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository reminderRepository;
    private final NoteRepository noteRepository;

    @Autowired
    public ReminderServiceImpl(ReminderRepository reminderRepository, NoteRepository noteRepository) {
        this.reminderRepository = reminderRepository;
        this.noteRepository = noteRepository;
    }

    @Override
    public Reminder createReminder(ReminderDto dto, User owner) {
        Reminder reminder = new Reminder();
        applyDtoToEntity(dto, reminder, owner);
        return reminderRepository.save(reminder);
    }

    @Override
    public Reminder updateReminder(ReminderDto dto, User owner) {
        Reminder reminder = getReminder(dto.getId(), owner);
        applyDtoToEntity(dto, reminder, owner);
        // Editing a reminder's date/time should let it notify again if it
        // was already sent — otherwise correcting a mistaken time would
        // silently never notify the user.
        reminder.setNotificationSent(false);
        return reminderRepository.save(reminder);
    }

    @Override
    public void deleteReminder(Integer reminderId, User owner) {
        Reminder reminder = getReminder(reminderId, owner);
        reminderRepository.delete(reminder);
    }

    @Override
    public Reminder getReminder(Integer reminderId, User owner) {
        return reminderRepository.findByIdAndUser(reminderId, owner)
                .orElseThrow(() -> new ReminderNotFoundException(
                        "Reminder not found or you do not have access to it: " + reminderId));
    }

    @Override
    public List<Reminder> getRemindersForMonth(YearMonth month, User owner) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return reminderRepository.findByUserAndDateBetweenOrderByDateAscTimeAsc(owner, start, end);
    }

    @Override
    public List<Reminder> getRemindersForDate(LocalDate date, User owner) {
        return reminderRepository.findByUserAndDateOrderByTimeAsc(owner, date);
    }

    @Override
    public List<Reminder> getTodaysReminders(User owner) {
        return reminderRepository.findByUserAndDate(owner, LocalDate.now());
    }

    private void applyDtoToEntity(ReminderDto dto, Reminder reminder, User owner) {
        reminder.setTitle(dto.getTitle());
        reminder.setDescription(dto.getDescription());
        reminder.setDate(LocalDate.parse(dto.getDate()));
        reminder.setTime(LocalTime.parse(dto.getTime()));
        reminder.setRepeatType(dto.getRepeatType());
        reminder.setUser(owner);

        if (dto.getNoteId() != null) {
            Note note = noteRepository.findByIdAndUser(dto.getNoteId(), owner).orElse(null);
            reminder.setNote(note);
        } else {
            reminder.setNote(null);
        }
    }
    @Override
    public List<Reminder> getAllReminders(User owner) {
        return reminderRepository.findByUserOrderByDateAscTimeAsc(owner);
    }
}