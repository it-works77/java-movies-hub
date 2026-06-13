package ru.practicum.moviehub.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

public class MoviesApiGetMoviesTest {
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
                "Ожидается пустой JSON-массив");
    }

    @Test
    void getMovies_whenHaveMovies_returnsAddedMovies() throws Exception {

        store.putMovie(new Movie("a", 2000));
        store.putMovie(new Movie("b", 2001));

        HttpResponse<String> resp = getResponseForGetRequest("/movies");

        checkResponseContentTypeHeader(resp);
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String body = resp.body().trim();
        assertEquals("[{\"id\":1,\"title\":\"a\",\"year\":2000},{\"id\":2,\"title\":\"b\",\"year\":2001}]", body,
                "Ожидается массив из двух фильмов");
    }

    /* GET /movies/{id}
     *  - возвращает фильм по существующему `id`;
     *  - возвращает ошибку, если фильм не найден;
     *  - возвращает ошибку, если `id` не число.
     */

    @Test
    void getMovies_whenIdExists_returnsMovie() throws Exception {
        store.putMovie(new Movie("a", 2000));

        HttpResponse<String> resp = getResponseForGetRequest("/movies/1");

        checkResponseContentTypeHeader(resp);
        assertEquals(200, resp.statusCode(), "GET /movies/{id} должен вернуть 200");

        String body = resp.body().trim();
        assertEquals("{\"id\":1,\"title\":\"a\",\"year\":2000}", body,
                "Ожидается объект фильма");
    }

    @Test
    void getMovie_whenIdNotNumber_returnsError() throws Exception {
        HttpResponse<String> resp = getResponseForGetRequest(ROUTE + "/asdf");

        checkResponseContentTypeHeader(resp);
        assertEquals(400, resp.statusCode(), "GET /movies/{id} " +
                "возвращает ошибку, если id не является числом");

        String body = resp.body().trim();
        assertEquals("{\"error\":\"Некорректный ID\",\"details\":\"ID, указанный в пути запроса, не число\"}", body,
                "Ожидается описание ошибки");
    }

    @Test
    void getMovies_whenNotExists_returnsNotFound() throws Exception {
        HttpResponse<String> resp = getResponseForGetRequest("/movies/999999999");

        checkResponseContentTypeHeader(resp);
        assertEquals(404, resp.statusCode(), "GET /movies/{id} должен вернуть 404 для неверного id");

        String body = resp.body().trim();
        assertEquals("{\"error\":\"Некорректный ID\",\"details\":\"Фильм не найден\"}", body,
                "Ожидается описание ошибки");
    }

    /* GET /movies?year=YYYY
     *  - возвращает фильмы указанного года;
     *  - возвращает пустой список, если фильмов с таким годом нет;
     *  - возвращает ошибку, если параметр `year` не число.
     */
    @Test
    void getMovies_whenHaveMoviesWithSuchYear_returnsMovies() throws Exception {

        store.putMovie(new Movie("a", 2000));
        store.putMovie(new Movie("b", 2001));
        store.putMovie(new Movie("c", 2001));

        HttpResponse<String> resp = getResponseForGetRequest("/movies?year=2001");

        checkResponseContentTypeHeader(resp);
        assertEquals(200, resp.statusCode(), "GET /movies?year=2001 должен вернуть 200");

        String body = resp.body().trim();
        assertEquals("[{\"id\":2,\"title\":\"b\",\"year\":2001},{\"id\":3,\"title\":\"c\",\"year\":2001}]", body,
                "Ожидается массив из двух фильмов");
    }

    @Test
    void getMoviesByYear_whenEmpty_returnsEmptyArray() throws IOException, InterruptedException {
        HttpResponse<String> resp = getResponseForGetRequest("/movies?year=9999");

        checkResponseContentTypeHeader(resp);
        assertEquals(200, resp.statusCode(), "GET /movies?year=9999 должен вернуть 200");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается пустой JSON-массив");
    }

    @Test
    void getMoviesByYear_whenYearNotNumber_returnsError() throws IOException, InterruptedException {
        HttpResponse<String> resp = getResponseForGetRequest("/movies?year=asdf");

        checkResponseContentTypeHeader(resp);
        assertEquals(400, resp.statusCode(), "GET /movies?year=9999 " +
                "возвращает ошибку, если id не является числом");

        String body = resp.body().trim();
        assertEquals("{\"error\":\"Некорректный year\",\"details\":\"Год, указанный в пути запроса, не является числом\"}", body,
                "Ожидается описание ошибки");
    }



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