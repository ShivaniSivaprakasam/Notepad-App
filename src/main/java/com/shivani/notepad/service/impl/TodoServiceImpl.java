package com.shivani.notepad.service.impl;

import com.shivani.notepad.dto.TodoDto;
import com.shivani.notepad.entity.Note;
import com.shivani.notepad.entity.Todo;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.exception.TodoNotFoundException;
import com.shivani.notepad.repository.NoteRepository;
import com.shivani.notepad.repository.TodoRepository;
import com.shivani.notepad.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final NoteRepository noteRepository;

    @Autowired
    public TodoServiceImpl(TodoRepository todoRepository, NoteRepository noteRepository) {
        this.todoRepository = todoRepository;
        this.noteRepository = noteRepository;
    }

    @Override
    public Todo createTask(TodoDto dto, User owner) {
        String taskText = dto.getTask() == null ? "" : dto.getTask().trim();
        if (taskText.isEmpty()) {
            throw new IllegalArgumentException("Task description cannot be blank.");
        }

        Note note = null;
        if (dto.getNoteId() != null) {
            note = getOwnedNote(dto.getNoteId(), owner);
        }

        boolean duplicate = (note != null)
                ? todoRepository.existsByUserAndNoteAndTaskIgnoreCase(owner, note, taskText)
                : todoRepository.existsByUserAndNoteIsNullAndTaskIgnoreCase(owner, taskText);

        if (duplicate) {
            throw new IllegalArgumentException("This task already exists.");
        }

        Todo todo = new Todo();
        todo.setTask(taskText);
        todo.setCompleted(false);
        todo.setUser(owner);
        todo.setNote(note);

        return todoRepository.save(todo);
    }

    @Override
    public void deleteTask(Integer todoId, User owner) {
        todoRepository.delete(getOwnedTodo(todoId, owner));
    }

    @Override
    public Todo markComplete(Integer todoId, User owner) {
        Todo todo = getOwnedTodo(todoId, owner);
        todo.setCompleted(true);
        return todoRepository.save(todo);
    }

    @Override
    public Todo markIncomplete(Integer todoId, User owner) {
        Todo todo = getOwnedTodo(todoId, owner);
        todo.setCompleted(false);
        return todoRepository.save(todo);
    }

    @Override
    public List<Todo> getTasks(Integer noteId, User owner) {
        return todoRepository.findByNote(getOwnedNote(noteId, owner));
    }

    @Override
    public List<Todo> getAllTasks(User owner) {
        return todoRepository.findAllByUser(owner);
    }

    private Note getOwnedNote(Integer noteId, User owner) {
        return noteRepository.findByIdAndUser(noteId, owner)
                .orElseThrow(() -> new TodoNotFoundException(
                        "Note not found or you do not have access to it: " + noteId));
    }

    private Todo getOwnedTodo(Integer todoId, User owner) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoNotFoundException("Todo not found: " + todoId));
        if (!isOwnedBy(todo, owner)) {
            throw new TodoNotFoundException("Todo not found or you do not have access to it: " + todoId);
        }
        return todo;
    }

    /** Prefers the direct user reference; falls back to the note's owner for legacy rows. */
    private boolean isOwnedBy(Todo todo, User owner) {
        if (todo.getUser() != null) return todo.getUser().getId().equals(owner.getId());
        if (todo.getNote() != null) return todo.getNote().getUser().getId().equals(owner.getId());
        return false;
    }
}