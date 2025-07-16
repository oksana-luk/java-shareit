package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.RandomUtils;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingRepositoryTest {
    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Test
    void save_shouldReturnSavedBooking() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long bookingId;
        User owner = new User(0L, name, RandomUtils.getRandomEmail());
        User user = new User(0L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        BookingState state = BookingState.APPROVED;

        Item item = new Item(0L, owner, name, description, available, null, null, null);
        Booking booking = new Booking(0L, item, user, state, startDate, endDate);

        User savedUser = userRepository.save(owner);
        assertNotNull(savedUser);
        assertTrue(savedUser.getId() > 0);

        savedUser = userRepository.save(user);
        assertNotNull(savedUser);
        assertTrue(savedUser.getId() > 0);

        Item savedItem = itemRepository.save(item);
        assertNotNull(savedItem);
        assertTrue(savedItem.getId() > 0);

        Booking savedBooking = bookingRepository.save(booking);
        assertNotNull(savedBooking);
        assertTrue(savedBooking.getId() > 0);
        bookingId = savedBooking.getId();

        Optional<Booking> findedBookingOpt = bookingRepository.findById(savedBooking.getId());
        assertThat(!findedBookingOpt.isEmpty());
        Booking findedBooking = findedBookingOpt.get();
        assertEquals(bookingId, findedBooking.getId());
        assertThat(savedBooking).usingRecursiveComparison().isEqualTo(findedBooking);
    }

    @Test
    void findPastBookingsByOwner_shouldReturnPassedBookingsOrderByStartDesc() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long bookingId1;
        long bookingId2;
        User owner = new User(0L, name, RandomUtils.getRandomEmail());
        User user = new User(0L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now().minusDays(8);
        BookingState state = BookingState.APPROVED;

        Item item = new Item(0L, owner, name, description, available, null, null, null);

        Booking booking1 = new Booking(0L, item, user, state, startDate, endDate);
        Booking booking2 = new Booking(0L, item, user, state, startDate.plusDays(3), endDate.plusDays(5));

        User savedUser = userRepository.save(owner);
        assertNotNull(savedUser);
        assertTrue(savedUser.getId() > 0);

        savedUser = userRepository.save(user);
        assertNotNull(savedUser);
        assertTrue(savedUser.getId() > 0);

        Item savedItem = itemRepository.save(item);
        assertNotNull(savedItem);
        assertTrue(savedItem.getId() > 0);

        Booking savedBooking1 = bookingRepository.save(booking1);
        assertNotNull(savedBooking1);
        assertTrue(savedBooking1.getId() > 0);
        bookingId1 = savedBooking1.getId();

        Booking savedBooking2 = bookingRepository.save(booking2);
        assertNotNull(savedBooking2);
        assertTrue(savedBooking2.getId() > 0);
        bookingId2 = savedBooking2.getId();

        List<Booking> findedBookings = bookingRepository.findPastBookingsByOwner(owner.getId(), LocalDateTime.now());
        assertThat(!findedBookings.isEmpty());
        assertThat(findedBookings.size() == 2);
        Booking findedBooking1 = findedBookings.get(0);
        Booking findedBooking2 = findedBookings.get(1);

        //expected order booking2, booking1
        assertThat(findedBooking1).usingRecursiveComparison().isEqualTo(savedBooking2);
        assertThat(findedBooking2).usingRecursiveComparison().isEqualTo(savedBooking1);
    }

    @Test
    void findAllByOwnerWithState_shouldReturnRejectedBookingsOrderByStartDesc() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long bookingId1;
        long bookingId2;
        User owner = new User(0L, name, RandomUtils.getRandomEmail());
        User user = new User(0L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now().minusDays(8);
        BookingState state = BookingState.REJECTED;

        Item item = new Item(0L, owner, name, description, available, null, null, null);

        Booking booking1 = new Booking(0L, item, user, state, startDate, endDate);
        Booking booking2 = new Booking(0L, item, user, state, startDate.plusDays(3), endDate.plusDays(5));
        Booking booking3 = new Booking(0L, item, user, BookingState.APPROVED, startDate.plusDays(5), endDate.plusDays(10));

        User savedUser = userRepository.save(owner);
        assertNotNull(savedUser);
        assertTrue(savedUser.getId() > 0);

        savedUser = userRepository.save(user);
        assertNotNull(savedUser);
        assertTrue(savedUser.getId() > 0);

        Item savedItem = itemRepository.save(item);
        assertNotNull(savedItem);
        assertTrue(savedItem.getId() > 0);

        Booking savedBooking1 = bookingRepository.save(booking1);
        assertNotNull(savedBooking1);
        assertTrue(savedBooking1.getId() > 0);
        bookingId1 = savedBooking1.getId();

        Booking savedBooking2 = bookingRepository.save(booking2);
        assertNotNull(savedBooking2);
        assertTrue(savedBooking2.getId() > 0);
        bookingId2 = savedBooking2.getId();

        Booking savedBooking3 = bookingRepository.save(booking3);
        assertNotNull(savedBooking3);
        assertTrue(savedBooking3.getId() > 0);

        List<Booking> findedBookings = bookingRepository.findAllByOwnerWithState(owner.getId(), state);
        assertThat(!findedBookings.isEmpty());
        assertThat(findedBookings.size() == 2);
        Booking findedBooking1 = findedBookings.get(0);
        Booking findedBooking2 = findedBookings.get(1);

        //expected only booking2, booking1
        assertThat(findedBooking1).usingRecursiveComparison().isEqualTo(savedBooking2);
        assertThat(findedBooking2).usingRecursiveComparison().isEqualTo(savedBooking1);
    }

    @Test
    void findAllCurrentBookingsByUser_shouldReturnListOfCurrentBookings() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long bookingId1;
        long bookingId2;
        User owner = new User(0L, name, RandomUtils.getRandomEmail());
        User user = new User(0L, name, RandomUtils.getRandomEmail());
        LocalDateTime now = LocalDateTime.now();
        BookingState state = BookingState.APPROVED;

        Item item = new Item(0L, owner, name, description, available, null, null, null);

        //past booking
        Booking booking1 = new Booking(0L, item, user, state, now.minusDays(5), now.minusDays(4));
        //current booking
        Booking booking2 = new Booking(0L, item, user, state, now.minusDays(3), now.plusDays(5));
        //future booking
        Booking booking3 = new Booking(0L, item, user, state, now.plusDays(6), now.plusDays(10));

        User savedUser = userRepository.save(owner);
        assertNotNull(savedUser);
        assertTrue(savedUser.getId() > 0);

        savedUser = userRepository.save(user);
        assertNotNull(savedUser);
        assertTrue(savedUser.getId() > 0);

        Item savedItem = itemRepository.save(item);
        assertNotNull(savedItem);
        assertTrue(savedItem.getId() > 0);

        Booking savedBooking1 = bookingRepository.save(booking1);
        assertNotNull(savedBooking1);
        assertTrue(savedBooking1.getId() > 0);
        bookingId1 = savedBooking1.getId();

        Booking savedBooking2 = bookingRepository.save(booking2);
        assertNotNull(savedBooking2);
        assertTrue(savedBooking2.getId() > 0);
        bookingId2 = savedBooking2.getId();

        Booking savedBooking3 = bookingRepository.save(booking2);
        assertNotNull(savedBooking3);
        assertTrue(savedBooking3.getId() > 0);

        List<Booking> findedBookings = bookingRepository.findAllByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(
                user.getId(), LocalDateTime.now(), LocalDateTime.now());
        assertThat(!findedBookings.isEmpty());
        assertThat(findedBookings.size() == 1);
        Booking findedBooking = findedBookings.get(0);

        //expected only booking2
        assertThat(findedBooking).usingRecursiveComparison().isEqualTo(savedBooking2);
    }
}
