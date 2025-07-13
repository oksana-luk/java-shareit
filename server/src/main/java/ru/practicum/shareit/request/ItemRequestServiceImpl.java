package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto addRequest(Long userId, NewItemRequest newItemRequest) {
        User requestor = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        ItemRequest request = ItemRequestMapper.mapToItemRequest(newItemRequest, requestor);
        request = itemRequestRepository.save(request);
        return ItemRequestMapper.mapToItemRequestDto(request, Set.of());
    }

    @Override
    public List<ItemRequestDto> getRequestsByRequestor(long userId) {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        Map<Long, Set<ItemDtoAnswer>> answersByRequests = getAnswersByRequests(requests);

        return requests.stream().map(request -> {
            Set<ItemDtoAnswer> currentAnswer = answersByRequests.get(request.getId());
            return ItemRequestMapper.mapToItemRequestDto(request, currentAnswer);
        }).toList();
    }

    @Override
    public List<ItemRequestDto> getAll(long userId) {
        return itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId).stream()
                .map(itemRequest -> ItemRequestMapper.mapToItemRequestDto(itemRequest, Set.of()))
                .toList();
    }

    @Override
    public ItemRequestDto getRequest(long userId, long itemRequestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new NotFoundException("Not found"));
        Map<Long, Set<ItemDtoAnswer>> answers = getAnswersByRequests(List.of(itemRequest));
        return ItemRequestMapper.mapToItemRequestDto(itemRequest, answers.get(itemRequest.getId()));
    }

    public Map<Long, Set<ItemDtoAnswer>> getAnswersByRequests(List<ItemRequest> requests) {
        Map<Long, Set<ItemDtoAnswer>> requestsAnswer = new HashMap<>();
        List<Item> answers = itemRepository.findAllByItemRequestInAndAvailableTrue(requests);


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
}
