package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.NewRequestDto;

@Component
public class RequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public RequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getRequest(long userId, long requestId) {
        return get("/" + requestId, userId);
    }


    public ResponseEntity<Object> createRequest(long userId, NewRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getRequestsByRequestor(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAll(long userId) {
        return get("/all", userId);
    }
}
