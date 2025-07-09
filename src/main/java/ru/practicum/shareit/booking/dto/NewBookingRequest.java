package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewBookingRequest {
    @NotNull
    private Long itemId;
    @NotNull
    private String start;
    @NotNull
    private String end;
}
