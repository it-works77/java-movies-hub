package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.api.response.ErrorResponse;

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
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
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

        // Сервер ОБЯЗАН сгенерировать поле заголовка Allow в ответе с кодом 405,
        // которое содержит список текущих доступных методов ресурса.
        ex.getResponseHeaders().set("Allow", String.join(", ", MoviesServer.ALLOWED_METHODS));

        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(405, 0);

        ErrorResponse errorResponse = new ErrorResponse("Method Not Allowed",
                "Неподдерживаемый HTTP метод");
        Gson gson = new Gson();
        String errorResponseString = gson.toJson(errorResponse);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(errorResponseString.getBytes());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    protected void sendError(HttpExchange ex, Integer code, String error, String details) throws java.io.IOException {
        // общий для всех хендлеров метод
        // ошибки возвращают объект с полем `error` (и при необходимости `details`)
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(code, 0);

        ErrorResponse errorResponse = new ErrorResponse(error, details);
        Gson gson = new Gson();
        String errorResponseString = gson.toJson(errorResponse);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(errorResponseString.getBytes());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
