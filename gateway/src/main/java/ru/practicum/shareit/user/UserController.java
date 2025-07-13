package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.CreateUserRequestDto;
import ru.practicum.shareit.user.dto.UpdateUserRequestDto;

@Slf4j
@Controller
@Validated
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody CreateUserRequestDto requestDto) {
        log.info("Creating user {}", requestDto);
        return userClient.createUser(requestDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@Positive @PathVariable long userId) {
        log.info("Get user, userId={}",  userId);
        return userClient.getUser(userId);
    }

    //
    @GetMapping
    public ResponseEntity<Object> findAll() {
        log.info("Get all users.");
        return userClient.getAllUsers();
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@Valid @RequestBody UpdateUserRequestDto updateUserRequest,
                                             @Positive @PathVariable long userId) {
        log.info("Creating user {}, userId={}", updateUserRequest, userId);
        return userClient.updateUser(updateUserRequest, userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@Positive @PathVariable long userId) {
        log.info("Deleting user, userId={}", userId);
        return userClient.deleteUser(userId);
    }

}
