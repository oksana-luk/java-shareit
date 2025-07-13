package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.DuplicateEmailException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Slf4j
@Component
public class UserInMemoryStorage implements UserStorage {
    private static final Map<Long, User> users = new HashMap<>();
    private static final Set<String> emails = new HashSet<>();
    private static long COUNTER = 0;

    @Override
    public Collection<User> findAllUsers() {
        return users.values();
    }

    @Override
    public Optional<User> findUserById(long userId) {
        User user = users.get(userId);
        if (user == null) {
            return Optional.empty();
        } else {
            return Optional.of(new User(user.getId(), user.getName(), user.getEmail()));
        }
    }

    @Override
    public User addUser(User user) {
        if (emails.add(user.getEmail())) {
            user.setId(getNext());
            users.put(user.getId(), new User(user.getId(), user.getName(), user.getEmail()));
            return user;
        } else {
            String message = String.format("The service consist the email %s", user.getEmail());
            log.warn("The process validation user ended with an error. {}", message);
            throw new DuplicateEmailException(message);
        }
    }

    @Override
    public User updateUser(User user) {
        String newEmail = user.getEmail();
        String oldEmail = users.get(user.getId()).getEmail();
        if (!newEmail.equals(oldEmail) && !emails.add(newEmail)) {
            String message = String.format("The service consist the email %s", newEmail);
            log.warn("The process validation of user ended with an error. {}", message);
            throw new DuplicateEmailException(message);
        }
        users.put(user.getId(), new User(user.getId(), user.getName(), user.getEmail()));
        return user;
    }

    @Override
    public boolean deleteUserById(long userId) {
        return users.remove(userId) != null;
    }

    private static long getNext() {
        return ++COUNTER;
    }
}
