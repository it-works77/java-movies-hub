package ru.practicum.moviehub.api.response;

public class ErrorResponse {
    private final String error; // короткое описание ошибки, например, Ошибка валидации.
    private final String details; // массив строк с деталями проблемы

    public ErrorResponse(String error, String details) {
        this.error = error;
        this.details = details;
    }

    public String getError() {
        return error;
    }

    public String getDetails() {
        return details;
    }
}
