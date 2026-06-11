package ru.practicum.moviehub.exception;

public class WrongTitleException extends MovieException {
    public WrongTitleException(String message) {
        super(message);
    }
}