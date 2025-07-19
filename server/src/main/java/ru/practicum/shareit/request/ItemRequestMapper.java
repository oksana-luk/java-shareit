package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDtoAnswer;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {
    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

    public static ItemRequest mapToItemRequest(NewItemRequest requestNew, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(requestNew.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    public static ItemRequestDto mapToItemRequestDto(ItemRequest request, Set<ItemDtoAnswer> antworts) {
        String registrationDate = dateTimeFormatter.format(request.getCreated());
        return new ItemRequestDto(
             request.getId(),
             UserMapper.mapToUserDto(request.getRequestor()),
             request.getDescription(),
             registrationDate,
             antworts);
    }
}
