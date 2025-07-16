package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateItemRequest {
    private String name;
    private String description;
    private Boolean available;

    public boolean hasName() {
        return ! (name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return ! (description == null || description.isBlank());
    }

    public boolean hasAvailable() {
        return available != null;
    }
}
