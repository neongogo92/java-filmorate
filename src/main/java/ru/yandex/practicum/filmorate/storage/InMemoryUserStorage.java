package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
  private static long id = 1;
  private final Map<Long, User> idToUser = new HashMap<>();

  @Override
  public User add(User user) {
    idToUser.put(user.getId(), user);
    return user;
  }

  @Override
  public User update(User user) {
    idToUser.put(user.getId(), user);
    return user;
  }

  @Override
  public void remove(Long id) {
    idToUser.remove(id);
  }

  @Override
  public List<User> findAll() {

    return new ArrayList<>(idToUser.values());
  }

  @Override
  public User findById(Long id) {
    return idToUser.get(id);
  }

  @Override
  public long getNextId() {
    return id++;
  }
}
