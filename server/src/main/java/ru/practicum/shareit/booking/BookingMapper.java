package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDtoAnswer;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapper {
    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

    public static Booking mapToBooking(NewBookingRequest newBookingRequest, Item item, User user) {
        String start = newBookingRequest.getStart();
        String end = newBookingRequest.getEnd();

        LocalDateTime startTime = LocalDateTime.parse(start, dateTimeFormatter);
        LocalDateTime endTime = LocalDateTime.parse(end, dateTimeFormatter);
        return new Booking(
                null,
                item,
                user,
                BookingState.WAITING,
                startTime,
                endTime);
    }

    public static BookingDto mapToBookingDto(Booking booking) {
        String startTime = booking.getStartTime().format(dateTimeFormatter);
        String endTime = booking.getEndTime().format(dateTimeFormatter);
        return new BookingDto(
                    booking.getId(),
                    new ItemDtoAnswer(booking.getItem().getId(), booking.getItem().getName(), booking.getItem().getOwner().getId()),
                    new BookerDto(booking.getUser().getId()),
                    booking.getState(),
                    startTime,
                    endTime);
    }

    public static LastBookingDto mapToLastBookingDto(Booking booking) {
        String startTime = booking.getStartTime().format(dateTimeFormatter);
        String endTime = booking.getEndTime().format(dateTimeFormatter);
        return new LastBookingDto(
                booking.getId(),
                new BookerDto(booking.getUser().getId()),
                startTime,
                endTime);
    }
}
