package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.CustomExceptions;
import ru.yandex.practicum.filmorate.exception.CustomExceptions.UserException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User add(User user) {
        long id = userStorage.getNextId();

        if (user.getId() == null) {
            user.setId(id);
        }

        if (userStorage.findById(user.getId()) != null) {
            throw new UserException(String.format("Пользователь с id=%s уже существует", user.getId()));
        }

        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        user.setLogin(user.getLogin().trim());

        log.info("Сохраняем нового пользователя {}", user);
        userStorage.add(user);

        return user;
    }

    public List<User> findAll() {
        List<User> users = userStorage.findAll();
        log.info("Возвращаем всех пользователей. Общее количество: {}", users.size());

        return users;
    }

    public User update(User user) {
        if (userStorage.findById(user.getId()) == null) {
            log.error("Пользователь с id={} не существует", user.getId());
            throw new CustomExceptions.UserDoesNotExistsException(
                    String.format("Пользователя с id=%s не существует", user.getId()));
        }

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        log.info("Обновляем данные пользователя с id={}", user.getId());
        userStorage.update(user);

        return user;
    }

    public void addFriend(Long userId, Long friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info(
                "Пользователи {}(id={}) и {}(id={}) теперь друзья",
                user.getName(),
                userId,
                friend.getName(),
                friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info(
                "Пользователи {}(id={}) и {}(id={}) больше не друзья",
                user.getName(),
                userId,
                friend.getName(),
                friendId);
    }

    public User getUser(Long id) {
        User user = userStorage.findById(id);

        if (user == null) {
            throw new CustomExceptions.UserDoesNotExistsException(String.format("Пользователь id=%s не существует", id));
        }
        return user;
    }

    public User findById(Long id) {
        return getUser(id);
    }

    public List<User> getFriends(Long id) {
        List<User> friends = new ArrayList<>();
        User user = getUser(id);

        user.getFriends().forEach(x -> friends.add(userStorage.findById(x)));

        return friends;
    }

    public List<User> getMutualFriends(Long userId, Long otherUserId) {

        User user = getUser(userId);
        User otherUser = getUser(otherUserId);

        if (user.getFriends() == null || otherUser.getFriends() == null) {
            return new ArrayList<>();
        }

        return user.getFriends().stream()
                .filter(id -> otherUser.getFriends().stream().anyMatch(otherId -> otherId.equals(id)))
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }
}
