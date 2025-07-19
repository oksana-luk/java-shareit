package ru.practicum.shareit.request;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.NewRequestDto;

@Slf4j
@Controller
@RequestMapping(path = "/requests")
@Validated
@RequiredArgsConstructor
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @RequestBody NewRequestDto requestDto) {
        log.info("Creating request {}, userId={}", requestDto, userId);
        return requestClient.createRequest(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getRequestsByRequestor(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Getting requests by requestor, requestorId={}", userId);
        return requestClient.getRequestsByRequestor(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Getting all requests, userId={}", userId);
        return requestClient.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @Positive @PathVariable(name = "requestId") long requestId) {
        log.info("Getting request, requestId={}, userId={}", userId, requestId);
        return requestClient.getRequest(userId, requestId);
    }
}
