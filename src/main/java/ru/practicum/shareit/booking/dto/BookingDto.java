package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.BookingState;

@Data
@AllArgsConstructor
public class BookingDto {
    private Long id;
    @NotNull
    private ItemDto item;
    private BookerDto booker;
    private BookingState status;
    private String start;
    private String end;
}
