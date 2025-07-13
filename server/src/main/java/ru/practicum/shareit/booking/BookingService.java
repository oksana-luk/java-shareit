package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;

import java.util.List;

public interface BookingService {

    BookingDto addBooking(Long userId, NewBookingRequest newBookingRequest);

    BookingDto getBookingById(Long userId, Long bookingId);

    BookingDto approveBooking(Long userId, Long bookingId, boolean approved);

    List<BookingDto> getAllBookingsByUser(Long userId, BookingStateFilter state);

    List<BookingDto> getAllBookingByOwner(Long ownerId, BookingStateFilter state);
}
