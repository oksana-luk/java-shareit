package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Slf4j
@Service
public class ItemInMemoryStorage implements ItemStorage {
    private static final Map<Long, Item> items = new HashMap<>();
    private static long COUNTER = 0L;

    @Override
    public Optional<Item> findItemById(long itemId) {
        Item item = items.get(itemId);
        return Optional.of(new Item(item.getId(), item.getOwner(), item.getName(), item.getDescription(),
                item.isAvailable(), null, null));
    }

    @Override
    public Collection<Item> findAllItemsByUserId(long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId() == userId)
                .toList();
    }

    @Override
    public Item addItem(Item item) {
        item.setId(getNext());
        items.put(item.getId(), new Item(item.getId(), item.getOwner(),  item.getName(), item.getDescription(),
                            item.isAvailable(), null, null));
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        items.put(item.getId(), new Item(item.getId(), item.getOwner(), item.getName(), item.getDescription(),
                        item.isAvailable(), null, null));
        return item;
    }

    @Override
    public Collection<Item> findItemsByPattern(String pattern) {
        String finalPattern = pattern.toLowerCase();
        return items.values().stream()
                .filter(Item::isAvailable)
                .filter(item -> (item.getName().toLowerCase().contains(finalPattern)
                                    || item.getDescription().toLowerCase().contains(finalPattern)))
                .toList();
    }

    private long getNext() {
        return ++COUNTER;
    }
}
