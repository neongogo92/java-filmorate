package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.CustomExceptions;
import ru.yandex.practicum.filmorate.exception.CustomExceptions.FilmException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
  private final FilmStorage filmStorage;
  private final UserService userService;

  @Autowired
  public FilmService(FilmStorage filmStorage, UserService userService) {
    this.filmStorage = filmStorage;
    this.userService = userService;
  }

  public List<Film> findAll() {
    List<Film> films = filmStorage.findAll();
    log.info("Возвращаем все фильмы. Общее количество: {}", films.size());

    return films;
  }

  public Film add(Film film) {
    long id = filmStorage.getNextId();

    if (film.getId() == null) {
      film.setId(id);
    }

    if (filmStorage.findById(film.getId()) != null) {
      log.error("Фильм с id={} уже существует", film.getId());
      throw new FilmException(String.format("Фильм с id=%s уже существует", film.getId()));
    }

    if (film.getLikes() == null) {
      film.setLikes(new HashSet<>());
    }

    filmStorage.add(film);
    log.info("Сохраняем новый фильм {}", film);

    return film;
  }

  public Film update(Film film) {
    if (filmStorage.findById(film.getId()) == null) {
      log.error("Фильм с id={} не существует", film.getId());
      throw new CustomExceptions.FilmDoesNotExistsException(
          String.format("Фильм с id=%s не существует", film.getId()));
    }

    if (film.getLikes() == null) {
      film.setLikes(new HashSet<>());
    }

    filmStorage.update(film);
    log.info("Обновляем фильм {}", film);

    return film;
  }

  public Film findById(Long id) {
    return getFilm(id);
  }

  public void addLike(Long filmId, Long userId) {
    User user = userService.getUser(userId);
    Film film = getFilm(filmId);

    film.getLikes().add(userId);

    log.info(
        "Пользователю {}(id = {}) нравится фильм {}(id = {})",
        user.getName(),
        user.getId(),
        film.getName(),
        film.getId());
  }

  private Film getFilm(Long filmId) {
    Film film = filmStorage.findById(filmId);

    if (film == null)
      throw new CustomExceptions.FilmDoesNotExistsException(String.format("Фильм с id=%s не существует", filmId));

    return film;
  }

  public void deleteLike(Long filmId, Long userId) {
    User user = userService.getUser(userId);
    Film film = getFilm(filmId);

    film.getLikes().remove(userId);

    log.info(
        "Пользователю {}(id = {}) больше не нравится фильм {}(id = {})",
        user.getName(),
        user.getId(),
        film.getName(),
        film.getId());
  }

  public List<Film> getPopular(String filmsCount) {
    long count = Long.parseLong(filmsCount);

    Comparator<Film> comparator =
            Comparator.comparing(x -> x.getLikes().size(), Comparator.reverseOrder());

    return filmStorage.findAll().stream().sorted(comparator).limit(count).collect(Collectors.toList());
  }
}
