package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/films")
@RequiredArgsConstructor
public class FilmController {
  private final FilmService filmService;

  @PostMapping
  public Film add(@Valid @RequestBody Film film) {
    return filmService.add(film);
  }

  @PutMapping
  public Film update(@Valid @RequestBody Film film) {
    return filmService.update(film);
  }

  @GetMapping
  public List<Film> findAll() {
    return filmService.findAll();
  }

  @GetMapping("/{id}")
  public Film findById(@PathVariable Long id) {
    return filmService.findById(id);
  }

  @PutMapping("/{id}/like/{userId}")
  public void addLike(@PathVariable Long id, @PathVariable Long userId) {
    filmService.addLike(id, userId);
  }

  @DeleteMapping("/{id}/like/{userId}")
  public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
    filmService.deleteLike(id, userId);
  }

  @GetMapping("/popular")
  public List<Film> getPopular(@RequestParam(defaultValue = "10") String count) {
    return filmService.getPopular(count);
  }
}
