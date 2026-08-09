package com.shivani.notepad.exception;

/**
 * Thrown when a requested note doesn't exist, or exists but doesn't
 * belong to the currently authenticated user (we deliberately don't
 * distinguish between these two cases in the error message, to avoid
 * leaking information about which note IDs exist for other users).
 */
public class NoteNotFoundException extends RuntimeException{
    public NoteNotFoundException(String message){
        super(message);
    }
}
