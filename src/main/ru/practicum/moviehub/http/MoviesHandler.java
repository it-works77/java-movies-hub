package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.dto.response.MovieResponse;
import ru.practicum.moviehub.exception.*;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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

        if (pathParts.length != 2) {
            sendError(ex, 400, "Неверный путь запроса", "Path: %s".formatted(path));
        }

        String body;
        try (InputStream inputStream = ex.getRequestBody()) {
            body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            sendError(ex, 500, "IOException", "Can't read request bode");
            return;
        }

        try {
            Movie movie = Movie.fromJson(body);

            int id = moviesStore.putMovie(movie);
            MovieResponse movieResponse = new MovieResponse(id, movie.getTitle(), movie.getYear());

            Gson gson = new Gson();
            String movieResponseString = gson.toJson(movieResponse);
            sendJson(ex, 201, movieResponseString);

        } catch (JsonSyntaxException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            sendError(ex, 422, "Ошибка валидации запроса", "Неверный Json");

        } catch (AbsentTitleException e) {
            System.out.println(e.getMessage());
            sendError(ex, 422, "Ошибка валидации запроса", "Отсутствует название");

        } catch (WrongTitleException e) {
            System.out.println(e.getMessage());
            sendError(ex, 422, "Ошибка валидации запроса", "Неверное название: %s"
                    .formatted(e.getMessage()));

        } catch (AbsentYearException e) {
            System.out.println(e.getMessage());
            sendError(ex, 422, "Ошибка валидации запроса", "Отсутствует год");

        } catch (WrongYearException e) {
            System.out.println(e.getMessage());
            sendError(ex, 422, "Ошибка валидации запроса", "Неверные год: %s"
                    .formatted(e.getMessage()));

        } catch (MovieException e) {
            System.out.println(e.getMessage());
            sendError(ex, 422, "Ошибка валидации запроса", "MovieException: %s"
                    .formatted(e.getMessage()));

        } catch (MovieStoreMovieExistsException e) {
            System.out.println("Некорректный title. Фильм уже добавлен");
            sendError(ex, 404, "Некорректный title", "Фильм уже добавлен");
        }
    }

    private void handleDelete(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (pathParts.length != 3) {
            sendError(ex, 400, "Неверный путь запроса", "Path: %s".formatted(path));
        }

        try {
            int id = Integer.parseInt(pathParts[2]);
            moviesStore.removeMovie(id);
            sendNoContent(ex);
        } catch (NumberFormatException e) {
            sendError(ex, 400, "Некорректный ID", "ID, указанный в пути запроса, не число");
        } catch (MovieStoreNoSuchMovieException e) {
            sendError(ex, 404, "Некорректный ID", "Фильм не найден");
        }
    }
}
