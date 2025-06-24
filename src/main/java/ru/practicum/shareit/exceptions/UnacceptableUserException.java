package ru.practicum.shareit.exceptions;

public class UnacceptableUserException extends RuntimeException {
    public UnacceptableUserException(String message) {
        super(message);
    }
}
