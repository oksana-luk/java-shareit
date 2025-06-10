package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewItemRequest {
    @NotBlank(message = "Name should not be empty")
    private String name;

    @NotBlank(message = "Description should not be empty")
    private String description;

    @NotNull
    private Boolean available;
}
