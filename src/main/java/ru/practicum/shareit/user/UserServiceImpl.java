package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
        log.debug("User service. Bean UserStorage created.");
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        log.debug("Method get all users in User service.");
        return userStorage.findAllUsers().stream()
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
    public UserDto saveUser(NewUserRequest newUserRequest) {
        log.debug("Method save user by id in User service.");
        User user = UserMapper.mapToUser(newUserRequest);
        return UserMapper.mapToUserDto(userStorage.addUser(user));
    }

    @Override
    public UserDto updateUser(UpdateUserRequest updateUserRequest, long userId) {
        log.debug("Method update user by id in User service.");
        User user = validateUserNotFound(userId);
        UserMapper.updateUserFields(user, updateUserRequest);
        return UserMapper.mapToUserDto(userStorage.updateUser(user));
    }

    @Override
    public void deleteUserById(long userId) {
        log.debug("Method delete user by id in User service.");
        if (!userStorage.deleteUserById(userId)) {
            String message = String.format("The service did not find user by id %s", userId);
            log.warn("The process deletion of user ended with an error. {}", message);
            throw new NotFoundException(message);
        }
    }

    private User validateUserNotFound(long userId) {
        Optional<User> userOpt = userStorage.findUserById(userId);
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
