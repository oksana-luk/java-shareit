package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByItemIdOrderByCreatedDesc(Long itemId);

    List<Comment> findAllByAuthorIdOrderByCreatedDesc(Long authorId);

    @Query("select comment " +
            "from Comment as comment " +
            "where comment.item.id = ?1 " +
            "and LOWER(comment.text) like LOWER(CONCAT('%', ?2, '%'))")
    List<Comment> findAllByItemByPattern(Long itemId, String pattern);

    List<Comment> findAllByItemIdInOrderByCreatedDesc(List<Long> itemIds);
}
