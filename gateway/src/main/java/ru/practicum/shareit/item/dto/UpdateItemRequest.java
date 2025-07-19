package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {
    @Size(max = 50, message = "Name should be no longer than 250 characters")
    private String name;

    @Size(max = 250, message = "Description should be no longer than 250 characters")
    private String description;

    private Boolean available;
}
