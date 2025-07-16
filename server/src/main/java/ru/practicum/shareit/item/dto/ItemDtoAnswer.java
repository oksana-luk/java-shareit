package ru.practicum.shareit.item.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ItemDtoAnswer {
    private Long id;
    private String name;
    private Long ownerId;
}
