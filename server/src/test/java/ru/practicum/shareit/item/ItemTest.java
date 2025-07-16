package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemTest {
    @Test
    void testItemModelConstructorAndGetters() {
        Item item = new Item();
        item.setId(1L);
        assertEquals(1L, item.getId());
        item.toString();
        item.hashCode();
        Item otherItem = new Item();
        otherItem.setId(1L);
        assertTrue(item.equals(otherItem));
    }
}
