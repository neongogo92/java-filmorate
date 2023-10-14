package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.CustomExceptions;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MpaDaoImpl implements MpaDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> findAll() {
        return jdbcTemplate.query(
                "select * from ratings order by id",
                (rs, num) -> new Mpa(rs.getLong("id"), rs.getString("name")));
    }

    @Override
    public Mpa findById(Long id) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from ratings where id = ?", id);

        if (sqlRowSet.next()) {
            return new Mpa(sqlRowSet.getLong("id"), sqlRowSet.getString("name"));
        } else {
            throw new CustomExceptions.MpaDoesNotExistsException(
                    String.format("Рейтинг с id = %s не существует", id));
        }
    }
}
