package com.shivani.notepad.repository;

import com.shivani.notepad.entity.Note;
import com.shivani.notepad.entity.Reminder;
import com.shivani.notepad.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Integer> {

    List<Reminder> findByUserAndDateBetweenOrderByDateAscTimeAsc(User user, LocalDate start, LocalDate end);

    List<Reminder> findByUserAndDateOrderByTimeAsc(User user, LocalDate date);

    Optional<Reminder> findByIdAndUser(Integer id, User user);

    /** JOIN FETCH loads the User in the same query, so the scheduler's
     Hibernate session never needs to lazy-load it later. */
    @Query("SELECT r FROM Reminder r JOIN FETCH r.user WHERE r.date <= :today AND r.notificationSent = false")
    List<Reminder> findDueForEmail(@Param("today") LocalDate today);

    List<Reminder> findByUserAndDate(User user, LocalDate date);

    List<Reminder> findByNote(Note note);

    List<Reminder> findByUser(User user);

    List<Reminder> findByUserOrderByDateAscTimeAsc(User user);
}