package ru.practicum.moviehub.model;

import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.exception.AbsentTitleException;
import ru.practicum.moviehub.exception.MovieException;
import ru.practicum.moviehub.exception.WrongTitleException;
import ru.practicum.moviehub.exception.WrongYearException;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

class MovieTest {

    @Test
    void newMovie_whenWrongTitle_exception() {
        assertThrows(WrongTitleException.class, () -> new Movie("", 2000));
        assertThrows(AbsentTitleException.class, () -> new Movie(null, 2000));
        assertThrows(WrongTitleException.class, () -> new Movie("A".repeat(101), 2000));
    }

    @Test
    void newMovie_whenWrongYear_exception() {
        assertThrows(WrongYearException.class, () -> new Movie("A".repeat(100), 1887));
        assertThrows(WrongYearException.class, () -> new Movie("A", Year.now().getValue() + 2));
    }

    @Test
    void newMovie_whenAllCorrect_returnMovie() throws MovieException {
        Movie movie1 = new Movie("A".repeat(100), Year.now().getValue() + 1);
        Movie movie2 = new Movie("A", 1888);
    }
}