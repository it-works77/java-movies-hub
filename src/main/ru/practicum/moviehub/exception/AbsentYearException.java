package ru.practicum.moviehub.exception;

public class AbsentYearException extends MovieException {
    public AbsentYearException(String message) {
        super(message);
    }
}