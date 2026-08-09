package com.shivani.notepad.controller;

import com.shivani.notepad.dto.ReminderDto;
import com.shivani.notepad.entity.NoteStatus;
import com.shivani.notepad.entity.Reminder;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.repository.NoteRepository;
import com.shivani.notepad.service.ReminderService;
import com.shivani.notepad.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reminders")
public class ReminderController {

    private final ReminderService reminderService;
    private final UserService userService;
    private final NoteRepository noteRepository;

    @Autowired
    public ReminderController(ReminderService reminderService, UserService userService,
                              NoteRepository noteRepository) {
        this.reminderService = reminderService;
        this.userService = userService;
        this.noteRepository = noteRepository;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
    }

    private User getCurrentUser(Authentication authentication) {
        return userService.getUserByUsername(authentication.getName());
    }

    /** Only ACTIVE notes are offered for linking — a trashed or archived
     note shouldn't be selectable as if it were still a normal note. */
    private List<com.shivani.notepad.entity.Note> getLinkableNotes(User user) {
        return noteRepository.findByUserAndStatus(user, NoteStatus.ACTIVE);
    }

    @GetMapping("/calendar")
    public String showCalendar(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month,
                               Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        YearMonth ym = (year != null && month != null) ? YearMonth.of(year, month) : YearMonth.now();

        List<Reminder> monthReminders = reminderService.getRemindersForMonth(ym, user);
        Map<Integer, List<Reminder>> remindersByDay = monthReminders.stream()
                .collect(Collectors.groupingBy(r -> r.getDate().getDayOfMonth()));

        model.addAttribute("yearMonth", ym);
        model.addAttribute("monthLabel", ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + ym.getYear());
        model.addAttribute("remindersByDay", remindersByDay);
        model.addAttribute("daysInMonth", ym.lengthOfMonth());
        model.addAttribute("firstDayOffset", ym.atDay(1).getDayOfWeek().getValue() % 7);
        model.addAttribute("prevYear", ym.minusMonths(1).getYear());
        model.addAttribute("prevMonth", ym.minusMonths(1).getMonthValue());
        model.addAttribute("nextYear", ym.plusMonths(1).getYear());
        model.addAttribute("nextMonth", ym.plusMonths(1).getMonthValue());
        model.addAttribute("today", LocalDate.now());
        return "calendar";
    }

    @GetMapping("/day/{date}")
    public String showDay(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                          Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("date", date);
        model.addAttribute("reminders", reminderService.getRemindersForDate(date, user));
        return "reminder-day";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(required = false) String date,
                                 Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        ReminderDto dto = new ReminderDto();
        if (date != null) dto.setDate(date);
        model.addAttribute("reminder", dto);
        model.addAttribute("notes", getLinkableNotes(user));
        return "reminder-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        Reminder reminder = reminderService.getReminder(id, user);

        ReminderDto dto = new ReminderDto();
        dto.setId(reminder.getId());
        dto.setTitle(reminder.getTitle());
        dto.setDescription(reminder.getDescription());
        dto.setDate(reminder.getDate().toString());
        dto.setTime(reminder.getTime().toString());
        dto.setRepeatType(reminder.getRepeatType());

        // If this reminder is currently linked to a note that is no
        // longer ACTIVE (trashed/archived after the link was made),
        // the link is dropped from the edit form rather than shown as
        // if it were still a valid, selectable option.
        List<com.shivani.notepad.entity.Note> linkable = getLinkableNotes(user);
        if (reminder.getNote() != null && linkable.stream().anyMatch(n -> n.getId().equals(reminder.getNote().getId()))) {
            dto.setNoteId(reminder.getNote().getId());
        }

        model.addAttribute("reminder", dto);
        model.addAttribute("notes", linkable);
        return "reminder-form";
    }

    @PostMapping
    public String saveReminder(@Valid @ModelAttribute("reminder") ReminderDto dto,
                               BindingResult result, Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);

        if (!result.hasErrors()) {
            try {
                LocalDate reminderDate = LocalDate.parse(dto.getDate());
                LocalTime reminderTime = LocalTime.parse(dto.getTime());
                LocalDate today = LocalDate.now();

                if (reminderDate.isBefore(today)) {
                    result.reject("pastDate", "Reminder date cannot be in the past.");
                } else if (reminderDate.isEqual(today) && reminderTime.isBefore(LocalTime.now())) {
                    result.reject("pastTime", "Reminder time cannot be in the past for today's date.");
                }
            } catch (Exception parseEx) {
                result.reject("invalidDateTime", "Please provide a valid date and time.");
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("notes", getLinkableNotes(user));
            model.addAttribute("dateError", result.getGlobalError() != null ? result.getGlobalError().getDefaultMessage() : null);
            return "reminder-form";
        }

        Reminder saved = (dto.getId() != null)
                ? reminderService.updateReminder(dto, user)
                : reminderService.createReminder(dto, user);

        return "redirect:/reminders/calendar?year=" + saved.getDate().getYear()
                + "&month=" + saved.getDate().getMonthValue();
    }

    @GetMapping("/delete/{id}")
    public String deleteReminder(@PathVariable Integer id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Reminder reminder = reminderService.getReminder(id, user);
        int year = reminder.getDate().getYear();
        int month = reminder.getDate().getMonthValue();
        reminderService.deleteReminder(id, user);
        return "redirect:/reminders/calendar?year=" + year + "&month=" + month;
    }

    @GetMapping("/grouped")
    public String showGrouped(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        List<Reminder> all = reminderService.getAllReminders(user);

        model.addAttribute("daily", all.stream().filter(r -> r.getRepeatType() == com.shivani.notepad.entity.RepeatType.DAILY).collect(Collectors.toList()));
        model.addAttribute("weekly", all.stream().filter(r -> r.getRepeatType() == com.shivani.notepad.entity.RepeatType.WEEKLY).collect(Collectors.toList()));
        model.addAttribute("monthly", all.stream().filter(r -> r.getRepeatType() == com.shivani.notepad.entity.RepeatType.MONTHLY).collect(Collectors.toList()));
        model.addAttribute("oneTime", all.stream().filter(r ->
                r.getRepeatType() == com.shivani.notepad.entity.RepeatType.NONE
                        || r.getRepeatType() == com.shivani.notepad.entity.RepeatType.YEARLY).collect(Collectors.toList()));
        return "reminders-grouped";
    }

    @GetMapping("/api/today")
    @ResponseBody
    public List<Map<String, Object>> getTodaysRemindersJson(Authentication authentication) {
        User user = getCurrentUser(authentication);
        LocalTime now = LocalTime.now();
        return reminderService.getTodaysReminders(user).stream()
                .map(r -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", r.getId());
                    map.put("title", r.getTitle());
                    map.put("time", r.getTime().toString());
                    map.put("isPast", r.getTime().isBefore(now));
                    return map;
                })
                .collect(Collectors.toList());
    }
}