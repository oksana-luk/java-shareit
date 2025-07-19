package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UnacceptableValueException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public BookingDto addBooking(Long userId, NewBookingRequest newBookingRequest) {
        User user = getUser(userId);
        Item item = getItem(newBookingRequest.getItemId());
        Booking booking = BookingMapper.mapToBooking(newBookingRequest, item, user);

        validateItemAvailable(item.isAvailable());

        validatePeriodsOverlap(item, booking.getStartTime(), booking.getEndTime());
        return BookingMapper.mapToBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = getBooking(bookingId);
        if (!(booking.getUser().getId() == userId || booking.getItem().getOwner().getId() == userId)) {
            throw new UnacceptableValueException(String.format("Current user with %d is not the owner of item and has got no booking of it", userId));
        }
        return BookingMapper.mapToBookingDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long userId, Long bookingId, boolean approved) {
        Booking booking = getBooking(bookingId);
        validateOwner(booking.getItem().getOwner().getId(), userId);
        if (booking.getState() != BookingState.WAITING) {
            throw new ValidationException(String.format("The status of booking should be WAITING. Current status is %s",
                    booking.getState()));
        }
        booking.setState(approved ? BookingState.APPROVED : BookingState.REJECTED);
        return BookingMapper.mapToBookingDto(bookingRepository.save(booking));
    }

    @Override
    public List<BookingDto> getAllBookingsByUser(Long userId, BookingStateFilter state) {
        User booker = getUser(userId);
        List<Booking> bookings;
        switch (state) {
            case BookingStateFilter.CURRENT:
                bookings = bookingRepository.findAllByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now());
                break;
            case BookingStateFilter.PAST:
                bookings = bookingRepository.findAllByUserIdAndEndTimeBeforeOrderByStartTimeDesc(userId, LocalDateTime.now());
                break;
            case BookingStateFilter.FUTURE:
                bookings = bookingRepository.findAllByUserIdAndStartTimeAfterOrderByStartTimeDesc(userId, LocalDateTime.now());
                break;
            default:
                if (state == BookingStateFilter.WAITING || state == BookingStateFilter.APPROVED
                        || state == BookingStateFilter.REJECTED) {
                    bookings = bookingRepository.findAllByUserIdAndStateOrderByStartTimeDesc(userId, BookingState.valueOf(state.name()));
                } else {
                    bookings = bookingRepository.findAllByUserIdOrderByStartTimeDesc(userId);
                }
        }
        return bookings.stream()
                .map(BookingMapper::mapToBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getAllBookingByOwner(Long ownerId, BookingStateFilter state) {
        User owner = getUser(ownerId);
        List<Booking> bookings;
        switch (state) {
            case BookingStateFilter.CURRENT:
                bookings = bookingRepository.findCurrentBookingsByOwner(ownerId, LocalDateTime.now());
                break;
            case BookingStateFilter.PAST:
                bookings = bookingRepository.findPastBookingsByOwner(ownerId, LocalDateTime.now());
                break;
            case BookingStateFilter.FUTURE:
                bookings = bookingRepository.findFutureBookingsByOwner(ownerId, LocalDateTime.now());
                break;
            default:
                if (state == BookingStateFilter.WAITING || state == BookingStateFilter.APPROVED
                        || state == BookingStateFilter.REJECTED) {
                    bookings = bookingRepository.findAllByOwnerWithState(ownerId, BookingState.valueOf(state.name()));
                } else {
                    bookings = bookingRepository.findAllByItemOwnerIdOrderByStartTimeDesc(ownerId);
                }
        }
        return bookings.stream()
                .map(BookingMapper::mapToBookingDto)
                .toList();
    }

    private Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking with id %d not found", bookingId)));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId)));
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item with id %d not found", itemId)));
    }

    private void validateItemAvailable(boolean isAvailable) {
        if (!isAvailable) {
            throw new RuntimeException("Item is not available");
        }
    }

    private void validateOwner(Long ownerId, Long userId) {
        if (!Objects.equals(ownerId, userId)) {
            throw new UnacceptableValueException(String.format("User with id %d is not the owner of item", userId));
        }
    }

    private void validatePeriodsOverlap(Item item, LocalDateTime startTime, LocalDateTime endTime) {
        List<Item> items = new ArrayList<>();
        items.add(item);
        List<Booking> bookings = bookingRepository.findAllCurrentAndFutureBookingForItems(items, LocalDateTime.now());
        if (bookings.isEmpty()) {
            return;
        }
        boolean isOverlap = bookings.stream()
                .anyMatch(booking -> (startTime.isAfter(booking.getStartTime()) && startTime.isBefore(booking.getEndTime())) ||
                            (endTime.isAfter(booking.getStartTime()) && endTime.isBefore(booking.getEndTime())));
        if (isOverlap) {
            throw new UnacceptableValueException("The item is already booked for this period");
        }
    }
}
