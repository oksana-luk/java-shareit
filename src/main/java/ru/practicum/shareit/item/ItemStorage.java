package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {

    Optional<Item> findItemById(long itemId);

    Collection<Item> findAllItemsByUserId(long userId);

    Item addItem(Item item);

    Item updateItem(Item item);

    Collection<Item> findItemsByPattern(String namePattern);
}
