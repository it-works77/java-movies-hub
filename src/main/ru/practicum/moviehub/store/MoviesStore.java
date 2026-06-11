package ru.practicum.moviehub.store;

import ru.practicum.moviehub.exception.MovieException;
import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.Optional;

public class MoviesStore {
    private final HashMap<Integer, Movie> store;

    public MoviesStore() {
        this.store = new HashMap<>();
    }

    public void putMovie(Movie movie) {
        store.put(getId(), movie);
    }

    public void putMovie(String title, Integer year) throws MovieException {
        store.put(getId(), new Movie(title, year));
    }

    public Optional<Movie> getMovie(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    public void clear() {
        store.clear();
    }

    private Integer getId() {
        Optional<Integer> currentMaxId = store.keySet().stream()
                .max(Integer::compareTo);

        return currentMaxId.map(integer -> integer + 1).orElse(1);
    }
}