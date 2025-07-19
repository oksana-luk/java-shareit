package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto addRequest(Long userId, NewItemRequest request);

    List<ItemRequestDto> getRequestsByRequestor(long userId);

    List<ItemRequestDto> getAll(long userId);

    ItemRequestDto getRequest(long userId, long requestId);
}
