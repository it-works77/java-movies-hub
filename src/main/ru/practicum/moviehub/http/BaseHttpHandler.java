package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final String CT_JSON = "application/json; charset=UTF-8"; // !!! Укажите содержимое заголовка Content-Type

    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        // общий для всех хендлеров метод
        // для отправки ответа с телом в формате JSON
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, 0);

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }

    }

    protected void sendNoContent(HttpExchange ex) throws java.io.IOException {
        // общий для всех хендлеров метод
        // для отправки ответа без тела и кодом 204
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(204, -1);
    }

    protected void sendMethodNotAllowed(HttpExchange ex) throws java.io.IOException {
        // общий для всех хендлеров метод
        // для отправки ответа без тела и кодом 405
        ex.getResponseHeaders().set("Content-Type", CT_JSON);

        // Сервер ОБЯЗАН сгенерировать поле заголовка Allow в ответе с кодом 405,
        // которое содержит список текущих доступных методов ресурса.
        ex.getResponseHeaders().set("Allow", String.join(", ", MoviesServer.ALLOWED_METHODS));

        // TODO ошибки возвращают объект с полем `error` (и при необходимости `details`)
        ex.sendResponseHeaders(405, -1);
    }
}
