package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewItemRequest {
    @NotBlank(message = "Name should not be empty")
    @Size(max = 50, message = "Name should be no longer than 250 characters")
    private String name;

    @NotBlank(message = "Description should not be empty")
    @Size(max = 250, message = "Description should be no longer than 250 characters")
    private String description;

    @NotNull(message = "Available should be not empty")
    private Boolean available;

    private Long requestId;
}
