package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;

import java.util.Set;

@Data
public class ItemDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long id;
    private String name;
    private String description;
    private boolean available;
    private Booking lastBooking;
    private Booking nextBooking;
    private Set<CommentDto> comments;
}
