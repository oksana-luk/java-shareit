package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userStorage;

    public UserServiceImpl(UserRepository userStorage) {
        this.userStorage = userStorage;
        log.debug("User service. Bean UserRepository created.");
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        log.debug("Method get all users in User service.");
        return userStorage.findAll().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    @Override
    public UserDto findUserById(long userId) {
        log.debug("Method find user by id in User service.");
        User user = validateUserNotFound(userId);
        return UserMapper.mapToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto saveUser(NewUserRequest newUserRequest) {
        log.debug("Method save user by id in User service.");
        User user = UserMapper.mapToUser(newUserRequest);
        return UserMapper.mapToUserDto(userStorage.save(user));
    }

    @Override
    @Transactional
    public UserDto updateUser(UpdateUserRequest updateUserRequest, long userId) {
        log.debug("Method update user by id in User service.");
        User user = validateUserNotFound(userId);
        UserMapper.updateUserFields(user, updateUserRequest);
        return UserMapper.mapToUserDto(userStorage.save(user));
    }

    @Override
    @Transactional
    public void deleteUserById(long userId) {
        log.debug("Method delete user by id in User service.");
        User user = validateUserNotFound(userId);
        userStorage.delete(user);
    }

    private User validateUserNotFound(long userId) {
        Optional<User> userOpt = userStorage.findById(userId);
        if (userOpt.isEmpty()) {
            String message = String.format("The service did not find user by id %s", userId);
            log.warn("The process validation id user id ended with an error. {}", message);
            throw new NotFoundException(message);
        } else {
            log.debug("Validation user id is successfully ended.");
            return userOpt.get();
        }
    }
}
