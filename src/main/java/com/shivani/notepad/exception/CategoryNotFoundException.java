package com.shivani.notepad.exception;

/**
 * Thrown when a requested category doesn't exist, or exists but doesn't
 * belong to the currently authenticated user.
 */
public class CategoryNotFoundException extends RuntimeException{
    public CategoryNotFoundException(String message){
        super(message);
    }
}
