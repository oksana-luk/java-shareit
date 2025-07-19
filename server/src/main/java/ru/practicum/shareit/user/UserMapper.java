package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static User mapToUser(NewUserRequest newUserRequest) {
        User user = new User();
        user.setName(newUserRequest.getName());
        user.setEmail(newUserRequest.getEmail());
        return user;
    }

    public static UserDto mapToUserDto(User user) {
        return UserDto.builder().id(user.getId()).name(user.getName()).email(user.getEmail()).build();
    }

    public static void updateUserFields(User user, UpdateUserRequest updateUserRequest) {
        if (updateUserRequest.hasName()) {
            user.setName(updateUserRequest.getName());
        }
        if (updateUserRequest.hasEmail()) {
            user.setEmail(updateUserRequest.getEmail());
        }
    }
}
