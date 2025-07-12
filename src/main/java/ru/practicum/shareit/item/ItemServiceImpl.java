package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.LastBookingDto;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemStorage;
    private final UserRepository userStorage;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto getItemById(long itemId) {
        Item item = validateItemNotFound(itemId);
        Set<CommentDto> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toSet());
        return ItemMapper.mapToItemDto(item, null, null, comments);
    }

    @Override
    public Collection<ItemDto> getItemsByOwnerId(long ownerId) {
        User owner = validateUserNotFound(ownerId);

        List<Item> items = itemStorage.findAllByOwner(owner);
        List<Booking> allBookings = bookingRepository.findAllBookings(items, LocalDateTime.now());
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        List<Comment> allComments = commentRepository.findAllByItemIdInOrderByCreatedDesc(itemIds);

        Map<Long, List<Booking>> itemsBookings = new HashMap<>();
        for (Booking booking : allBookings) {
            Long itemId = booking.getItem().getId();
            if (!itemsBookings.containsKey(itemId)) {
                itemsBookings.put(itemId, new ArrayList<>());
            }
            List<Booking> curBookings = itemsBookings.get(itemId);
            curBookings.add(booking);
        }

        return items.stream().map(item -> {
            List<Booking> bookings = itemsBookings.get(item.getId());
            return ItemMapper.mapToItemDto(item, getLastBooking(bookings), getNextBooking(bookings),
                    getCommentDtos(item, allComments));
            }).toList();
    }

    @Override
    public Collection<ItemDto> getItemsByPattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return List.of();
        } else {
            return itemStorage.findItemsByPattern(pattern).stream()
                    .map(item -> ItemMapper.mapToItemDto(item, null, null, Set.of()))
                    .toList();
        }
    }

    @Override
    @Transactional
    public ItemDto addItem(long userId, NewItemRequest newItemRequest) {
        Item item = ItemMapper.mapToItem(newItemRequest);
        User user = validateUserNotFound(userId);
        item.setOwner(user);
        if (newItemRequest.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(newItemRequest.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Request not found"));
            item.setItemRequest(itemRequest);
        }

        return ItemMapper.mapToItemDto(itemStorage.save(item), null, null, Set.of());
    }

    @Override
    @Transactional
    public ItemDto updateItem(long userId, long itemId, UpdateItemRequest updateItemRequest) {
        Item item = validateItemNotFound(itemId);
        if (item.getOwner().getId() != userId) {
            String message = String.format("The user with id %s is not the owner of item", userId);
            log.warn("The process validation id of owner of item ended with an error. {}", message);
            throw new UnacceptableValueException(message);
        }
        ItemMapper.updateItemFields(item, updateItemRequest);
        return ItemMapper.mapToItemDto(itemStorage.save(item), null, null, Set.of());
    }

    @Override
    @Transactional
    public CommentDto addComment(NewCommentRequest newCommentRequest, long userId, long itemId) {
        Item item = validateItemNotFound(itemId);
        User author = validateUserNotFound(userId);

        if (item.getOwner().getId() == userId) {
            throw new UnacceptableValueException("The owner should not add comment to item");
        }
        bookingRepository.findAllByUserIdAndItemIdAndEndTimeBeforeOrderByStartTimeDesc(author.getId(), item.getId(), LocalDateTime.now())
                .stream()
                .filter(booking -> booking.getState() == BookingState.APPROVED)
                .findFirst()
                .orElseThrow(() -> new ValidationException("The user has no completed bookings and can't add a comment"));
        Comment comment = CommentMapper.mapToComment(newCommentRequest, author, item);

        return CommentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    private Item validateItemNotFound(long itemId) {
        Optional<Item> itemOpt = itemStorage.findById(itemId);
        if (itemOpt.isEmpty()) {
            String message = String.format("The service did not find item by id %s", itemId);
            log.warn("The process validation id of item ended with an error. {}", message);
            throw new NotFoundException(message);
        } else {
            log.debug("Validation item id is successfully ended.");
            return itemOpt.get();
        }
    }

    private User validateUserNotFound(long userId) {
        Optional<User> userOpt = userStorage.findById(userId);
        if (userOpt.isEmpty()) {
            String message = String.format("The service did not find user by id %s", userId);
            log.warn("The process validation id user id ended with an error. {}", message);
            throw new NotFoundException(message);
        } else {
            log.debug("Validation user id is successfully ended.");
            return userOpt.get();
        }
    }

    public LastBookingDto getLastBooking(List<Booking> bookings) {
        if (bookings == null) {
            return null;
        }
        Optional<Booking> bookingOpt =  bookings.stream()
                .filter(b -> (b.getEndTime().isBefore(LocalDateTime.now()) ||
                        b.getStartTime().isBefore(LocalDateTime.now()) && b.getEndTime().isAfter(LocalDateTime.now())))
                .max(Comparator.comparing(Booking::getEndTime));
        if (bookingOpt.isEmpty()) {
            return null;
        }
        return BookingMapper.mapToLastBookingDto(bookingOpt.get());
    }

    public LastBookingDto getNextBooking(List<Booking> bookings) {
        if (bookings == null) {
            return null;
        }
        Optional<Booking> bookingOpt =  bookings.stream()
                .filter(b -> b.getStartTime().isAfter(LocalDateTime.now()))
                .min(Comparator.comparing(Booking::getStartTime));
        if (bookingOpt.isEmpty()) {
            return null;
        }
        return BookingMapper.mapToLastBookingDto(bookingOpt.get());
    }

    public Set<CommentDto> getCommentDtos(Item item, List<Comment> allComments) {
        return allComments.stream()
                .filter(comment -> comment.getItem() == item)
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toSet());
    }
}



