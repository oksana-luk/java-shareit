package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;

import java.util.Collection;
import java.util.Map;

public interface ItemService {
    ItemDto getItemById(long itemId);

    Collection<ItemDto> getItemsByOwnerId(long userId);

    Collection<ItemDto> getItemsByPattern(String pattern);

    ItemDto addItem(long userId, NewItemRequest newItemRequest);

    ItemDto updateItem(long userId, long itemId, UpdateItemRequest updateItemRequest);

    CommentDto addComment(Map<String, String> comment, long userId, long itemId);
}
