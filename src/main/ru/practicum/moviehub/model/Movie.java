package ru.practicum.moviehub.model;

import com.google.gson.Gson;
import ru.practicum.moviehub.exception.*;

import java.time.Year;

public class Movie {
    private final String title;
    private final Integer year;
    private static final Gson gson = new Gson();

    private static final Integer MAX_TITLE_LENGTH = 100;
    private static final Integer MIN_YEAR_VALUE = 1888;

    public Movie(String title, Integer year) throws MovieException {
        this.title = title;
        this.year = year;
        validate();
    }

    public Movie(Movie movie){
        title = movie.getTitle();
        year = movie.getYear();
    }

    public static Movie fromJson(String movieJson) throws MovieException {
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
        } else if (title.length() > MAX_TITLE_LENGTH) {
            throw new WrongTitleException("title - длина ≤ %d символов.".formatted(MAX_TITLE_LENGTH));
        } else if (year == null) {
            throw new AbsentYearException("year — не может быть null.");
        } else if (year < MIN_YEAR_VALUE) {
            throw new WrongYearException("year - не может быть меньше %d".formatted(MIN_YEAR_VALUE));
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