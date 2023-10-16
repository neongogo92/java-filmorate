package ru.yandex.practicum.filmorate.storage;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
                    PreparedStatement ps = con.prepareStatement(query, new String[]{"id"});
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
            if (!film.getLikes().isEmpty()) {
                List<Long> validLikes = film.getLikes().stream().filter(userId -> userId != 0).collect(Collectors.toList());
                if (!validLikes.isEmpty()) {
                    jdbcTemplate.batchUpdate(
                            "insert into likes(film_id, user_id) values(?, ?)",
                            validLikes,
                            50,
                            (ps, userLike) -> {
                                ps.setLong(1, film.getId());
                                ps.setLong(2, userLike);
                            });

                }
            }
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
        String queryFilm =
                "SELECT f.*, g.id AS genre_id, g.name AS genre_name, r.id AS rating_id, r.name AS rating_name, l.user_id \n" +
                        "FROM film f \n" +
                        "LEFT JOIN films_genres fg ON f.id = fg.film_id \n" +
                        "LEFT JOIN genres g ON fg.genre_id = g.id \n" +
                        "LEFT JOIN ratings r ON f.rating_id = r.id \n" +
                        "LEFT JOIN likes l ON f.id = l.film_id \n" +
                        "WHERE f.id = ?\n" +
                        "ORDER BY f.id, g.id\n";

        List<Film> films = jdbcTemplate.query(queryFilm, (rs, rowNum) -> {
            long filmId = rs.getLong("id");
            String filmName = rs.getString("name");
            String filmDescription = rs.getString("description");
            LocalDate releaseDate = LocalDate.parse(rs.getString("release_date"), formatter);
            long duration = rs.getLong("duration");
            Mpa mpa = new Mpa(rs.getLong("rating_id"), rs.getString("rating_name"));
            Set<Long> likes = new HashSet<>();
            Set<Genre> genres = new HashSet<>();

            do {
                Long genreId = rs.getLong("genre_id");
                String genreName = rs.getString("genre_name");
                if (genreId != 0 && genreName != null) {
                    genres.add(new Genre(genreId, genreName));
                }
                likes.add(rs.getLong("user_id"));
            } while (rs.next() && rs.getLong("id") == filmId);

            return new Film(filmId, filmName, filmDescription, releaseDate, duration, likes, mpa, genres);
        }, id);

        return films.isEmpty() ? null : films.get(0);
    }

//    public Film findById(Long id) {
//        String queryFilm = "select * from film where id = ?";
//
//        try {
//            Film film = jdbcTemplate.queryForObject(queryFilm, (rs, rowNum) -> {
//                long filmId = rs.getLong("id");
//                String filmName = rs.getString("name");
//                String filmDescription = rs.getString("description");
//                LocalDate releaseDate = LocalDate.parse(rs.getString("release_date"), formatter);
//                long duration = rs.getLong("duration");
//
//                List<Long> likes = jdbcTemplate.queryForList("select user_id from likes where film_id = ?", Long.class, filmId);
//
//                List<Genre> genres = jdbcTemplate.query("select g.id, g.name from genres g " +
//                        "inner join films_genres fg on g.id = fg.genre_id " +
//                        "where fg.film_id = ?", new BeanPropertyRowMapper<>(Genre.class), filmId);
//
//                Mpa mpa = jdbcTemplate.queryForObject("select r.id, r.name from ratings r " +
//                        "inner join film f on r.id = f.rating_id " +
//                        "where f.id = ?", new BeanPropertyRowMapper<>(Mpa.class), filmId);
//
//                return new Film(filmId, filmName, filmDescription, releaseDate, duration, new HashSet<>(likes), mpa, new HashSet<>(genres));
//            }, id);
//
//            return film;
//        } catch (EmptyResultDataAccessException ex) {
//            return null;
//        }
//    }

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
