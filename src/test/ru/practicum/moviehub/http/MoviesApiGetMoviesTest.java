package ru.practicum.moviehub.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

public class MoviesApiGetMoviesTest {
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore store;

    private static final int CONNECTION_TIMEOUT = 5;

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

    }

    @AfterAll
    static void afterAll() {
        if (server != null) {
            server.stop();
        }
    }
    //

    /*  Get movies
    *   - возвращает пустой список, если нет фильмов;
    *   - возвращает список с ранее добавленными фильмами.
    */
    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {

        HttpResponse<String> resp = getResponseForGetRequest("/movies");

        checkResponseContentTypeHeader(resp);
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void getMovies_whenEmpty_returnsAddedMovies() throws Exception {

        // TODO добавить фильмы

        HttpResponse<String> resp = getResponseForGetRequest("/movies");

        checkResponseContentTypeHeader(resp);
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        // TODO Fix Проверить, что вернулись
        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    /* GET /movies/{id}
     *  - возвращает фильм по существующему `id`;
     *  - возвращает ошибку, если фильм не найден;
     *  - возвращает ошибку, если `id` не число.
     */

    @Test
    void getMovies_whenIdExists_returnsMovie() throws Exception {

        HttpResponse<String> resp = getResponseForGetRequest("/movies/1");

        checkResponseContentTypeHeader(resp);
        assertEquals(200, resp.statusCode(), "GET /movies/{id} должен вернуть 200");

        // TODO Implement body check
        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void getMovies_whenNotExists_returnsNotFound() throws Exception {

        HttpResponse<String> resp = getResponseForGetRequest("/movies/999999999");

        checkResponseContentTypeHeader(resp);
        assertEquals(404, resp.statusCode(), "GET /movies/{id} должен вернуть 404 для неверного id");

        // TODO Implement body check
        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    /* GET /movies?year=YYYY
     *  - возвращает фильмы указанного года;
     *  - возвращает пустой список, если фильмов с таким годом нет;
     *  - возвращает ошибку, если параметр `year` не число.
     */

    private static HttpResponse<String> getResponseForGetRequest(String s) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + s))
                .GET()
                .build();

        return client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void checkResponseContentTypeHeader(HttpResponse<String> resp) {
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }
}