package ru.practicum.moviehub.api.response;

public class MovieResponse {
    private final int id;
    private final String title;

    public MovieResponse(int id, String title, Integer year) {
        this.id = id;
        this.title = title;
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getYear() {
        return year;
    }

    private final Integer year;
}
