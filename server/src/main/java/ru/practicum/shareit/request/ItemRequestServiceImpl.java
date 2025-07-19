package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDtoAnswer;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto addRequest(Long userId, NewItemRequest newItemRequestRequest) {
        User requestor = validateUserNotFound(userId);
        ItemRequest request = ItemRequestMapper.mapToItemRequest(newItemRequestRequest, requestor);
        request = itemRequestRepository.save(request);
        return ItemRequestMapper.mapToItemRequestDto(request, Set.of());
    }

    @Override
    public List<ItemRequestDto> getRequestsByRequestor(long userId) {
        validateUserNotFound(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        Map<Long, Set<ItemDtoAnswer>> answersByRequests = getAnswersByRequests(requests);

        return requests.stream().map(request -> {
            Set<ItemDtoAnswer> currentAnswer = answersByRequests.get(request.getId());
            return ItemRequestMapper.mapToItemRequestDto(request, currentAnswer);
        }).toList();
    }

    @Override
    public List<ItemRequestDto> getAll(long userId) {
        validateUserNotFound(userId);
        return itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId).stream()
                .map(itemRequest -> ItemRequestMapper.mapToItemRequestDto(itemRequest, Set.of()))
                .toList();
    }

    @Override
    public ItemRequestDto getRequest(long userId, long itemRequestId) {
        validateUserNotFound(userId);
        ItemRequest itemRequest = validateItemRequestNotFound(itemRequestId);
        Map<Long, Set<ItemDtoAnswer>> answers = getAnswersByRequests(List.of(itemRequest));
        return ItemRequestMapper.mapToItemRequestDto(itemRequest, answers.get(itemRequest.getId()));
    }

    private Map<Long, Set<ItemDtoAnswer>> getAnswersByRequests(List<ItemRequest> requests) {
        Map<Long, Set<ItemDtoAnswer>> requestsAnswer = new HashMap<>();
        List<Long> requestsId = requests.stream().map(request -> request.getId()).toList();
        List<Item> answers = itemRepository.findAllByItemRequestId(requestsId);

        for (Item answer : answers) {
            Long requestId = answer.getItemRequest().getId();
            if (!requestsAnswer.containsKey(requestId)) {
                requestsAnswer.put(requestId, new HashSet<>());
            }
            Set<ItemDtoAnswer> currentAnswer = requestsAnswer.get(requestId);
            currentAnswer.add(ItemMapper.mapToItemDtoShort(answer));
        }
        return requestsAnswer;
    }

    private User validateUserNotFound(long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            String message = String.format("The service did not find user by id %s", userId);
            log.warn("The process validation id user id ended with an error. {}", message);
            throw new NotFoundException(message);
        } else {
            log.debug("Validation user id is successfully ended.");
            return userOpt.get();
        }
    }

    private ItemRequest validateItemRequestNotFound(long itemRequest) {
        Optional<ItemRequest> itemRequestOpt = itemRequestRepository.findById(itemRequest);
        if (itemRequestOpt.isEmpty()) {
            String message = String.format("The service did not find request by id %s", itemRequest);
            log.warn("The process validation request id ended with an error. {}", message);
            throw new NotFoundException(message);
        } else {
            log.debug("Validation request id is successfully ended.");
            return itemRequestOpt.get();
        }
    }
}
