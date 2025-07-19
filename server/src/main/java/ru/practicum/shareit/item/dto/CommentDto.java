package ru.practicum.shareit.item.dto;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String authorName;
    private String text;
    private String created;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommentDto that)) return false;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }
}
