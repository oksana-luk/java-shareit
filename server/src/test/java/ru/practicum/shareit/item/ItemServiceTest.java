package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.RandomUtils;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UnacceptableValueException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

    @Mock private ItemRepository itemRepositoryMock;
    @Mock private UserRepository userRepositoryMock;
    @Mock private BookingRepository bookingRepositoryMock;
    @Mock private CommentRepository commentRepositoryMock;
    @Mock private ItemRequestRepository itemRequestRepositoryMock;

    @Mock
    private ItemMapper itemMapper;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepositoryMock, userRepositoryMock,
                bookingRepositoryMock, commentRepositoryMock, itemRequestRepositoryMock);
    }

    @Test
    void getItemById_shouldReturnItemWithCommentsWithoutLastNextBookings() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long requestId = 11L;
        long commentId = 13L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        ItemRequest request = new ItemRequest(requestId, null, null, null);

        Item item = new Item(itemId, owner, name, description, available, null, null, request);
        Comment comment = new Comment(commentId, item, user, "comment text", LocalDateTime.now());

        when(itemRepositoryMock.findById(itemId))
                .thenReturn(Optional.of(item));

        when(commentRepositoryMock.findAllByItemIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(comment));

        ItemDto findedItem = itemService.getItemById(itemId);

        assertNotNull(findedItem);
        assertEquals(itemId, findedItem.getId());
        assertEquals(name, findedItem.getName());
        assertEquals(description, findedItem.getDescription());
        assertEquals(available, findedItem.isAvailable());
        assertNull(findedItem.getLastBooking());
        assertNull(findedItem.getNextBooking());
        assertEquals(requestId, findedItem.getItemRequest());
        assertNotNull(findedItem.getComments());
        assertEquals(1, findedItem.getComments().size());
        CommentDto commemtDto = findedItem.getComments().stream().toList().getFirst();

        assertEquals(commentId, commemtDto.getId());
        assertEquals(user.getName(), commemtDto.getAuthorName());
        assertEquals(comment.getText(), commemtDto.getText());
        assertEquals(dateTimeFormatter.format(comment.getCreated()), commemtDto.getCreated());

        verify(itemRepositoryMock).findById(itemId);
        verify(commentRepositoryMock).findAllByItemIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getItemById_shouldThrowExceptionWhenNotFound() {

        when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemById(anyLong()));

          verify(itemRepositoryMock).findById(anyLong());
        verify(commentRepositoryMock, never()).findAllByItemIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getItemByOwnerId_shouldReturnItemWithLastBookingAndNextBooking() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long requestId = 11L;
        long commentId = 13L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        ItemRequest request = new ItemRequest(requestId, null, null, null);

        Item item = new Item(itemId, owner, name, description, available, null, null, request);

        Booking lastBooking = new Booking(1L, item, user, BookingState.APPROVED, LocalDateTime.MIN, LocalDateTime.MIN.plusDays(1));
        Booking nextBooking = new Booking(2L, item, user, BookingState.APPROVED, LocalDateTime.MAX.minusDays(1), LocalDateTime.MAX);

        when(userRepositoryMock.findById(anyLong()))
                        .thenReturn(Optional.of(owner));

        when(itemRepositoryMock.findAllByOwner(any(User.class)))
                .thenReturn(List.of(item));
        when(bookingRepositoryMock.findAllApprovedBookings(anyList()))
                .thenReturn(List.of(lastBooking, nextBooking));

        when(commentRepositoryMock.findAllByItemIdInOrderByCreatedDesc(anyList()))
                .thenReturn(List.of());

        Collection<ItemDto> findedItems = itemService.getItemsByOwnerId(owner.getId());

        assertNotNull(findedItems);
        assertEquals(1, findedItems.size());
        ItemDto findedItem = findedItems.stream().findFirst().get();
        assertEquals(itemId, findedItem.getId());
        assertEquals(name, findedItem.getName());
        assertEquals(description, findedItem.getDescription());
        assertEquals(available, findedItem.isAvailable());
        assertEquals(lastBooking.getId(), findedItem.getLastBooking().getId());
        assertEquals(dateTimeFormatter.format(lastBooking.getStartTime()), findedItem.getLastBooking().getStart());
        assertEquals(nextBooking.getId(), findedItem.getNextBooking().getId());
        assertEquals(dateTimeFormatter.format(nextBooking.getEndTime()), findedItem.getNextBooking().getEnd());

        assertEquals(requestId, findedItem.getItemRequest());
        assertThat(findedItem.getComments().isEmpty());

        verify(userRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock).findAllByOwner(any(User.class));
        verify(bookingRepositoryMock).findAllApprovedBookings(anyList());
        verify(commentRepositoryMock).findAllByItemIdInOrderByCreatedDesc(anyList());
    }

    @Test
    void getItemByOwnerId_shouldReturnItemWithLastBookingNotEnded() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long requestId = 11L;
        long commentId = 13L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        ItemRequest request = new ItemRequest(requestId, null, null, null);

        Item item = new Item(itemId, owner, name, description, available, null, null, request);


        Booking lastBooking = new Booking(1L, item, user, BookingState.APPROVED,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        Booking nextBooking = new Booking(2L, item, user, BookingState.APPROVED,
                LocalDateTime.MAX.minusDays(1), LocalDateTime.MAX);

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));

        when(itemRepositoryMock.findAllByOwner(any(User.class)))
                .thenReturn(List.of(item));
        when(bookingRepositoryMock.findAllApprovedBookings(anyList()))
                .thenReturn(List.of(lastBooking, nextBooking));

        when(commentRepositoryMock.findAllByItemIdInOrderByCreatedDesc(anyList()))
                .thenReturn(List.of());

        Collection<ItemDto> findedItems = itemService.getItemsByOwnerId(owner.getId());

        assertNotNull(findedItems);
        assertEquals(1, findedItems.size());
        ItemDto findedItem = findedItems.stream().findFirst().get();
        assertEquals(itemId, findedItem.getId());
        assertEquals(name, findedItem.getName());
        assertEquals(description, findedItem.getDescription());
        assertEquals(available, findedItem.isAvailable());
        assertNotNull(findedItem.getLastBooking());
        assertEquals(lastBooking.getId(), findedItem.getLastBooking().getId());
        assertEquals(dateTimeFormatter.format(lastBooking.getStartTime()), findedItem.getLastBooking().getStart());
        assertEquals(nextBooking.getId(), findedItem.getNextBooking().getId());
        assertEquals(dateTimeFormatter.format(nextBooking.getEndTime()), findedItem.getNextBooking().getEnd());

        assertEquals(requestId, findedItem.getItemRequest());
        assertThat(findedItem.getComments().isEmpty());

        verify(userRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock).findAllByOwner(any(User.class));
        verify(bookingRepositoryMock).findAllApprovedBookings(anyList());
        verify(commentRepositoryMock).findAllByItemIdInOrderByCreatedDesc(anyList());
    }

    @Test
    void getItemByOwnerId_shouldThrowExceptionWhenOwnerNotFound() {
        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemsByOwnerId(anyLong()));

        verify(itemRepositoryMock, never()).findAllByOwner(any(User.class));
        verify(commentRepositoryMock, never()).findAllByItemIdOrderByCreatedDesc(anyLong());
        verify(bookingRepositoryMock, never()).findAllApprovedBookings(anyList());
        verify(commentRepositoryMock, never()).findAllByItemIdInOrderByCreatedDesc(anyList());
    }

    @Test
    void getItemsByPattern_shouldReturnCollectionOfItems() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());

        Item item = new Item(itemId, owner, name, description, available, null, null, null);

        when(itemRepositoryMock.findItemsByPattern(anyString()))
                .thenReturn(List.of(item));

        Collection<ItemDto> findedItems = itemService.getItemsByPattern("string");

        assertNotNull(findedItems);
        assertEquals(1, findedItems.size());
        ItemDto findedItem = findedItems.stream().findFirst().get();
        assertEquals(itemId, findedItem.getId());
        assertEquals(name, findedItem.getName());
        assertEquals(description, findedItem.getDescription());
        assertEquals(available, findedItem.isAvailable());

        verify(itemRepositoryMock).findItemsByPattern(anyString());
    }

    @Test
    void addItem_shouldReturnItem() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long itemId = 12L;
        long requestId = 11L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User user = new User(2L, name, RandomUtils.getRandomEmail());
        ItemRequest request = new ItemRequest(requestId, null, null, null);
        NewItemRequest newItemRequest = NewItemRequest.builder().name(name).description(description)
                .available(available).requestId(requestId).build();
        Item item = new Item(itemId, owner, name, description, available, null, null, request);

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));

        when(itemRequestRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(request));

        when(itemRepositoryMock.save(any(Item.class)))
                .thenReturn(item);

        ItemDto savedItemDto = itemService.addItem(user.getId(), newItemRequest);

        assertNotNull(savedItemDto);
        assertEquals(itemId, savedItemDto.getId());
        assertEquals(name, savedItemDto.getName());
        assertEquals(description, savedItemDto.getDescription());
        assertEquals(available, savedItemDto.isAvailable());
        assertEquals(requestId, savedItemDto.getItemRequest());


        verify(userRepositoryMock).findById(anyLong());
        verify(itemRequestRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock).save(any(Item.class));
    }

    @Test
    void addItem_shouldThrowExceptionWhenUserNotFound() {
        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addItem(1L, any(NewItemRequest.class)));

        verify(userRepositoryMock).findById(anyLong());
        verify(itemRequestRepositoryMock, never()).findById(anyLong());
        verify(itemRepositoryMock, never()).save(any(Item.class));
    }

    @Test
    void addItem_shouldThrowExceptionWhenRequestNotFound() {
        String name = "Some Name";
        String description = "Some description";
        boolean available = true;
        long requestId = 11L;
        NewItemRequest newItemRequest = NewItemRequest.builder().name(name).description(description)
                .available(available).requestId(requestId).build();
        User user = new User(2L, name, RandomUtils.getRandomEmail());

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(itemRequestRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addItem(1L, newItemRequest));

        verify(userRepositoryMock).findById(anyLong());
        verify(itemRequestRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock, never()).save(any(Item.class));
    }

    @Test
    void addComment_shouldReturnComment() {
        String name = "Some Name";
        String authorName = "Author name";
        String description = "Some description";
        String text = "dhjfhjhdjfhjdhjfhj";
        boolean available = true;
        long itemId = 12L;
        long requestId = 11L;
        long commentId = 13L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User author = new User(2L, authorName, RandomUtils.getRandomEmail());

        NewCommentRequest newCommentReq = new NewCommentRequest(text);
        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        Booking lastBooking = new Booking(1L, item, author, BookingState.APPROVED, LocalDateTime.MIN, LocalDateTime.MIN.plusDays(1));
        Comment comment = new Comment(commentId, item, author, text, LocalDateTime.now());

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(author));

        when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingRepositoryMock.findAllByUserIdAndItemIdAndEndTimeBeforeOrderByStartTimeDesc(anyLong(), anyLong(),
                any(LocalDateTime.class))).thenReturn(List.of(lastBooking));

        when(commentRepositoryMock.save(any(Comment.class)))
                .thenReturn(comment);

        CommentDto commentDto = itemService.addComment(newCommentReq, author.getId(), item.getId());

        assertNotNull(commentDto);
        assertEquals(commentId, commentDto.getId());
        assertEquals(text, commentDto.getText());
        assertEquals(comment.getAuthor().getName(), commentDto.getAuthorName());


        verify(userRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findAllByUserIdAndItemIdAndEndTimeBeforeOrderByStartTimeDesc(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(commentRepositoryMock).save(any(Comment.class));
    }

    @Test
    void addComment_shouldThrowExceptionWhenUserIsOwner() {
        String name = "Some Name";
        String description = "Some description";
        String text = "dhjfhjhdjfhjdhjfhj";
        boolean available = true;
        long itemId = 12L;
        long commentId = 13L;
        User owner = new User(2L, name, RandomUtils.getRandomEmail());

        NewCommentRequest newCommentReq = new NewCommentRequest(text);
        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        Comment comment = new Comment(commentId, item, owner, text, LocalDateTime.now());

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));

        when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(item));

        assertThrows(UnacceptableValueException.class, () -> itemService.addComment(newCommentReq, owner.getId(), item.getId()));

        verify(userRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock, never()).findAllByUserIdAndItemIdAndEndTimeBeforeOrderByStartTimeDesc(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(commentRepositoryMock, never()).save(any(Comment.class));
    }

    @Test
    void addComment_shouldThrowExceptionWhenUserHasNoEndedBookings() {
        String name = "Some Name";
        String description = "Some description";
        String text = "dhjfhjhdjfhjdhjfhj";
        boolean available = true;
        long itemId = 12L;
        long commentId = 13L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        User author = new User(2L, name, RandomUtils.getRandomEmail());

        NewCommentRequest newCommentReq = new NewCommentRequest(text);
        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        Booking lastBooking = new Booking(1L, item, author, BookingState.APPROVED, LocalDateTime.MIN, LocalDateTime.MIN.plusDays(1));
        Comment comment = new Comment(commentId, item, author, text, LocalDateTime.now());

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(author));

        when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingRepositoryMock.findAllByUserIdAndItemIdAndEndTimeBeforeOrderByStartTimeDesc(anyLong(), anyLong(),
                any(LocalDateTime.class))).thenReturn(List.of());

        assertThrows(ValidationException.class, () -> itemService.addComment(newCommentReq, author.getId(), item.getId()));
        verify(userRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock).findById(anyLong());
        verify(bookingRepositoryMock).findAllByUserIdAndItemIdAndEndTimeBeforeOrderByStartTimeDesc(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(commentRepositoryMock, never()).save(any(Comment.class));
    }

    @Test
    void updateItem_shouldReturnUpdatedUser() {
        String name = "Some Name";
        String newName = "New name";
        String description = "Some description";
        String newDescription = "New description";
        boolean available = true;
        long itemId = 12L;
        User owner = new User(1L, name, RandomUtils.getRandomEmail());

        UpdateItemRequest updateItemRequest = new UpdateItemRequest(newName, newDescription, !available);

        Item item = new Item(itemId, owner, name, description, available, null, null, null);
        Item updatedItem = new Item(itemId, owner, newName, newDescription, !available, null, null, null);

        when(itemRepositoryMock.findById(anyLong())).thenReturn(Optional.of(item));

        when(itemRepositoryMock.save(any(Item.class))).thenReturn(updatedItem);

        ItemDto actuelItem = itemService.updateItem(owner.getId(), itemId, updateItemRequest);

        assertNotNull(actuelItem);
        assertEquals(itemId, actuelItem.getId());
        assertEquals(newName, actuelItem.getName());
        assertEquals(newDescription, actuelItem.getDescription());
        assertEquals(!available, actuelItem.isAvailable());

        verify(itemRepositoryMock).findById(anyLong());
        verify(itemRepositoryMock).save(any(Item.class));
    }
}
