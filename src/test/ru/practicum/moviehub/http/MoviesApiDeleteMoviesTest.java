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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.moviehub.MovieHubApp.SERVER_BASE_URL;
import static ru.practicum.moviehub.http.MoviesServer.MOVIES_CONTEXT;

public class MoviesApiDeleteMoviesTest {
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

    /* DELETE /movies/{id}
     *  - удаляет фильм по существующему `id`;
     *  - возвращает ошибку, если фильм не найден;
     *  - возвращает ошибку, если `id` не число.
     */

    // удаляет фильм по существующему `id`;
    @Test
    void deleteMovie_returnsSuccess() throws Exception {
        store.putMovie(new Movie("a", 2000));

        HttpResponse<String> resp = getResponseForDeleteRequest(ROUTE + "/1");

        checkResponseContentTypeHeader(resp);
        assertEquals(204, resp.statusCode(), "DELETE /movies/{id} " +
                "удаляет фильм по существующему `id`");

        Optional<Movie> movie = store.getMovie(1);
        assertTrue(movie.isEmpty());
    }

    // возвращает ошибку, если фильм не найден;
    @Test
    void deleteMovie_whenWrongId_returnsError() throws Exception {
        HttpResponse<String> resp = getResponseForDeleteRequest(ROUTE + "/9999");

        checkResponseContentTypeHeader(resp);
        assertEquals(404, resp.statusCode(), "DELETE /movies/{id} " +
                "возвращает ошибку, если фильм не найден");

        String body = resp.body().trim();
        assertEquals("{\"error\":\"Некорректный ID\",\"details\":\"Фильм не найден\"}", body,
                "Ожидается описание ошибки");
    }

    // возвращает ошибку, если `id` не число.
    @Test
    void deleteMovie_whenIdNotNumber_returnsError() throws Exception {
        HttpResponse<String> resp = getResponseForDeleteRequest(ROUTE + "/asdf");

        checkResponseContentTypeHeader(resp);
        assertEquals(400, resp.statusCode(), "DELETE /movies/{id} " +
                "возвращает ошибку, если фильм не найден");

        String body = resp.body().trim();
        assertEquals("{\"error\":\"Некорректный ID\",\"details\":\"ID, указанный в пути запроса, не число\"}",
                body,
                "Ожидается описание ошибки");
    }

    private static HttpResponse<String> getResponseForDeleteRequest(String s) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + s))
                .DELETE()
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