package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

import static ru.practicum.moviehub.http.MoviesServer.ALLOWED_METHODS;

public class MoviesHandler extends BaseHttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();

        if (!ALLOWED_METHODS.contains(method)) {
            sendMethodNotAllowed(ex);
        }
        // TODO fix this stub
        if (method.equalsIgnoreCase("GET")) {
            // Напишите реализацию с использованием метода sendJson
            sendJson(ex, 200, "[]");
        } else {
            sendError(ex, 500, "No handler", "It's not handled at all");

        }
    }
}
