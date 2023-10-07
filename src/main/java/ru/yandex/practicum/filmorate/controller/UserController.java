package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping
  public List<User> findAll() {
    return userService.findAll();
  }

  @PostMapping
  public User add(@Valid @RequestBody User user) {
    return userService.add(user);
  }

  @PutMapping
  public User update(@Valid @RequestBody User user) {
    return userService.update(user);
  }

  @GetMapping("/{id}")
  public User findById(@PathVariable Long id) {
    return userService.findById(id);
  }

  @PutMapping("/{id}/friends/{friendId}")
  public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
    userService.addFriend(id, friendId);
  }

  @DeleteMapping("/{id}/friends/{friendId}")
  public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
    userService.deleteFriend(id, friendId);
  }

  @GetMapping("/{id}/friends")
  public List<User> getFriends(@PathVariable Long id) {
    return userService.getFriends(id);
  }

  @GetMapping("/{id}/friends/common/{otherId}")
  public List<User> getMutualFriends(@PathVariable Long id, @PathVariable Long otherId) {
    return userService.getMutualFriends(id, otherId);
  }
}
