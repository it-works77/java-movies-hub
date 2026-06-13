package ru.practicum.moviehub.store;

import ru.practicum.moviehub.exception.MovieException;
import ru.practicum.moviehub.exception.MovieStoreMovieExistsException;
import ru.practicum.moviehub.exception.MovieStoreNoSuchMovieException;
import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.Optional;

public class MoviesStore {
    private final HashMap<Integer, Movie> store;

    public MoviesStore() {
        this.store = new HashMap<>();
    }

    public int putMovie(Movie movie) throws MovieStoreMovieExistsException {
        int id = getId();
        if (store.containsValue(movie)) {
            throw new MovieStoreMovieExistsException("Фильм %s, снятый в %d уже есть в хранилище"
                    .formatted(movie.getTitle(), movie.getYear()));
        } else {
            store.put(id, movie);
            return id;
        }
    }

    public Optional<Movie> getMovie(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    public void removeMovie(int id) throws MovieStoreNoSuchMovieException {
        if (store.containsKey(id)) {
            store.remove(id);
        } else {
            throw new MovieStoreNoSuchMovieException("Нет фильма с идентификатором %d".formatted(id));
        }
    }

    private Integer getId() {
        Optional<Integer> currentMaxId = store.keySet().stream()
                .max(Integer::compareTo);

        return currentMaxId.map(id -> id + 1).orElse(1);
    }

    public void clear() {
        store.clear();
    }
}