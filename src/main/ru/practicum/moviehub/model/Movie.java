package ru.practicum.moviehub.model;

import ru.practicum.moviehub.exception.AbsentTitleException;
import ru.practicum.moviehub.exception.MovieException;
import ru.practicum.moviehub.exception.WrongTitleException;

public class Movie {
    private final String title;
    private final Integer year;

    public Movie(String title, Integer year) throws MovieException {
        // title — не пустая строка, длина ≤ 100 символов.
        if (title == null) {
            throw new AbsentTitleException("title — не может быть null.");
        } else if (title.isBlank()) {
            throw new WrongTitleException("title — не пустая строка.");
        } else if (title.length() > 100) {
            throw new WrongTitleException("title - длина ≤ 100 символов.");
        }

        this.title = title;
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public Integer getYear() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Movie movie = (Movie) o;
        return title.equals(movie.title) && year.equals(movie.year);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + year.hashCode();
        return result;
    }
}