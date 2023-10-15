package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.CustomExceptions;
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
        String sql =
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, r.id AS rating_id, r.name AS rating_name, " +
                        "g.id AS genre_id, g.name AS genre_name, l.user_id " +
                        "FROM film f " +
                        "INNER JOIN ratings r ON f.rating_id = r.id " +
                        "LEFT JOIN films_genres fg ON f.id = fg.film_id " +
                        "LEFT JOIN genres g ON fg.genre_id = g.id " +
                        "LEFT JOIN likes l ON f.id = l.film_id";

        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        Map<Long, Film> filmMap = new HashMap<>();

        while (rs.next()) {
            long filmId = rs.getLong("id");

            if (!filmMap.containsKey(filmId)) {
                Mpa mpa = new Mpa(rs.getLong("rating_id"), rs.getString("rating_name"));
                Set<Genre> genres = new HashSet<>();
                Set<Long> likes = new HashSet<>();
                Film film = new Film(
                        filmId,
                        rs.getString("name"),
                        rs.getString("description"),
                        LocalDate.parse(rs.getString("release_date"), formatter),
                        rs.getLong("duration"),
                        likes,
                        mpa,
                        genres
                );
                filmMap.put(filmId, film);
            }

            Film film = filmMap.get(filmId);

            long genreId = rs.getLong("genre_id");
            if (genreId != 0) {
                Genre genre = new Genre(genreId, rs.getString("genre_name"));
                film.getGenres().add(genre);
            }

            long like = rs.getLong("user_id");
            if (like != 0) {
                film.getLikes().add(like);
            }
        }

        return new ArrayList<>(filmMap.values());
    }

    @Override
    public Film findById(Long id) {
        String sql =
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, r.id AS rating_id, r.name AS rating_name, " +
                        "g.id AS genre_id, g.name AS genre_name, l.user_id " +
                        "FROM film f " +
                        "INNER JOIN ratings r ON f.rating_id = r.id " +
                        "LEFT JOIN films_genres fg ON f.id = fg.film_id " +
                        "LEFT JOIN genres g ON fg.genre_id = g.id " +
                        "LEFT JOIN likes l ON f.id = l.film_id " +
                        "WHERE f.id = ?";

        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, id);
        Film film = null;

        while (rs.next()) {
            if (film == null) {
                Mpa mpa = new Mpa(rs.getLong("rating_id"), rs.getString("rating_name"));
                Set<Genre> genres = new HashSet<>();
                Set<Long> likes = new HashSet<>();
                film = new Film(
                        id,
                        rs.getString("name"),
                        rs.getString("description"),
                        LocalDate.parse(rs.getString("release_date"), formatter),
                        rs.getLong("duration"),
                        likes,
                        mpa,
                        genres
                );
            }

            long genreId = rs.getLong("genre_id");
            if (genreId != 0) {
                Genre genre = new Genre(genreId, rs.getString("genre_name"));
                film.getGenres().add(genre);
            }

            long like = rs.getLong("user_id");
            if (like != 0) {
                film.getLikes().add(like);
            }
        }

        if (film == null) {
            throw new CustomExceptions.FilmDoesNotExistsException(
                    String.format("Фильм с id = %s не существует", id));
        }

        return film;
    }
}
