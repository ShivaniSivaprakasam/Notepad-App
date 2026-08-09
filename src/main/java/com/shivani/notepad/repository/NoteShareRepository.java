package com.shivani.notepad.repository;

import com.shivani.notepad.entity.Note;
import com.shivani.notepad.entity.NoteShare;
import com.shivani.notepad.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface NoteShareRepository extends JpaRepository<NoteShare, Integer>{
    List<NoteShare> findByNote(Note note);
    Optional<NoteShare> findByNoteAndSharedWithUser(Note note, User user);
    /** Powers the "Shared with Me" view — every share granted to this user, across all owners. */
    List<NoteShare> findBySharedWithUser(User user);
    /** Every share granted on notes owned by this user — i.e., what THEY have shared with others. */
    List<NoteShare> findByNote_User(User user);
}
