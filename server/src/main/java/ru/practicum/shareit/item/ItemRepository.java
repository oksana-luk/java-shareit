package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwner(User user);

    @Query("select itm " +
            "from Item as itm " +
            "where itm.available = true " +
            "and (UPPER(itm.name) like UPPER(CONCAT('%', ?1, '%')) " +
            "or UPPER(itm.description) like UPPER(CONCAT('%', ?1, '%')))")
    List<Item> findItemsByPattern(String pattern);


    @Query("select itm " +
            "from Item as itm " +
            "where itm.itemRequest.id in :ids " +
            "and itm.available = true")
    List<Item> findAllByItemRequestId(@Param("ids")List<Long> itemRequests);
}
