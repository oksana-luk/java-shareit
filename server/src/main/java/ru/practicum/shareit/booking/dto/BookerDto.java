package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BookerDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
}
