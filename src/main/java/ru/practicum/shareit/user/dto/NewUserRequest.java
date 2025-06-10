package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewUserRequest {
    @NotBlank(message = "Name should not be empty")
    private String name;

    @NotNull
    @Email(message = "Invalid email format")
    private String email;
}
