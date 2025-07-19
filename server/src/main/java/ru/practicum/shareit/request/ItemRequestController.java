package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto addRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @RequestBody NewItemRequest request) {
        return itemRequestService.addRequest(userId, request);
    }

    @GetMapping
    public List<ItemRequestDto> getRequestsByRequestor(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.getRequestsByRequestor(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @PathVariable(name = "requestId") long requestId) {
        return itemRequestService.getRequest(userId, requestId);
    }
}
