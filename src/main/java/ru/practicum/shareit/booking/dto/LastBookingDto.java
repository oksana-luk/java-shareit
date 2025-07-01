package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LastBookingDto {
    private Long id;
    @NotNull
    private BookerDto booker;
    private String start;
    private String end;
}
