package com.shivani.notepad.exception;

/**
 * Thrown when a requested todo doesn't exist, or exists but its parent
 * note doesn't belong to the currently authenticated user.
 */
public class TodoNotFoundException extends RuntimeException{
    public TodoNotFoundException(String message){
        super(message);
    }
}
