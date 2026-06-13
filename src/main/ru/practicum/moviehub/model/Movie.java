package ru.practicum.moviehub.model;

import com.google.gson.Gson;
import ru.practicum.moviehub.exception.*;

import java.time.Year;

public class Movie {
    private final String title;
    private final Integer year;

    public Movie(String title, Integer year) throws MovieException {
        this.title = title;
        this.year = year;
        validate();
    }

    public static Movie fromJson(String movieJson) throws MovieException {
        Gson gson = new Gson();
        Movie movie = gson.fromJson(movieJson, Movie.class);
        movie.validate();
        return movie;
    }

    public String getTitle() {
        return title;
    }

    public Integer getYear() {
        return year;
    }

    private void validate() throws MovieException {
        if (title == null) {
            throw new AbsentTitleException("title — не может быть null.");
        } else if (title.isBlank()) {
            throw new WrongTitleException("title — не пустая строка.");
        } else if (title.length() > 100) {
            throw new WrongTitleException("title - длина ≤ 100 символов.");
        } else if (year == null) {
            throw new AbsentYearException("year — не может быть null.");
        } else if (year < 1888) {
            throw new WrongYearException("year - не может быть меньше 1888");
        } else if (year > (Year.now().getValue() + 1)) {
            throw new WrongYearException("year - не может быть больше, чем текущий год + 1");
        }
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