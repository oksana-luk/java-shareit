package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequestDto {
    private String name;

    @Email(message = "Invalid email format")
    private String email;
}
