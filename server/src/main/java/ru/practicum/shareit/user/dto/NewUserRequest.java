package ru.practicum.shareit.user.dto;

import lombok.Data;

@Data
public class NewUserRequest {
    //@NotBlank(message = "Name should not be empty")
    private String name;

    //@NotNull
    //@Email(message = "Invalid email format")
    private String email;
}
