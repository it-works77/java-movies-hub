package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.exception.MovieStoreNoSuchMovieException;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

import static ru.practicum.moviehub.http.MoviesServer.ALLOWED_METHODS;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore moviesStore;

    public MoviesHandler(MoviesStore moviesStore) {
        super();
        this.moviesStore = moviesStore;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod().toUpperCase();

        if (!ALLOWED_METHODS.contains(method)) {
            sendMethodNotAllowed(ex);
            return;
        }

        switch (method) {
            case "GET":
                sendJson(ex, 200, "[]");
                break;
            case "POST":
                handlePost(ex);
                break;
            case "DELETE":
                handleDelete(ex);
                break;
            default:
                sendError(ex, 500, "No handler", "It's not handled at all");
        }
    }

    private void handlePost(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length != 3) {
            sendError(ex,400, "Неверный путь запроса", "Path: %s".formatted(path));
        }
    }

    private void handleDelete(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length != 3) {
            sendError(ex,400, "Неверный путь запроса", "Path: %s".formatted(path));
        }

        try {
            int id = Integer.parseInt(pathParts[2]);
            moviesStore.removeMovie(id);
            sendNoContent(ex);
        } catch (NumberFormatException e) {
            sendError(ex,400, "Некорректный ID", "ID, указанный в пути запроса, не число");

        } catch (MovieStoreNoSuchMovieException e) {
            sendError(ex,404, "Некорректный ID", "Фильм не найден");
        }
    }
}
