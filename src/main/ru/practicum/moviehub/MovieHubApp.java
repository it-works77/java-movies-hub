package ru.practicum.moviehub;

import ru.practicum.moviehub.http.MoviesServer;
import ru.practicum.moviehub.store.MoviesStore;

public class MovieHubApp {
    public static final String SERVER_BASE_URL = "http://localhost:8080";

    public static void main(String[] args) {
        final MoviesServer server = new MoviesServer(new MoviesStore(), SERVER_BASE_URL);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }
}