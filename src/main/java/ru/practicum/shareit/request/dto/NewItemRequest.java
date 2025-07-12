package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewItemRequest {
    @NotBlank(message = "Name should not be empty")
    private String description;
}
