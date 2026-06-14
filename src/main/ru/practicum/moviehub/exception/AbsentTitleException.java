package ru.practicum.moviehub.exception;

public class AbsentTitleException extends MovieException {
    public AbsentTitleException(String message) {
        super(message);
    }
}