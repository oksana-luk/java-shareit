package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum BookingStateFilter {
    ALL, WAITING, APPROVED, REJECTED, CANCELED;

    public static Optional<BookingStateFilter> from(String stringState) {
        for (BookingStateFilter state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
