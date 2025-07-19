package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.Map;

@Component
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(long userId, NewItemRequest requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getItem(long userId, long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getItemsByOwner() {
        return get("/");
    }

    public ResponseEntity<Object> getItemsByPattern(long userId, String pattern) {
        Map<String, Object> parameters = Map.of(
                "text", pattern
        );
        return get("?text={pattern}", userId, parameters);
    }

    public ResponseEntity<Object> updateItem(long userId, long itemId, UpdateItemRequest requestDto) {
        return patch("/" + itemId, userId, requestDto);
    }

    public ResponseEntity<Object> createComment(long userId, long itemId, CommentDto requestDto) {
        return post("/" + itemId + "/comment", userId, requestDto);
    }
}
