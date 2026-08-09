package com.shivani.notepad.exception;
/**
 * Thrown when a registration attempt uses a username or email
 * that already exists in the database. Caught in the controller
 * layer to display a user-friendly error message instead of a
 * generic 500 error page.
 */
public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(String message){
        super(message);
    }
}
