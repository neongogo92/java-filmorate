package ru.yandex.practicum.filmorate.model;

public class FilmSequence {
    private static long id = 1;

    public static long getNextId() {
        return id++;
    }
}