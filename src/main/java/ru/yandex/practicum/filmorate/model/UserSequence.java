package ru.yandex.practicum.filmorate.model;

public class UserSequence {
    private static long id = 1;

    public static long getNextId() {
        return id++;
    }
}
