package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.RandomUtils;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRepositoryTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Test
    void save_shouldReturnSavedItemWithIdOwnerRequest() {
        User user = new User(0, "user name", RandomUtils.getRandomEmail());
        User owner = new User(0, "owner name", RandomUtils.getRandomEmail());

        user = userRepository.save(user);
        owner = userRepository.save(owner);

        assertNotNull(user);
        assertNotNull(owner);
        assertTrue(user.getId() > 0);
        assertTrue(owner.getId() > 0);

        ItemRequest request = new ItemRequest(0L, user, "request description", LocalDateTime.now());

        request = itemRequestRepository.save(request);

        assertNotNull(request);

        Item item = new Item(0, owner, "item name", "item description",
                true, null, null, request);

        Item savedItem = itemRepository.save(item);

        assertNotNull(savedItem);
        assertTrue(savedItem.getId() > 0);
        assertThat(item).usingRecursiveComparison().ignoringFields("id").isEqualTo(savedItem);
    }

    @Test
    void findByPattern_shouldReturnListOfItemsByNameIgnoreCase() {
        String name = "SuPeR item";
        String pattern = "sUpEr";
        User owner = new User(0, "user name", RandomUtils.getRandomEmail());

        owner = userRepository.save(owner);

        assertNotNull(owner);
        assertTrue(owner.getId() > 0);

        Item item = new Item(0, owner, name, "item description",
                true, null, null, null);

        Item savedItem = itemRepository.save(item);

        assertNotNull(savedItem);
        assertTrue(savedItem.getId() > 0);
        assertThat(item).usingRecursiveComparison().ignoringFields("id").isEqualTo(savedItem);

        List<Item> itemsByPattern = itemRepository.findItemsByPattern(pattern);
        assertNotNull(itemsByPattern);
        assertThat(itemsByPattern.size() == 1);
        Item itemByPattern = itemsByPattern.getFirst();
        assertThat(itemByPattern).usingRecursiveComparison().isEqualTo(savedItem);
    }

    @Test
    void findByPattern_shouldReturnListOfItemsByDescriptionIgnoreCase() {
        String description = "sUpEr item";
        String pattern = "SuPeR";
        User owner = new User(0, "user name", RandomUtils.getRandomEmail());

        owner = userRepository.save(owner);

        assertNotNull(owner);
        assertTrue(owner.getId() > 0);

        Item item = new Item(0, owner, "name", description,
                true, null, null, null);

        Item savedItem = itemRepository.save(item);

        assertNotNull(savedItem);
        assertTrue(savedItem.getId() > 0);
        assertThat(item).usingRecursiveComparison().ignoringFields("id").isEqualTo(savedItem);

        List<Item> itemsByPattern = itemRepository.findItemsByPattern(pattern);
        assertNotNull(itemsByPattern);
        assertThat(itemsByPattern.size() == 1);
        Item itemByPattern = itemsByPattern.getFirst();
        assertThat(itemByPattern).usingRecursiveComparison().isEqualTo(savedItem);
    }

    @Test
    void findByPattern_shouldReturnListOfApprovedItems() {
        String description = "sUpEr item";
        String pattern = "SuPeR";
        boolean approved = false;
        User owner = new User(0, "user name", RandomUtils.getRandomEmail());

        owner = userRepository.save(owner);

        assertNotNull(owner);
        assertTrue(owner.getId() > 0);

        Item item = new Item(0, owner, "name", description,
                approved, null, null, null);

        Item savedItem = itemRepository.save(item);

        assertNotNull(savedItem);
        assertTrue(savedItem.getId() > 0);
        assertThat(item).usingRecursiveComparison().ignoringFields("id").isEqualTo(savedItem);

        List<Item> itemsByPattern = itemRepository.findItemsByPattern(pattern);
        assertNotNull(itemsByPattern);
        assertThat(itemsByPattern.isEmpty());
    }

    @Test
    void findAllByItemRequestInAndAvailableTrue_shouldReturnListOfApprovedItemsCreatedOnRequest() {
        boolean approved = true;
        User user = new User(0, "user name", RandomUtils.getRandomEmail());
        User owner = new User(0, "owner name", RandomUtils.getRandomEmail());

        user = userRepository.save(user);
        owner = userRepository.save(owner);

        assertNotNull(user);
        assertNotNull(owner);
        assertTrue(user.getId() > 0);
        assertTrue(owner.getId() > 0);

        ItemRequest request = new ItemRequest(0L, user, "request description", LocalDateTime.now());

        request = itemRequestRepository.save(request);

        assertNotNull(request);
        assertThat(request.getId() > 0);

        Item approvedItem = new Item(0, owner, "item name", "item description",
                approved, null, null, request);
        Item notApprovedItem = new Item(0, owner, "item name", "item description",
                !approved, null, null, request);

        approvedItem = itemRepository.save(approvedItem);
        notApprovedItem = itemRepository.save(notApprovedItem);

        assertNotNull(approvedItem);
        assertNotNull(notApprovedItem);
        assertTrue(approvedItem.getId() > 0);
        assertTrue(notApprovedItem.getId() > 0);

        List<Item> items = itemRepository.findAllByItemRequestId(List.of(request.getId()));

        assertNotNull(items);
        assertThat(items.size() == 1);
        Item findedItem = items.getFirst();
        assertThat(findedItem).usingRecursiveComparison().isEqualTo(approvedItem);
    }
}
