package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component("filmDBStorage")
@RequiredArgsConstructor
public class FilmDBStorage implements FilmStorage {
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final JdbcTemplate jdbcTemplate;
  private final MpaDao mpaDao;

  @Override
  public Film add(Film film) {
    String query =
        "insert into film(name, description, release_date, duration, rating_id) values(?, ?, ?, ?, ?)";
    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(
        con -> {
          PreparedStatement ps = con.prepareStatement(query, new String[] {"id"});
          ps.setString(1, film.getName());
          ps.setString(2, film.getDescription());
          ps.setObject(3, film.getReleaseDate());
          ps.setLong(4, film.getDuration());
          ps.setLong(5, film.getMpa().getId());

          return ps;
        },
        keyHolder);

    long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();

    film.setId(filmId);

    if (!film.getGenres().isEmpty()) {
      insertFilmsGenres(film);
    }

    return film;
  }

  private void insertFilmsGenres(Film film) {
    jdbcTemplate.batchUpdate(
        "insert into films_genres(film_id, genre_id) values(?, ?)",
        film.getGenres(),
        50,
        (ps, genre) -> {
          ps.setLong(1, film.getId());
          ps.setLong(2, genre.getId());
        });
  }

  @Override
  public Film update(Film film) {
    jdbcTemplate.update(
        "update film set name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? where id = ?",
        film.getName(),
        film.getDescription(),
        film.getReleaseDate(),
        film.getDuration(),
        film.getMpa().getId(),
        film.getId());

    jdbcTemplate.update("delete from films_genres where film_id = ?", film.getId());

    if (film.getGenres() != null) {
      if (!film.getGenres().isEmpty()) insertFilmsGenres(film);

      Comparator<Genre> comparator = Comparator.comparing(Genre::getId);
      TreeSet<Genre> newSet = new TreeSet<>(comparator);
      newSet.addAll(film.getGenres());

      film.setGenres(newSet);
    }

    jdbcTemplate.update("delete from likes where film_id = ?", film.getId());

    if (film.getLikes() != null)
      if (!film.getLikes().isEmpty())
        jdbcTemplate.batchUpdate(
            "insert into likes(film_id, user_id) values(?, ?)",
            film.getLikes(),
            50,
            (ps, userLike) -> {
              ps.setLong(1, film.getId());
              ps.setLong(2, userLike);
            });

    return film;
  }

  @Override
  public void remove(Long id) {
    jdbcTemplate.update("delete from film where id = ?", id);
  }

  @Override
  public List<Film> findAll() {
    List<Film> films = new ArrayList<>();
    List<FilmLike> filmsLikes =
        jdbcTemplate.query(
            "select film_id, user_id from likes",
            (rs, rownum) -> new FilmLike(rs.getLong(1), rs.getLong(2)));
    List<FilmGenre> filmGenres =
        jdbcTemplate.query(
            "select fg.film_id, g.id, g.name "
                + "from film f, films_genres fg, genres g "
                + "where f.id = fg.film_id "
                + "and fg.genre_id = g.id",
            (rs, rownum) -> new FilmGenre(rs.getLong(1), rs.getLong(2), rs.getString(3)));
    List<Mpa> allMpas = mpaDao.findAll();

    SqlRowSet rs = jdbcTemplate.queryForRowSet("select * from film");

    while (rs.next()) {
      //      films.add(findById(rs.getLong("id")));
      Set<Long> likes =
          filmsLikes.stream()
              .filter(x -> x.filmId.equals(rs.getLong("id")))
              .map(FilmLike::getUserId)
              .collect(Collectors.toSet());
      Set<Genre> genres =
          filmGenres.stream()
              .filter(x -> x.filmId.equals(rs.getLong("id")))
              .map(x -> new Genre(x.getGenreId(), x.genreName))
              .collect(Collectors.toSet());

      Mpa mpa = null;
      Optional<Mpa> mpaOptional =
          allMpas.stream().filter(x -> x.getId().equals(rs.getLong("rating_id"))).findFirst();
      if (mpaOptional.isPresent()) mpa = mpaOptional.get();

      films.add(makeFilm(rs, likes, mpa, genres));
    }

    return films;
  }

  private Film makeFilm(SqlRowSet rs, Set<Long> likes, Mpa mpa, Set<Genre> genres) {
    return new Film(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("description"),
        LocalDate.parse(Objects.requireNonNull(rs.getString("release_date")), formatter),
        rs.getLong("duration"),
        likes,
        mpa,
        genres);
  }

  @Override
  public Film findById(Long id) {
    SqlRowSet rsFilm = jdbcTemplate.queryForRowSet("select * from film where id = ?", id);
    if (rsFilm.next()) {
      String queryLikes = String.format("select user_id from likes where film_id = %s", id);
      String queryGenres =
          String.format(
              "select genre_id, name from films_genres f, genres g where film_id = %s and g.id = f.genre_id",
              id);
      String queryMpa =
          String.format(
              "select rating_id, r.name from film f, ratings r where f.id = %s and r.id = f.rating_id",
              id);

      List<Long> likes = jdbcTemplate.query(queryLikes, (rs, rowNum) -> rs.getLong(1));
      List<Genre> genres =
          jdbcTemplate.query(
              queryGenres, (rs, rowNum) -> new Genre(rs.getLong(1), rs.getString(2)));
      List<Mpa> mpas =
          jdbcTemplate.query(queryMpa, (rs, rowNum) -> new Mpa(rs.getLong(1), rs.getString(2)));

      return makeFilm(rsFilm, new HashSet<>(likes), mpas.get(0), new HashSet<>(genres));
    } else {
      return null;
    }
  }

  @Data
  @AllArgsConstructor
  private static class FilmLike {
    Long filmId;
    Long userId;
  }

  @Data
  @AllArgsConstructor
  private static class FilmGenre {
    Long filmId;
    Long genreId;
    String genreName;
  }
}
