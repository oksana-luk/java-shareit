package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewUserRequest {
    private String name;

    private String email;
}
