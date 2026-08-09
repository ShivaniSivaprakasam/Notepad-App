package com.shivani.notepad.service;

import com.shivani.notepad.entity.NoteShare;
import com.shivani.notepad.entity.SharePermission;
import com.shivani.notepad.entity.User;

import java.util.List;

public interface NoteShareService {

    NoteShare shareNote(Integer noteId, User owner, String recipientEmail, SharePermission permission);

    /** Removes a share. Returns the noteId, purely for controller redirect convenience. */
    Integer unshareNote(Integer shareId, User owner);

    List<NoteShare> getSharesForNote(Integer noteId, User owner);

    List<NoteShare> getNotesSharedWithMe(User user);

    List<NoteShare> getSharesGrantedByUser(User owner);
}
