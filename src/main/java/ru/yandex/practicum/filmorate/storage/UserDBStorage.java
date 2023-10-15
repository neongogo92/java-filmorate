package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component("userDBStorage")
@RequiredArgsConstructor
public class UserDBStorage implements UserStorage {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User add(User user) {
        String query = "insert into \"USER\"(email, login, name, birthday) values(?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(query, new String[] { "id" });
                    ps.setString(1, user.getEmail());
                    ps.setString(2, user.getLogin());
                    ps.setString(3, user.getName());
                    ps.setObject(4, user.getBirthday());

                    return ps;
                },
                keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update(
                "update \"USER\" set email = ?, login = ?, name = ?, birthday = ? where id = ?",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        jdbcTemplate.update("delete from friends where user_id = ?", user.getId());

        jdbcTemplate.batchUpdate(
                "insert into friends(user_id, friend_id, confirm) values(?, ?, true)",
                user.getFriends(),
                50,
                (PreparedStatement ps, Long friend) -> {
                    ps.setLong(1, user.getId());
                    ps.setLong(2, friend);
                });

        return user;
    }

    @Override
    public void remove(Long id) {
        jdbcTemplate.update("delete from \"USER\" where id = ?", id);
    }

    @Override
    public List<User> findAll() {
        String query =
                "SELECT u.id, u.email, u.login, u.name, u.birthday, f.friend_id " +
                        "FROM \"USER\" u " +
                        "LEFT JOIN friends f ON u.id = f.user_id AND f.confirm = true";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
        return mapUsersFromResultSet(rs);
    }

    @Override
    public User findById(Long id) {
        String query =
                "SELECT u.id, u.email, u.login, u.name, u.birthday, f.friend_id " +
                        "FROM \"USER\" u " +
                        "LEFT JOIN friends f ON u.id = f.user_id AND f.confirm = true " +
                        "WHERE u.id = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(query, id);
        List<User> users = mapUsersFromResultSet(rs);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    private List<User> mapUsersFromResultSet(SqlRowSet rs) {
        List<User> users = null;
        while (rs.next()) {
            if (users == null) {
                users = new ArrayList<>();
            }

            long userId = rs.getLong("id");
            Set<Long> friends = users
                    .stream()
                    .filter(u -> u.getId().equals(userId))
                    .findFirst()
                    .map(User::getFriends)
                    .orElse(null);

            if (friends == null) {
                friends = users
                        .stream()
                        .filter(u -> u.getId().equals(rs.getLong("friend_id")))
                        .findFirst()
                        .map(User::getFriends)
                        .orElse(new HashSet<>());
            }

            User user = new User(
                    userId,
                    rs.getString("email"),
                    rs.getString("login"),
                    rs.getString("name"),
                    LocalDate.parse(rs.getString("birthday"), formatter),
                    friends);
            users.add(user);
        }

        return users == null ? List.of() : users;
    }
}
