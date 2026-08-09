package com.shivani.notepad.service;

import com.shivani.notepad.dto.TodoDto;
import com.shivani.notepad.entity.Todo;
import com.shivani.notepad.entity.User;

import java.util.List;

/**
 * Business logic contract for todo management. Since Todo has no direct
 * `user` field (it belongs to a Note, which belongs to a User), every
 * ownership check here traverses Todo -> Note -> User.
 */
public interface TodoService {

    /** Creates a new todo attached to the given note, verifying note ownership. */
    Todo createTask(TodoDto dto, User owner);

    /** Deletes a todo, verifying ownership via its parent note. */
    void deleteTask(Integer todoId, User owner);

    /** Marks a todo as complete. */
    Todo markComplete(Integer todoId, User owner);

    /** Marks a todo as incomplete. */
    Todo markIncomplete(Integer todoId, User owner);

    /** Retrieves all todos for a given note, verifying ownership. */
    List<Todo> getTasks(Integer noteId, User owner);

    List<Todo> getAllTasks(User owner);
}