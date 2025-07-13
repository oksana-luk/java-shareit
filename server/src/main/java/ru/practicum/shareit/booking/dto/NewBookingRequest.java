package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewBookingRequest {
    private Long itemId;
    private String start;
    private String end;
}
