package ru.practicum.moviehub.http;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.response.MovieResponse;
import ru.practicum.moviehub.exception.*;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

        try {
            if (!ALLOWED_METHODS.contains(method)) {
                sendMethodNotAllowed(ex);
                return;
            }

            switch (method) {
                case "GET":
                    handleGet(ex);
                    break;
                case "POST":
                    if (ex.getRequestHeaders().containsKey("Content-Type")
                            && ex.getRequestHeaders().get("Content-Type").contains("application/json")) {
                        handlePost(ex);
                    } else {
                        sendUnsupportedMediaType(ex);
                        return;
                    }
                    break;
                case "DELETE":
                    handleDelete(ex);
                    break;
                default:
                    sendError(ex, 501, "No handler", "It's not handled at all");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            sendError(ex, 500, "Internal Server Error", """
                    Сервер столкнулся с неожиданной ошибкой, которая помешала ему выполнить запрос.
                    Этот код является обобщённым ответом на перехват всех исключений, которые не были обработаны.
                    """);
        }
    }

    private void handleGet(HttpExchange ex) throws IOException {
        String[] pathParts = getPathParts(ex);
        HashMap<String, String> queryParams = getQueryParams(ex);

        if (pathParts.length > 3) {
            sendError(ex, 400, "Неверный запрос", "Неверный путь или параметры запроса");
        }

        if (pathParts.length == 2) {
            if (queryParams.isEmpty()) {
                // GET /movies - Получение всех фильмов
                Map<Integer, Movie> movies = moviesStore.get();
                String movieResponsesJsonString = getJsonStringFromList(getMoviesResponses(movies));
                sendJson(ex, 200, movieResponsesJsonString);
            } else if (queryParams.containsKey("year")) {
                // GET /movies?year=YYYY - Получение фильмов по году

                int year;
                try {
                    year = Integer.parseInt(queryParams.get("year"));
                } catch (NumberFormatException e) {
                    sendError(ex, 400,
                            "Некорректный year", "Год, указанный в пути запроса, не является числом");
                    return;
                }

                Map<Integer, Movie> movies = moviesStore.getMoviesByYear(year);
                String movieResponsesJsonString = getJsonStringFromList(getMoviesResponses(movies));
                sendJson(ex, 200, movieResponsesJsonString);
            }
        } else if (pathParts.length == 3) {
            // GET /movies/{id} - получение фильма по Id
            try {
                int id = Integer.parseInt(pathParts[2]);
                Optional<Movie> movieOpt = moviesStore.getMovie(id);
                if (movieOpt.isEmpty()) {
                    sendError(ex, 404, "Некорректный ID", "Фильм не найден");
                } else {
                    Movie movie = movieOpt.get();
                    MovieResponse movieResponse = new MovieResponse(id, movie.getTitle(), movie.getYear());

                    String movieResponseString = gson.toJson(movieResponse);
                    sendJson(ex, 200, movieResponseString);
                }
            } catch (NumberFormatException e) {
                sendError(ex, 400, "Некорректный ID", "ID, указанный в пути запроса, не число");
            }
        } else {
            // Неверный запрос
            sendError(ex, 400, "Неверный запрос", "Неверный путь или параметры запроса");
        }
    }

    private String getJsonStringFromList(List<?> entriesList) {
        return gson.toJson(entriesList);
    }

    private List<MovieResponse> getMoviesResponses(Map<Integer, Movie> movies) {
            return movies.entrySet().stream()
                    .map(entry -> new MovieResponse(entry.getKey(),
                            entry.getValue().getTitle(),
                            entry.getValue().getYear())
                    )
                    .toList();
    }

    private void handlePost(HttpExchange ex) throws IOException {
        String[] pathParts = getPathParts(ex);

        if (pathParts.length != 2) {
            sendError(ex, 400, "Неверный запрос", "Неверный путь или параметры запроса");
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

            String movieResponseString = gson.toJson(movieResponse);
            sendJson(ex, 201, movieResponseString);

        } catch (JsonSyntaxException e) {
            System.out.println(e.getMessage());
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
            sendError(ex, 422, "Ошибка валидации запроса", "Неверный год: %s"
                    .formatted(e.getMessage()));

        } catch (MovieException e) {
            System.out.println(e.getMessage());
            sendError(ex, 422, "Ошибка валидации запроса", "MovieException: %s"
                    .formatted(e.getMessage()));

        } catch (MovieStoreMovieExistsException e) {
            System.out.println("Некорректный title. Фильм уже добавлен");
            sendError(ex, 422, "Ошибка добавления фильма", "Такой фильм уже добавлен в хранилище");
        }
    }

    private void handleDelete(HttpExchange ex) throws IOException {
        String[] pathParts = getPathParts(ex);

        if (pathParts.length != 3) {
            sendError(ex, 400, "Неверный запрос", "Неверный путь или параметры запроса");
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

    private String[] getPathParts(HttpExchange ex) {
        String path = ex.getRequestURI().getPath();
        return path.split("/");
    }

    private HashMap<String, String> getQueryParams(HttpExchange ex) {
        String queryParamsString = ex.getRequestURI().getQuery();

        HashMap<String, String> result = new HashMap<>();
        if (queryParamsString == null) {
            return result;
        }

        String[] paramsEntries = queryParamsString.split("&");

        for (String s: paramsEntries) {
            String[] paramEntry = queryParamsString.split("=");
            if (paramEntry.length == 2) {
                if (!(paramEntry[0].isBlank() && paramEntry[1].isBlank())) {
                    result.put(paramEntry[0], paramEntry[1]);
                } else {
                    System.out.printf("Skipping param %s%n", s);
                }
            }
        }
        return result;
    }

}
