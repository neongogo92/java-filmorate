package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Set;

public interface FilmStorage {
  Film add(Film film);

  Film update(Film film);

  void remove(Long id);

  List<Film> findAll();

  Film findById(Long id);
}
