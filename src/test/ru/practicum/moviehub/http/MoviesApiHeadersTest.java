package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.practicum.moviehub.MovieHubApp.SERVER_BASE_URL;

public class MoviesApiHeadersTest {
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

    @AfterAll
    static void afterAll() {
        if (server != null) {
            server.stop();
        }
    }

    /*  Общие:
     *   - при неподдерживаемом HTTP-методе возвращается `405 Method Not Allowed`..
     *   - если был получен запрос с неправильным значением заголовка Content-Type:
     * Код статуса — 415 Unsupported Media Type.
     */
    @Test
     void putMovies_returnMethodNotAllowed() throws Exception {
        String testString = "Test";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + "/movies"))
                .PUT(HttpRequest.BodyPublishers.ofString(testString))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(405, resp.statusCode(), "При неподдерживаемом HTTP-методе " +
                "возвращается `405 Method Not Allowed`");

        String body = resp.body().trim();
        assertEquals("{\"error\":\"Method Not Allowed\",\"details\":\"Неподдерживаемый HTTP метод\"}", body,
                "Ожидается описание ошибки");
    }

    @Test
    void getMovies_whenGetHasContentType_returnsSuccess() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + "/movies"))
                .setHeader("Content-Type", "application/xml")
                .GET()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(),
                "/movies");
    }

    @Test
    void postMovies_whenMissingContentType_returnUnsupportedMediaType() throws Exception {
        Gson gson = new Gson();
        String movieJsonString = gson.toJson(new Movie("A", 2000));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(movieJsonString))
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        checkResponseAcceptPostHeader(resp);
        assertEquals(415, resp.statusCode(),
                "запрос без заголовка Content-Type");
    }

    @Test
    void postMovies_whenWrongContentType_returnUnsupportedMediaType() throws Exception {
        Gson gson = new Gson();
        String movieJsonString = gson.toJson(new Movie("A", 2000));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + "/movies"))
                .setHeader("Content-Type", "application/xml")
                .POST(HttpRequest.BodyPublishers.ofString(movieJsonString))
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        checkResponseAcceptPostHeader(resp);
        assertEquals(415, resp.statusCode(),
                "запрос с неправильным значением заголовка Content-Type");
    }

    private static void checkResponseAcceptPostHeader(HttpResponse<String> resp) {
        String contentTypeHeaderValue =
                resp.headers().firstValue("Accept-Post").orElse("");
        assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }
}