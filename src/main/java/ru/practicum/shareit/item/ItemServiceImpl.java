package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UnacceptableValueException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
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
        return itemStorage.findAllByOwner(owner).stream()
                .map(item -> ItemMapper.mapToItemDto(item,
                        bookingRepository.findTop1ByItemIdAndStartTimeBeforeAndStateOrderByStartTimeDesc(item.getId(),
                                LocalDateTime.now(), BookingState.APPROVED).orElse(new Booking()),
                        bookingRepository.findTop1ByItemIdAndStartTimeAfterAndStateOrderByStartTimeAsc(item.getId(),
                                LocalDateTime.now(), BookingState.APPROVED).orElse(new Booking()),
                        commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId()).stream()
                                .map(CommentMapper::mapToCommentDto)
                                .collect(Collectors.toSet())))
                .toList();
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
    public CommentDto addComment(Map<String, String> mapComment, long userId, long itemId) {
        Item item = validateItemNotFound(itemId);
        User author = validateUserNotFound(userId);

        if (item.getOwner().getId() == userId) {
            throw new UnacceptableValueException("The owner should not add comment to item");
        }
        bookingRepository.findAllByUserIdAndItemIdAndEndTimeBeforeOrderByStartTimeDesc(author.getId(), item.getId(), LocalDateTime.now())
                .stream()
                .filter(booking -> booking.getState() == BookingState.APPROVED)
                .findFirst()
                .orElseThrow(() -> new ValidationException("User can not add the comment,he has never booked the item"));
        Comment comment = CommentMapper.mapToComment(mapComment, author, item);

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
}
