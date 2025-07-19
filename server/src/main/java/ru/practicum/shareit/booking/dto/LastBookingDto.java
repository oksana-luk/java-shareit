package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LastBookingDto {
    private Long id;
    private BookerDto booker;
    private String start;
    private String end;
}
