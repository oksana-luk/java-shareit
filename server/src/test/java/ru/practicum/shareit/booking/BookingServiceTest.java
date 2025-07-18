package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.RandomUtils;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.UnacceptableValueException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

    @Mock private ItemRepository itemRepositoryMock;
    @Mock private UserRepository userRepositoryMock;
    @Mock private BookingRepository bookingRepositoryMock;

    @Mock
    private BookingMapper itemMapper;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(userRepositoryMock, itemRepositoryMock, bookingRepositoryMock);
    }

    @Test
    void getBookingById_shouldReturnBooking() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        Booking booking = new Booking(bookingId, item, user, BookingState.WAITING, startDate, endDate);

        when(bookingRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        BookingDto findedBooking = bookingService.getBookingById(owner.getId(), bookingId);

        assertNotNull(findedBooking);
        assertEquals(bookingId, findedBooking.getId());
        assertEquals(itemId, findedBooking.getItem().getId());
        assertEquals(user.getId(), findedBooking.getBooker().getId());
        assertEquals(booking.getState(), findedBooking.getStatus());
        assertEquals(start, findedBooking.getStart());
        assertEquals(end, findedBooking.getEnd());

        verify(bookingRepositoryMock).findById(anyLong());
    }

    @Test
    void getBookingById_shouldThrowExceptionWhenUserIsNotOwnerAndNotBooker() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User booker = new User(2L, name, RandomUtils.getRandomEmail());
        User user = new User(3L, name, RandomUtils.getRandomEmail());

        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        Booking booking = new Booking(bookingId, item, booker, BookingState.WAITING, startDate, endDate);

        when(bookingRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        assertThrows(UnacceptableValueException.class, () -> bookingService.getBookingById(user.getId(), bookingId));

        verify(bookingRepositoryMock).findById(anyLong());
    }

    @Test
    void getAllBookingsByUser_shouldReturnListOfBookingsInStateCurrent() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);
        BookingStateFilter filter = BookingStateFilter.CURRENT;

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        Booking booking = new Booking(bookingId, item, user, BookingState.WAITING, startDate, endDate);

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(bookingRepositoryMock.findAllByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(anyLong(),
                        any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

         Collection<BookingDto> currentBookings = bookingService.getAllBookingsByUser(user.getId(), filter);

        assertNotNull(currentBookings);
        assertEquals(1, currentBookings.size());
        BookingDto currentBooking = currentBookings.stream().findFirst().get();
        assertEquals(booking.getId(), currentBooking.getId());
        assertNotNull(currentBooking);
        assertEquals(itemId, currentBooking.getItem().getId());
        assertEquals(owner.getId(), currentBooking.getItem().getOwnerId());
        assertEquals(user.getId(), currentBooking.getBooker().getId());
        assertEquals(start, currentBooking.getStart());
        assertEquals(end, currentBooking.getEnd());
        assertEquals(booking.getState(), currentBooking.getStatus());

        verify(userRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findAllByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getAllBookingsByUser_shouldReturnListOfBookingsInStateApproved() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);
        BookingStateFilter filter = BookingStateFilter.APPROVED;
        BookingState state = BookingState.APPROVED;

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        Booking booking = new Booking(bookingId, item, user, state, startDate, endDate);

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(bookingRepositoryMock.findAllByUserIdAndStateOrderByStartTimeDesc(anyLong(), any(BookingState.class)))
                .thenReturn(List.of(booking));

        Collection<BookingDto> currentBookings = bookingService.getAllBookingsByUser(user.getId(), filter);

        assertNotNull(currentBookings);
        assertEquals(1, currentBookings.size());
        BookingDto currentBooking = currentBookings.stream().findFirst().get();
        assertEquals(booking.getId(), currentBooking.getId());
        assertNotNull(currentBooking);
        assertEquals(itemId, currentBooking.getItem().getId());
        assertEquals(owner.getId(), currentBooking.getItem().getOwnerId());
        assertEquals(user.getId(), currentBooking.getBooker().getId());
        assertEquals(start, currentBooking.getStart());
        assertEquals(end, currentBooking.getEnd());
        assertEquals(booking.getState(), currentBooking.getStatus());

        verify(userRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findAllByUserIdAndStateOrderByStartTimeDesc(anyLong(), any(BookingState.class));
    }

    @Test
    void getAllBookingsByUser_shouldReturnListOfBookingsInStatePast() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().minusDays(5);
        LocalDateTime endDate = LocalDateTime.now().minusDays(3);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);
        BookingStateFilter filter = BookingStateFilter.PAST;
        BookingState state = BookingState.APPROVED;

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        Booking booking = new Booking(bookingId, item, user, state, startDate, endDate);

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(bookingRepositoryMock.findAllByUserIdAndEndTimeBeforeOrderByStartTimeDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        Collection<BookingDto> currentBookings = bookingService.getAllBookingsByUser(user.getId(), filter);

        assertNotNull(currentBookings);
        assertEquals(1, currentBookings.size());
        BookingDto currentBooking = currentBookings.stream().findFirst().get();
        assertEquals(booking.getId(), currentBooking.getId());
        assertNotNull(currentBooking);
        assertEquals(itemId, currentBooking.getItem().getId());
        assertEquals(owner.getId(), currentBooking.getItem().getOwnerId());
        assertEquals(user.getId(), currentBooking.getBooker().getId());
        assertEquals(start, currentBooking.getStart());
        assertEquals(end, currentBooking.getEnd());
        assertEquals(booking.getState(), currentBooking.getStatus());

        verify(userRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findAllByUserIdAndEndTimeBeforeOrderByStartTimeDesc(anyLong(), any(LocalDateTime.class));
    }


    @Test
    void getAllBookingsByOwner_shouldReturnListOfBookingsInStatePast() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);
        BookingStateFilter filter = BookingStateFilter.PAST;

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        Booking booking = new Booking(bookingId, item, user, BookingState.WAITING, startDate, endDate);

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(bookingRepositoryMock.findPastBookingsByOwner(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        Collection<BookingDto> currentBookings = bookingService.getAllBookingByOwner(owner.getId(), filter);

        assertNotNull(currentBookings);
        assertEquals(1, currentBookings.size());
        BookingDto currentBooking = currentBookings.stream().findFirst().get();
        assertEquals(booking.getId(), currentBooking.getId());
        assertNotNull(currentBooking);
        assertEquals(itemId, currentBooking.getItem().getId());
        assertEquals(owner.getId(), currentBooking.getItem().getOwnerId());
        assertEquals(user.getId(), currentBooking.getBooker().getId());
        assertEquals(start, currentBooking.getStart());
        assertEquals(end, currentBooking.getEnd());
        assertEquals(booking.getState(), currentBooking.getStatus());

        verify(userRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findPastBookingsByOwner(anyLong(), any(LocalDateTime.class));
    }

    @Test
    void getAllBookingsByOwner_shouldReturnListOfBookingsInStateFuture() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);
        BookingStateFilter filter = BookingStateFilter.FUTURE;

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        Booking booking = new Booking(bookingId, item, user, BookingState.WAITING, startDate, endDate);

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(bookingRepositoryMock.findFutureBookingsByOwner(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        Collection<BookingDto> currentBookings = bookingService.getAllBookingByOwner(owner.getId(), filter);

        assertNotNull(currentBookings);
        assertEquals(1, currentBookings.size());
        BookingDto currentBooking = currentBookings.stream().findFirst().get();
        assertEquals(booking.getId(), currentBooking.getId());
        assertNotNull(currentBooking);
        assertEquals(itemId, currentBooking.getItem().getId());
        assertEquals(owner.getId(), currentBooking.getItem().getOwnerId());
        assertEquals(user.getId(), currentBooking.getBooker().getId());
        assertEquals(start, currentBooking.getStart());
        assertEquals(end, currentBooking.getEnd());
        assertEquals(booking.getState(), currentBooking.getStatus());

        verify(userRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findFutureBookingsByOwner(anyLong(), any(LocalDateTime.class));
    }

    @Test
    void getAllBookingsByOwner_shouldReturnListOfBookingsInStateRejected() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);
        BookingStateFilter filter = BookingStateFilter.REJECTED;

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        Booking booking = new Booking(bookingId, item, user, BookingState.WAITING, startDate, endDate);

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(bookingRepositoryMock.findAllByOwnerWithState(anyLong(), any(BookingState.class)))
                .thenReturn(List.of(booking));

        Collection<BookingDto> currentBookings = bookingService.getAllBookingByOwner(owner.getId(), filter);

        assertNotNull(currentBookings);
        assertEquals(1, currentBookings.size());
        BookingDto currentBooking = currentBookings.stream().findFirst().get();
        assertEquals(booking.getId(), currentBooking.getId());
        assertNotNull(currentBooking);
        assertEquals(itemId, currentBooking.getItem().getId());
        assertEquals(owner.getId(), currentBooking.getItem().getOwnerId());
        assertEquals(user.getId(), currentBooking.getBooker().getId());
        assertEquals(start, currentBooking.getStart());
        assertEquals(end, currentBooking.getEnd());
        assertEquals(booking.getState(), currentBooking.getStatus());

        verify(userRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findAllByOwnerWithState(anyLong(), any(BookingState.class));
    }

    @Test
    void getAllBookingsByOwner_shouldReturnListOfBookingsInStateCurrent() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);
        BookingStateFilter filter = BookingStateFilter.CURRENT;

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        Booking booking = new Booking(bookingId, item, user, BookingState.WAITING, startDate, endDate);

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(bookingRepositoryMock.findCurrentBookingsByOwner(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        Collection<BookingDto> currentBookings = bookingService.getAllBookingByOwner(owner.getId(), filter);

        assertNotNull(currentBookings);
        assertEquals(1, currentBookings.size());
        BookingDto currentBooking = currentBookings.stream().findFirst().get();
        assertEquals(booking.getId(), currentBooking.getId());
        assertNotNull(currentBooking);
        assertEquals(itemId, currentBooking.getItem().getId());
        assertEquals(owner.getId(), currentBooking.getItem().getOwnerId());
        assertEquals(user.getId(), currentBooking.getBooker().getId());
        assertEquals(start, currentBooking.getStart());
        assertEquals(end, currentBooking.getEnd());
        assertEquals(booking.getState(), currentBooking.getStatus());

        verify(userRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findCurrentBookingsByOwner(anyLong(), any(LocalDateTime.class));
    }


    @Test
    void addBooking_shouldReturnBooking() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        NewBookingRequest newBookingRequest = new NewBookingRequest(itemId, start, end);
        Booking booking = new Booking(bookingId, item, user, BookingState.WAITING, startDate, endDate);


        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));

        when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingRepositoryMock.findAllCurrentAndFutureBookingForItems(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of());

        when(bookingRepositoryMock.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDto savedBooking = bookingService.addBooking(user.getId(), newBookingRequest);

        assertNotNull(savedBooking);
        assertEquals(bookingId, savedBooking.getId());
        assertEquals(itemId, savedBooking.getItem().getId());
        assertEquals(owner.getId(), savedBooking.getItem().getOwnerId());
        assertEquals(user.getId(), savedBooking.getBooker().getId());
        assertEquals(start, savedBooking.getStart());
        assertEquals(end, savedBooking.getEnd());
        assertEquals(booking.getState(), savedBooking.getStatus());


        verify(userRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findAllCurrentAndFutureBookingForItems(anyList(), any(LocalDateTime.class));
        verify(bookingRepositoryMock).save(any(Booking.class));
    }

    @Test
    void addBooking_shouldThrowExceptionWhenItemNotAvailable() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = false;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        NewBookingRequest newBookingRequest = new NewBookingRequest(itemId, start, end);
        Booking booking = new Booking(bookingId, item, user, BookingState.WAITING, startDate, endDate);


        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));

        when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(item));

        assertThrows(RuntimeException.class, () -> bookingService.addBooking(user.getId(), newBookingRequest));

        verify(userRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock, never()).findAllCurrentAndFutureBookingForItems(anyList(), any(LocalDateTime.class));
        verify(bookingRepositoryMock, never()).save(any(Booking.class));
    }

    @Test
    void addBooking_shouldThrowExceptionPeriodFullOverlapWithOtherBooking() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        long otherBookingId = 14L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        User otherUser = new User(3L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        NewBookingRequest newBookingRequest = new NewBookingRequest(itemId, start, end);
        Booking booking = new Booking(bookingId, item, user, BookingState.WAITING, startDate, endDate);
        Booking otherBooking = new Booking(otherBookingId, item, otherUser, BookingState.WAITING,
                startDate.minusHours(6), endDate.plusHours(6));

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));

        when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingRepositoryMock.findAllCurrentAndFutureBookingForItems(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(otherBooking));

        assertThrows(UnacceptableValueException.class, () -> bookingService.addBooking(user.getId(), newBookingRequest));

        verify(userRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findAllCurrentAndFutureBookingForItems(anyList(), any(LocalDateTime.class));
        verify(bookingRepositoryMock, never()).save(any(Booking.class));
    }

    @Test
    void addBooking_shouldThrowExceptionPeriodStartlOverlapWithOtherBooking() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        long otherBookingId = 14L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        User otherUser = new User(3L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        NewBookingRequest newBookingRequest = new NewBookingRequest(itemId, start, end);
        Booking booking = new Booking(bookingId, item, user, BookingState.WAITING, startDate, endDate);
        Booking otherBooking = new Booking(otherBookingId, item, otherUser, BookingState.WAITING,
                startDate.plusDays(1), endDate.plusDays(1));

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));

        when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingRepositoryMock.findAllCurrentAndFutureBookingForItems(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(otherBooking));

        assertThrows(UnacceptableValueException.class, () -> bookingService.addBooking(user.getId(), newBookingRequest));

        verify(userRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findAllCurrentAndFutureBookingForItems(anyList(), any(LocalDateTime.class));
        verify(bookingRepositoryMock, never()).save(any(Booking.class));
    }

    @Test
    void addBooking_shouldThrowExceptionPeriodEndlOverlapWithOtherBooking() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        long otherBookingId = 14L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        User otherUser = new User(3L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        NewBookingRequest newBookingRequest = new NewBookingRequest(itemId, start, end);
        Booking booking = new Booking(bookingId, item, user, BookingState.WAITING, startDate, endDate);
        Booking otherBooking = new Booking(otherBookingId, item, otherUser, BookingState.WAITING,
                startDate.minusDays(1), endDate.minusDays(1));

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));

        when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingRepositoryMock.findAllCurrentAndFutureBookingForItems(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(otherBooking));

        assertThrows(UnacceptableValueException.class, () -> bookingService.addBooking(user.getId(), newBookingRequest));

        verify(userRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findAllCurrentAndFutureBookingForItems(anyList(), any(LocalDateTime.class));
        verify(bookingRepositoryMock, never()).save(any(Booking.class));
    }


    @Test
    void addBooking_shouldThrowExceptionPeriodOverlapByStartWithOtherBooking() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        long otherBookingId = 14L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        User otherUser = new User(3L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime otherStartDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        LocalDateTime otherEndDate = LocalDateTime.now().plusDays(3);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        NewBookingRequest newBookingRequest = new NewBookingRequest(itemId, start, end);
        Booking booking = new Booking(bookingId, item, user, BookingState.WAITING, startDate, endDate);
        Booking otherBooking = new Booking(otherBookingId, item, otherUser, BookingState.WAITING,
                otherStartDate, otherEndDate);

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));

        when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingRepositoryMock.findAllCurrentAndFutureBookingForItems(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(otherBooking));

        assertThrows(UnacceptableValueException.class, () -> bookingService.addBooking(user.getId(), newBookingRequest));

        verify(userRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findAllCurrentAndFutureBookingForItems(anyList(), any(LocalDateTime.class));
        verify(bookingRepositoryMock, never()).save(any(Booking.class));
    }

    @Test
    void approveBooking_shouldReturnApprovedBooking() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);
        BookingState state = BookingState.WAITING;
        boolean approved = true;

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        NewBookingRequest newBookingRequest = new NewBookingRequest(itemId, start, end);
        Booking booking = new Booking(bookingId, item, user, state, startDate, endDate);

        when(bookingRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        when(bookingRepositoryMock.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDto savedBooking = bookingService.approveBooking(owner.getId(), bookingId, approved);

        assertNotNull(savedBooking);
        assertEquals(bookingId, savedBooking.getId());
        assertEquals(itemId, savedBooking.getItem().getId());
        assertEquals(owner.getId(), savedBooking.getItem().getOwnerId());
        assertEquals(user.getId(), savedBooking.getBooker().getId());
        assertEquals(start, savedBooking.getStart());
        assertEquals(end, savedBooking.getEnd());
        assertEquals(BookingState.APPROVED, savedBooking.getStatus());

        verify(bookingRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).save(any(Booking.class));
    }

    @Test
    void approveBooking_shouldThrowWhenApprovedNotOwner() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);
        BookingState state = BookingState.WAITING;
        boolean approved = true;

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        NewBookingRequest newBookingRequest = new NewBookingRequest(itemId, start, end);
        Booking booking = new Booking(bookingId, item, user, state, startDate, endDate);

        when(bookingRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        assertThrows(UnacceptableValueException.class, () -> bookingService.approveBooking(user.getId(), bookingId, approved));

        verify(bookingRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock, never()).save(any(Booking.class));
    }

    @Test
    void approveBooking_shouldThrowWhenCurrentStateIsNotWaiting() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);
        BookingState state = BookingState.CANCELED;
        boolean approved = true;

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        NewBookingRequest newBookingRequest = new NewBookingRequest(itemId, start, end);
        Booking booking = new Booking(bookingId, item, user, state, startDate, endDate);

        when(bookingRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.approveBooking(owner.getId(), bookingId, approved));

        verify(bookingRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock, never()).save(any(Booking.class));
    }

    @Test
    void approveBooking_shouldReturnRejectedBookingWhenApprovedIsFalse() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long bookingId = 15L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        LocalDateTime startDate = LocalDateTime.now().plusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        String start = dateTimeFormatter.format(startDate);
        String end = dateTimeFormatter.format(endDate);
        BookingState state = BookingState.WAITING;
        boolean approved = false;

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        NewBookingRequest newBookingRequest = new NewBookingRequest(itemId, start, end);
        Booking booking = new Booking(bookingId, item, user, state, startDate, endDate);

        when(bookingRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        when(bookingRepositoryMock.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDto savedBooking = bookingService.approveBooking(owner.getId(), bookingId, approved);

        assertNotNull(savedBooking);
        assertEquals(bookingId, savedBooking.getId());
        assertEquals(itemId, savedBooking.getItem().getId());
        assertEquals(owner.getId(), savedBooking.getItem().getOwnerId());
        assertEquals(user.getId(), savedBooking.getBooker().getId());
        assertEquals(start, savedBooking.getStart());
        assertEquals(end, savedBooking.getEnd());
        assertEquals(BookingState.REJECTED, savedBooking.getStatus());

        verify(bookingRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).save(any(Booking.class));
    }
}
