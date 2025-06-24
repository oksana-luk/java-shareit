package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAllUsers();

    Optional<User> findUserById(long userId);

    User addUser(User user);

    User updateUser(User user);

    boolean deleteUserById(long userId);
}
