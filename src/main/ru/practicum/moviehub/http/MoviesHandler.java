package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class MoviesHandler extends BaseHttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();

        // TODO fix this stub
        if (method.equalsIgnoreCase("GET")) {
            // Напишите реализацию с использованием метода sendJson
            sendJson(ex, 200, "[]");
        }
    }
}
