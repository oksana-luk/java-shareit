package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CommentMapper {
    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

    public static Comment mapToComment(NewCommentRequest newCommentRequest, User author, Item item) {
        return new Comment(
                null,
                item,
                author,
                newCommentRequest.getText(),
                LocalDateTime.now()
        );
    }

    public static CommentDto mapToCommentDto(Comment comment) {
        String created = comment.getCreated().format(dateTimeFormatter);
        return new CommentDto(
                comment.getId(),
                comment.getAuthor().getName(),
                comment.getText(),
                created);
    }
 }
