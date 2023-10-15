package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.exception.CustomExceptions;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GenreDaoImpl implements GenreDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> findAll() {
        return jdbcTemplate.query(
                "select * from genres order by id",
                (rs, num) -> new Genre(rs.getLong("id"), rs.getString("name")));
    }

    @Override
    public Genre findById(Long id) {
        try {
            return jdbcTemplate.queryForObject(
                    "select * from genres where id = ?",
                    (rs, num) -> new Genre(rs.getLong("id"), rs.getString("name")),
                    id
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new CustomExceptions.GenreDoesNotExistsException(
                    String.format("Рейтинг с id = %s не существует", id)
            );
        }
    }


}
