package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.item.dto.ItemDtoAnswer;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class ItemRequestDto {
    private Long id;
    private UserDto requestor;
    private String description;
    private String created;
    private Set<ItemDtoAnswer> items;
}
