package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserSequence;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value = "/users")
@Slf4j
public class UserController {
    private final Map<Long, User> idToUser = new HashMap<>();

    @GetMapping
    public List<User> findAll() {
        log.info("Возвращаем всех пользователей. Общее количество: {}", idToUser.size());

        return new ArrayList<>(idToUser.values());
    }

    @PostMapping
    public User add(@Valid @RequestBody User user) {
        long id = UserSequence.getNextId();

        if (user.getId() == null) {
            user.setId(id);
        }

        if (idToUser.containsKey(user.getId())) {
            throw new UserException(String.format("Пользователь с id=%s уже существует", user.getId()));
        }

        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        user.setLogin(user.getLogin().trim());

        log.info("Сохраняем нового пользователя {}", user);
        idToUser.put(user.getId(), user);

        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (!idToUser.containsKey(user.getId())) {
            log.warn("Пользователь с id={} не существует", user.getId());
            throw new UserException(String.format("Пользователя с id=%s не существует", user.getId()));
        }

        log.info("Обновляем данные пользователя с id={}", user.getId());
        idToUser.put(user.getId(), user);

        return user;
    }
}