package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;

import java.util.Collection;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable long itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public Collection<ItemDto> getItemsOfOwner(@RequestHeader("X-Sharer-User-Id") long ownerId) {
        return itemService.getItemsByOwnerId(ownerId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> getItemsByName(@RequestParam String text) {
        log.debug("Method search by pattern of name. Pattern : {}", text);
        return itemService.getItemsByPattern(text);
    }

    @PostMapping
    public ItemDto addItemByUser(@Valid @RequestBody NewItemRequest newItemRequest,
                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        log.debug("Method add item by user. Header userId is {}", userId);
        return itemService.addItem(userId, newItemRequest);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long itemId,
                              @RequestBody UpdateItemRequest updateItemRequest) {
        return itemService.updateItem(userId, itemId, updateItemRequest);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@Valid @RequestBody Map<String, String> comment,
                                 @RequestHeader("X-Sharer-User-Id") long userId,
                                 @PathVariable long itemId) {
        return itemService.addComment(comment, userId, itemId);
    }
}
