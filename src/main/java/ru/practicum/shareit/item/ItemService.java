package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.Collection;

public interface ItemService {
    ItemDto getItemById(long itemId);

    Collection<ItemDto> getItemsByUserId(long userId);

    Collection<ItemDto> getItemsByPattern(String pattern);

    ItemDto addItem(long userId, NewItemRequest newItemRequest);

    ItemDto updateItem(long userId, long itemId, UpdateItemRequest updateItemRequest);
}
