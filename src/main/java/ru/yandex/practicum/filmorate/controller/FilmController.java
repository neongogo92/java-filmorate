package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSequence;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> idToFilms = new HashMap<>();

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        long id = FilmSequence.getNextId();

        if (film.getId() == null) {
            film.setId(id);
        }

        if (idToFilms.containsKey(film.getId())) {
            log.error("Фильм с id={} уже существует", film.getId());
            throw new FilmException(String.format("Пользователь с id=%s уже существует", film.getId()));
        }

        idToFilms.put(film.getId(), film);
        log.info("Сохраняем новый фильм {}", film);

        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (!idToFilms.containsKey(film.getId())) {
            log.error("Фильм с id={} не существует", film.getId());
            throw new FilmException(String.format("Фильм с id=%s не существует", film.getId()));
        }

        idToFilms.put(film.getId(), film);
        log.info("Обновляем фильм {}", film);

        return film;
    }

    @GetMapping
    public List<Film> findAll() {
        log.info("Возвращаем все фильмы. Общее количество: {}", idToFilms.size());

        return new ArrayList<>(idToFilms.values());
    }
}