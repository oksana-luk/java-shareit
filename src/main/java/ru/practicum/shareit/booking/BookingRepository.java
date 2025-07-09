package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    //all bookings
    List<Booking> findAllByUserIdOrderByStartTimeDesc(Long userId);

    //past bookings
    List<Booking> findAllByUserIdAndEndTimeBeforeOrderByStartTimeDesc(Long userId, LocalDateTime timeNow);

    List<Booking> findAllByUserIdAndItemIdAndEndTimeBeforeOrderByStartTimeDesc(Long userId, Long itemId, LocalDateTime timeNow);

    //future bookings
    List<Booking> findAllByUserIdAndStartTimeAfterOrderByStartTimeDesc(Long userId, LocalDateTime timeNow);

    //current bookings
    List<Booking> findAllByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(Long userId, LocalDateTime timeNow, LocalDateTime timeNow2);

    //bookings by state
    List<Booking> findAllByUserIdAndStateOrderByStartTimeDesc(Long userId, BookingState state);

    @Query("select booking " +
            "from Booking as booking " +
            "where booking.item.owner.id = ?1 " +
            "and booking.endTime < ?2 " +
            "order by booking.startTime desc")
    List<Booking> findPastBookingsByOwner(Long ownerId, LocalDateTime timeNow);

    @Query("select booking " +
            "from Booking as booking " +
            "where booking.item.owner.id = ?1 " +
            "and booking.startTime > ?2 " +
            "order by booking.startTime desc")
    List<Booking> findFutureBookingsByOwner(Long ownerId, LocalDateTime timeNow);

    @Query("select booking " +
            "from Booking as booking " +
            "where booking.item.owner.id = ?1 " +
            "and booking.startTime < ?2 " +
            "and booking.endTime > ?2 " +
            "order by booking.startTime desc")
    List<Booking> findCurrentBookingsByOwner(Long ownerId, LocalDateTime timeNow);

    @Query("select booking " +
            "from Booking as booking " +
            "where booking.item.owner.id = ?1 " +
            "and booking.state = ?2 " +
            "order by booking.startTime desc")
    List<Booking> findAllByOwnerWithState(Long ownerId, BookingState state);

    //all booking
    List<Booking> findAllByItemOwnerIdOrderByStartTimeDesc(Long ownerId);

    @Query("select booking " +
            "from Booking as booking " +
            "where booking.item in ?1 " +
            "and (booking.startTime >= ?2 " +
            "or (booking.startTime < ?2 " +
            "and booking.endTime > ?2)) " +
            "and booking.state = 'APPROVED' " +
            "order by booking.startTime")
    List<Booking> findAllCurrentAndFutureBookingForItems(List<Item> items, LocalDateTime timeNow);

    //last booking
    Optional<Booking> findTop1ByItemIdAndStartTimeBeforeAndStateOrderByStartTimeDesc(Long itemId, LocalDateTime timeNow, BookingState state);

    //next booking
    Optional<Booking> findTop1ByItemIdAndStartTimeAfterAndStateOrderByStartTimeAsc(Long itemId, LocalDateTime timeNow, BookingState state);

    @Query("select booking " +
            "from Booking as booking " +
            "where booking.item in ?1 " +
            "and booking.state = 'APPROVED' " +
            "order by booking.startTime")
    List<Booking> findAllBookings(List<Item> items, LocalDateTime timeNow);

}
