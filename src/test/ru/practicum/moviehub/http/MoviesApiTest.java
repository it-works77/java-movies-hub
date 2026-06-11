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

public class MoviesApiTest {
    private static MoviesStore store;
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore moviesStore;

    private static final int CONNECTION_TIMEOUT = 5;

    @BeforeAll
    static void beforeAll() {
        store = new MoviesStore();
        server = new MoviesServer(moviesStore, SERVER_BASE_URL);
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
    // Get movies collection
    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpResponse<String> resp = getResponseForGetRequest("/movies");


        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        checkContentTypeHeader(resp);

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void HeadMovies_returnMethodNotAllowed() throws Exception {
        // .
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + "/movies"))
                .HEAD()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(405, resp.statusCode(), "При неподдерживаемом HTTP-методе " +
                "возвращается `405 Method Not Allowed`");

        // TODO Check: Сервер ОБЯЗАН сгенерировать поле заголовка Allow в ответе с кодом 405

    }

    private static HttpResponse<String> getResponseForGetRequest(String s) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + s))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return resp;
    }

    private static void checkContentTypeHeader(HttpResponse<String> resp) {
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }
}