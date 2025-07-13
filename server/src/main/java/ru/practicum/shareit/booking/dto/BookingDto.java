package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.item.dto.ItemDtoAnswer;

@Data
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private ItemDtoAnswer item;
    private BookerDto booker;
    private BookingState status;
    private String start;
    private String end;
}
