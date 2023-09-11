package ru.yandex.practicum.filmorate.exception;

public class FilmException extends RuntimeException {
    public FilmException(String error) {
        super(error);
    }
}