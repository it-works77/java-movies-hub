package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.exception.MovieException;
import ru.practicum.moviehub.exception.MovieStoreMovieExistsException;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.moviehub.MovieHubApp.SERVER_BASE_URL;
import static ru.practicum.moviehub.http.MoviesServer.MOVIES_CONTEXT;

public class MoviesApiPostMoviesTest {
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore store;

    private static final int CONNECTION_TIMEOUT = 5;
    private static final String ROUTE = MOVIES_CONTEXT;


    @BeforeAll
    static void beforeAll() {
        store = new MoviesStore();
        server = new MoviesServer(store, SERVER_BASE_URL);
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT))
                .build();
    }

    @BeforeEach
    void beforeEach() {
        store.clear();
    }

    @AfterAll
    static void afterAll() {
        if (server != null) {
            server.stop();
        }
    }

    /* POST /movies:
     *  - добавляет фильм при корректных данных;
     *  - возвращает ошибку при пустом `title`;
     *  - возвращает ошибку при слишком длинном `title` (> 100 символов);
     *  - возвращает ошибку при неверном `year` (меньше 1888 или больше текущего года + 1);
     *  - возвращает ошибку при неправильном `Content-Type`;
     *  - возвращает ошибку при некорректном JSON.
     */

    // - добавляет фильм при корректных данных;
    @Test
    void postMovie_whenCorrect_returnsAddedMovie() throws Exception {
        HttpResponse<String> resp = getResponseForPostMovieRequest(ROUTE, "a", 2000);

        checkResponseContentTypeHeader(resp);
        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");
        // TODO Implement body check
        // Тело — JSON созданного фильма с присвоенным ID.
        String body = resp.body().trim();
        assertEquals("""
                        {"id":1,"title":"a","year":2000}""", body,
                "Ожидается добавленный в хранилище фильм с присвоенным id");
    }

    // - возвращает ошибку при пустом `title`;
    @Test
    void postMovie_whenEmptyTitle_returnsError() throws Exception {
        String movieJsonString = """
                {"title":"","year":2000}
                """;

        HttpResponse<String> resp = getResponseForPostMovieJsonRequest(ROUTE, movieJsonString);

        checkResponseContentTypeHeader(resp);
        assertEquals(422, resp.statusCode(), "POST /movies возвращает ошибку 422 при пустом `title`");

        String body = resp.body().trim();
        assertEquals("""
                        {"error":"Ошибка валидации запроса","details":"Неверное название: title — не пустая строка."}""",
                body,
                "Ожидается описание ошибки");
    }

    // - возвращает ошибку при слишком длинном `title` (> 100 символов);
    @Test
    void postMovie_whenWrongTitleLength_returnsError() throws Exception {
        String movieJsonString = """
                {"title": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1", "year": 2000}
                """;

        HttpResponse<String> resp = getResponseForPostMovieJsonRequest(ROUTE, movieJsonString);

        checkResponseContentTypeHeader(resp);
        assertEquals(422, resp.statusCode(), "POST /movies возвращает ошибку 422 " +
                "при слишком длинном `title` (> 100 символов)`");

        String body = resp.body().trim();
        assertEquals("""
                        {"error":"Ошибка валидации запроса","details":"Неверное название: title - длина ≤ 100 символов."}""",
                body,
                "Ожидается описание ошибки");
    }

    //  - возвращает ошибку при неверном `year` (меньше 1888 или больше текущего года + 1);
    @Test
    void postMovie_whenYearTooEarly_returnsError() throws Exception {
        String movieJsonString = """
                {"title": "a", "year": 1887}
                """;

        HttpResponse<String> resp = getResponseForPostMovieJsonRequest(ROUTE, movieJsonString);

        checkResponseContentTypeHeader(resp);
        assertEquals(422, resp.statusCode(), "POST /movies возвращает ошибку 422 " +
                "при при неверном `year` (меньше 1888)");

        String body = resp.body().trim();
        assertEquals("""
                        {"error":"Ошибка валидации запроса","details":"Неверный год: year - не может быть меньше 1888"}""",
                body,
                "Ожидается описание ошибки");
    }

    @Test
    void postMovie_whenYearTooLate_returnsError() throws Exception {
        String movieJsonString = """
                {"title": "a", "year": 9999}
                """;

        HttpResponse<String> resp = getResponseForPostMovieJsonRequest(ROUTE, movieJsonString);

        checkResponseContentTypeHeader(resp);
        assertEquals(422, resp.statusCode(), "POST /movies возвращает ошибку 422 " +
                "при при неверном `year` (больше текущего года + 1)");

        String body = resp.body().trim();
        assertEquals("""
                        {"error":"Ошибка валидации запроса","details":"Неверный год: year - не может быть больше, чем текущий год + 1"}""",
                body,
                "Ожидается описание ошибки");
    }

    @Test
    void postMovie_whenAlreadyExistsInStore_returnsError() throws Exception {

        store.putMovie(new Movie("a", 2000));
        HttpResponse<String> resp = getResponseForPostMovieRequest(ROUTE, "a", 2000);

        checkResponseContentTypeHeader(resp);
        assertEquals(422, resp.statusCode(), "POST /movies возвращает ошибку 422 " +
                ", если фильм уже добавлен в хранилище.");

        // Тело — JSON созданного фильма с присвоенным ID.
        String body = resp.body().trim();
        assertEquals("""
                        {"error":"Ошибка добавления фильма","details":"Такой фильм уже добавлен в хранилище"}""", body,
                "Ожидается описание ошибки");
    }

    // - возвращает ошибку при некорректном JSON.
    @Test
    void postMovie_whenJsonIncorrect_returnsError() throws Exception {
        String movieJsonString = """
                not json
                """;

        HttpResponse<String> resp = getResponseForPostMovieJsonRequest(ROUTE, movieJsonString);

        checkResponseContentTypeHeader(resp);
        assertEquals(422, resp.statusCode(), "POST /movies возвращает ошибку 422 " +
                "при некорректном JSON.");

        String body = resp.body().trim();
        assertEquals("""
                        {"error":"Ошибка валидации запроса","details":"Неверный Json"}""", body,
                "Ожидается описание ошибки");
    }

    private static HttpResponse<String> getResponseForPostMovieRequest(String route,
                                                                       String movieTitle,
                                                                       Integer movieYear)
            throws IOException, InterruptedException, MovieException, MovieStoreMovieExistsException {
        Movie movie = new Movie(movieTitle, movieYear);

        Gson gson = new Gson();
        String movieJsonString = gson.toJson(movie);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + route))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(movieJsonString))
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static HttpResponse<String> getResponseForPostMovieJsonRequest(String route,
                                                                           String movieJsonString)
            throws IOException, InterruptedException, MovieException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + route))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(movieJsonString))
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    // - возвращает ошибку при неправильном `Content-Type`;
    private static void checkResponseContentTypeHeader(HttpResponse<String> resp) {
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }
}