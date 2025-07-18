package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.RandomUtils;

import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDtoAnswer;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {
    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

    @Mock private ItemRequestRepository itemRequestRepositoryMock;
    @Mock private ItemRepository itemRepositoryMock;
    @Mock private UserRepository userRepositoryMock;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    private ItemRequestService itemRequestService;

    @BeforeEach
    void setUp() {
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepositoryMock, userRepositoryMock, itemRepositoryMock);
    }

    @Test
    void addRequest_shouldReturnItemRequest() {
        String name = "Some Name";
        String description = "Some description";
        String email = RandomUtils.getRandomEmail();
        long itemId = 12L;
        long requestId = 11L;
        long userId = 1L;
        LocalDateTime createdDate = LocalDateTime.now();
        String created = dateTimeFormatter.format(createdDate);

        NewItemRequest newItemRequest = new NewItemRequest(description);
        UserDto requestor = UserDto.builder().id(userId).name(name).email(email).build();
        User user = new User(userId, name, email);
        ItemRequest request = new ItemRequest(requestId, user, description, createdDate);
        ItemRequestDto itemRequestDto = new ItemRequestDto(requestId, requestor, description, created, Set.of());

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(itemRequestRepositoryMock.save(any(ItemRequest.class)))
                .thenReturn(request);

        ItemRequestDto actuelItemRequestDto = itemRequestService.addRequest(user.getId(), newItemRequest);

        assertNotNull(actuelItemRequestDto);
        assertEquals(requestId, actuelItemRequestDto.getId());
        assertEquals(requestor.getId(), actuelItemRequestDto.getRequestor().getId());
        assertEquals(description, actuelItemRequestDto.getDescription());
        assertEquals(created, actuelItemRequestDto.getCreated());

        verify(userRepositoryMock).findById(anyLong());
        verify(itemRequestRepositoryMock).save(any(ItemRequest.class));
    }

    @Test
    void getRequestByRequestor_shouldReturnListOfRequestsWithAnswers() {
        String name = "Some Name";
        String description = "Some description";
        String email = RandomUtils.getRandomEmail();
        long itemId1 = 12L;
        long itemId2 = 13L;
        long requestId1 = 11L;
        long requestId2 = 10L;
        long userId = 1L;
        boolean available = true;
        LocalDateTime createdDate = LocalDateTime.now();
        String created = dateTimeFormatter.format(createdDate);

        UserDto requestor = UserDto.builder().id(userId).name(name).email(email).build();
        User user = new User(userId, name, email);

        ItemRequest request1 = new ItemRequest(requestId1, user, description, createdDate);
        ItemRequest request2 = new ItemRequest(requestId2, user, description, createdDate.plusDays(1));

        User owner = new User(1L, name, RandomUtils.getRandomEmail());
        Item item1 = new Item(itemId1, owner, name, description, available, null, null, request1);
        Item item2 = new Item(itemId2, owner, name, description, available, null, null, request2);

        ItemDtoAnswer answer1 = new ItemDtoAnswer(itemId1, name, owner.getId());
        ItemDtoAnswer answer2 = new ItemDtoAnswer(itemId2, name, owner.getId());

        ItemRequestDto itemRequestDto1 = new ItemRequestDto(requestId1, requestor, description, created, Set.of(answer1));
        ItemRequestDto itemRequestDto2 = new ItemRequestDto(requestId1, requestor, description, created, Set.of(answer2));

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(itemRequestRepositoryMock.findAllByRequestorIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(request1, request2));

        when(itemRepositoryMock.findAllByItemRequestId(anyList()))
                .thenReturn(List.of(item1, item2));

        List<ItemRequestDto> findedItemRequestDtos = itemRequestService.getRequestsByRequestor(user.getId());

        assertNotNull(findedItemRequestDtos);
        assertEquals(2, findedItemRequestDtos.size());
        ItemRequestDto actuelItemRequest1 = findedItemRequestDtos.get(0);
        ItemRequestDto actuelItemRequest2 = findedItemRequestDtos.get(1);

        assertEquals(request1.getId(), actuelItemRequest1.getId());
        assertEquals(request2.getId(), actuelItemRequest2.getId());
        assertEquals(description, actuelItemRequest1.getDescription());
        assertEquals(description, actuelItemRequest2.getDescription());

        assertEquals(requestor.getId(), actuelItemRequest1.getRequestor().getId());
        assertEquals(requestor.getId(), actuelItemRequest2.getRequestor().getId());

        assertEquals(dateTimeFormatter.format(createdDate.plusDays(1)), actuelItemRequest2.getCreated());
        assertEquals(created, actuelItemRequest1.getCreated());

        assertNotNull(actuelItemRequest1.getItems());
        assertNotNull(actuelItemRequest2.getItems());
        assertTrue(actuelItemRequest1.getItems().size() == 1);
        assertTrue(actuelItemRequest2.getItems().size() == 1);

        ItemDtoAnswer actuelAnswer1 = actuelItemRequest1.getItems().stream().toList().getFirst();
        ItemDtoAnswer actuelAnswer2 = actuelItemRequest2.getItems().stream().toList().getFirst();

        assertThat(actuelAnswer1).usingRecursiveComparison().isEqualTo(answer1);
        assertThat(actuelAnswer2).usingRecursiveComparison().isEqualTo(answer2);

        verify(userRepositoryMock).findById(anyLong());
        verify(itemRequestRepositoryMock).findAllByRequestorIdOrderByCreatedDesc(anyLong());
        verify(itemRepositoryMock).findAllByItemRequestId(anyList());
    }

    @Test
    void getAll_shouldReturnListOfItemRequest() {
        String name = "Some Name";
        String description = "Some description";
        String email = RandomUtils.getRandomEmail();
        long itemId = 12L;
        long requestId = 11L;
        long userId = 1L;
        LocalDateTime createdDate = LocalDateTime.now();
        String created = dateTimeFormatter.format(createdDate);

        NewItemRequest newItemRequest = new NewItemRequest(description);
        UserDto requestor = UserDto.builder().id(userId).name(name).email(email).build();
        User user = new User(userId, name, email);
        ItemRequest request = new ItemRequest(requestId, user, description, createdDate);
        ItemRequestDto itemRequestDto = new ItemRequestDto(requestId, requestor, description, created, Set.of());

        when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(user));

        when(itemRequestRepositoryMock.findAllByRequestorIdNotOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(request));

        List<ItemRequestDto> actuelItemRequestDto = itemRequestService.getAll(user.getId());

        assertNotNull(actuelItemRequestDto);
        assertTrue(actuelItemRequestDto.size() == 1);
        ItemRequestDto requestDto = actuelItemRequestDto.getFirst();

        assertThat(requestDto).usingRecursiveComparison().isEqualTo(itemRequestDto);

        verify(userRepositoryMock).findById(anyLong());
        verify(itemRequestRepositoryMock).findAllByRequestorIdNotOrderByCreatedDesc(anyLong());
    }
}
