package com.shivani.notepad.service.impl;

import com.shivani.notepad.entity.Note;
import com.shivani.notepad.entity.NoteShare;
import com.shivani.notepad.entity.SharePermission;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.exception.NoteNotFoundException;
import com.shivani.notepad.repository.NoteRepository;
import com.shivani.notepad.repository.NoteShareRepository;
import com.shivani.notepad.repository.UserRepository;
import com.shivani.notepad.service.NoteShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteShareServiceImpl implements NoteShareService {

    private final NoteShareRepository noteShareRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Autowired
    public NoteShareServiceImpl(NoteShareRepository noteShareRepository, NoteRepository noteRepository,
                                UserRepository userRepository) {
        this.noteShareRepository = noteShareRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    @Override
    public NoteShare shareNote(Integer noteId, User owner, String recipientEmail, SharePermission permission) {
        Note note = noteRepository.findByIdAndUser(noteId, owner)
                .orElseThrow(() -> new NoteNotFoundException("Note not found: " + noteId));

        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new IllegalArgumentException("No user found with that email address."));

        if (recipient.getId().equals(owner.getId())) {
            throw new IllegalArgumentException("You cannot share a note with yourself.");
        }

        // Re-sharing with someone already on the list updates their
        // permission level, rather than creating a duplicate share row.
        NoteShare share = noteShareRepository.findByNoteAndSharedWithUser(note, recipient)
                .orElseGet(NoteShare::new);

        share.setNote(note);
        share.setSharedWithUser(recipient);
        share.setPermission(permission);

        return noteShareRepository.save(share);
    }

    @Override
    public Integer unshareNote(Integer shareId, User owner) {
        NoteShare share = noteShareRepository.findById(shareId)
                .orElseThrow(() -> new NoteNotFoundException("Share not found: " + shareId));

        // Ownership check: only the note's owner can revoke a share,
        // never the recipient themselves via this endpoint.
        if (!share.getNote().getUser().getId().equals(owner.getId())) {
            throw new NoteNotFoundException("Share not found or you do not have access to it: " + shareId);
        }

        Integer noteId = share.getNote().getId();
        noteShareRepository.delete(share);
        return noteId;
    }

    @Override
    public List<NoteShare> getSharesForNote(Integer noteId, User owner) {
        Note note = noteRepository.findByIdAndUser(noteId, owner)
                .orElseThrow(() -> new NoteNotFoundException("Note not found: " + noteId));
        return noteShareRepository.findByNote(note);
    }

    @Override
    public List<NoteShare> getNotesSharedWithMe(User user) {
        return noteShareRepository.findBySharedWithUser(user);
    }
    @Override
    public List<NoteShare> getSharesGrantedByUser(User owner) {
        return noteShareRepository.findByNote_User(owner);
    }
}