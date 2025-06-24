package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UnacceptableUserException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;

    @Override
    public ItemDto getItemById(long itemId) {
        Item item = validateItemNotFound(itemId);
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public Collection<ItemDto> getItemsByUserId(long userId) {
        userService.findUserById(userId);
        return itemStorage.findAllItemsByUserId(userId).stream()
                .map(ItemMapper::mapToItemDto)
                .toList();
    }

    @Override
    public Collection<ItemDto> getItemsByPattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return List.of();
        } else {
            return itemStorage.findItemsByPattern(pattern).stream()
                    .map(ItemMapper::mapToItemDto)
                    .toList();
        }
    }

    @Override
    public ItemDto addItem(long userId, NewItemRequest newItemRequest) {
        userService.findUserById(userId);
        Item item = ItemMapper.mapToItem(newItemRequest);
        item.setOwner(userId);
        return ItemMapper.mapToItemDto(itemStorage.addItem(item));
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, UpdateItemRequest updateItemRequest) {
        Item item = validateItemNotFound(itemId);
        if (item.getOwner() != userId) {
            String message = String.format("The user with id %s is not the owner of item", userId);
            log.warn("The process validation id of owner of item ended with an error. {}", message);
            throw new UnacceptableUserException(message);
        }
        ItemMapper.updateItemFields(item, updateItemRequest);
        return ItemMapper.mapToItemDto(itemStorage.updateItem(item));
    }

    private Item validateItemNotFound(long itemId) {
        Optional<Item> itemOpt = itemStorage.findItemById(itemId);
        if (itemOpt.isEmpty()) {
            String message = String.format("The service did not find item by id %s", itemId);
            log.warn("The process validation id of item ended with an error. {}", message);
            throw new NotFoundException(message);
        } else {
            log.debug("Validation item id is successfully ended.");
            return itemOpt.get();
        }
    }
}
