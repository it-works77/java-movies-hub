package ru.practicum.moviehub.api.response;

public class ErrorResponse {
    private String error; // короткое описание ошибки, например, Ошибка валидации.
    private String details; // массив строк с деталями проблемы

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
