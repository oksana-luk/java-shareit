package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

@Slf4j
@Controller
@Validated
@RequestMapping(path = "/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @Positive @PathVariable long itemId) {
        log.info("Getting item, itemId={}, userId={}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") long ownerId) {
        log.info("Getting items by owner, ownerId={}", ownerId);
        return itemClient.getItemsByOwner();
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsByName(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @NotBlank @RequestParam String text) {
        log.info("Searching item by pattern, pattern={}", text);
        return itemClient.getItemsByPattern(userId, text);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @Valid @RequestBody NewItemRequest requestDto) {
        log.info("Creating item {}, userId={}", requestDto, userId);
        return itemClient.createItem(userId, requestDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @Positive @PathVariable long itemId,
                                                @Valid @RequestBody UpdateItemRequest requestDto) {
        log.info("Updating item {}, itemId={}, userId={}", requestDto, itemId, userId);
        return itemClient.updateItem(userId, itemId, requestDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @Positive @PathVariable long itemId,
                                                @Valid @RequestBody CommentDto requestDto) {
        log.info("Creating comment {}, itemId={}, userId={}", requestDto, itemId, userId);
        return itemClient.createComment(userId, itemId, requestDto);
    }
}
