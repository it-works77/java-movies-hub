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

public class MoviesApiDeleteMoviesTest {
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

    /* DELETE /movies/{id}
     *  - удаляет фильм по существующему `id`;
     *  - возвращает ошибку, если фильм не найден;
     *  - возвращает ошибку, если `id` не число.
     */




    private static HttpResponse<String> getResponseForDeleteRequest(String s) throws IOException, InterruptedException {
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