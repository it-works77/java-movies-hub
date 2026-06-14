package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class MoviesServer {
    private final MoviesStore moviesStore;
    private final String serverBaseUrl;
    private final HttpServer server;

    public static final String MOVIES_CONTEXT = "/movies";
    public static final List<String> ALLOWED_METHODS = List.of("GET", "POST", "DELETE");
    public static final int STOP_DELAY = 1;

    public MoviesServer(MoviesStore moviesStore, String serverBaseUrl) {
        this.moviesStore = moviesStore;
        this.serverBaseUrl = serverBaseUrl;

        try {

            server = HttpServer.create(new InetSocketAddress(new URI(serverBaseUrl).getPort()), 0);
            server.createContext(MOVIES_CONTEXT, new MoviesHandler(moviesStore));

        } catch (IOException ex) {
            throw new RuntimeException("Не удалось создать HTTP-сервер для %s\n".formatted(serverBaseUrl), ex);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(("Не удалось создать HTTP-сервер для %s:\nНеверный порт.")
                    .formatted(serverBaseUrl), ex);
        }
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        server.stop(STOP_DELAY);
        System.out.println("Сервер остановлен");
    }
}