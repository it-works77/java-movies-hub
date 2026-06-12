package ru.practicum.moviehub.exception;

public class MovieStoreNoSuchMovieException extends MovieStoreException {
    public MovieStoreNoSuchMovieException(String message) {
        super(message);
    }
}
