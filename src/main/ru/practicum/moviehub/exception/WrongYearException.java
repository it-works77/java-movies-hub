package ru.practicum.moviehub.exception;

public class WrongYearException extends MovieException {
    public WrongYearException(String message) {
        super(message);
    }
}