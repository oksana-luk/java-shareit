package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.RandomUtils;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestRepositoryTest {
    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;


    @Test
    void save_shouldReturnSavedItemRequest() {
        String name = "Some Name";
        String description = "Some description";
        String email = RandomUtils.getRandomEmail();
        long itemId = 12L;
        long requestId = 11L;
        long userId = 1L;
        LocalDateTime createdDate = LocalDateTime.now();
        String created = dateTimeFormatter.format(createdDate);

        User user = new User(0L, name, email);
        user = userRepository.save(user);
        assertNotNull(user);
        assertThat(user.getId() > 0);

        ItemRequest request = new ItemRequest(0L, user, description, createdDate);
        ItemRequest savedRequest = itemRequestRepository.save(request);
        assertNotNull(savedRequest);
        assertTrue(savedRequest.getId() > 0);

        assertThat(savedRequest).usingRecursiveComparison().ignoringFields("id").isEqualTo(request);
    }

    @Test
    void findAllByRequestorId_shouldReturnListOfItemRequestsOrderByCreatedDesc() {
        String name = "Some Name";
        String description = "Some description";
        String email = RandomUtils.getRandomEmail();
        long itemId = 12L;
        long requestId = 11L;
        long userId = 1L;
        LocalDateTime createdDate = LocalDateTime.now();
        String created = dateTimeFormatter.format(createdDate);

        User requestor = new User(0L, name, email);
        requestor = userRepository.save(requestor);
        assertNotNull(requestor);
        assertThat(requestor.getId() > 0);

        ItemRequest request1 = new ItemRequest(0L, requestor, description, createdDate);
        ItemRequest request2 = new ItemRequest(0L, requestor, description, createdDate.plusDays(1));

        ItemRequest savedRequest1 = itemRequestRepository.save(request1);
        assertNotNull(savedRequest1);
        assertTrue(savedRequest1.getId() > 0);

        ItemRequest savedRequest2 = itemRequestRepository.save(request2);
        assertNotNull(savedRequest2);
        assertTrue(savedRequest2.getId() > 0);

        List<ItemRequest> findedRequests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(requestor.getId());
        assertNotNull(findedRequests);
        assertThat(findedRequests.size() == 2);
        ItemRequest findedRequest1 = findedRequests.get(0);
        ItemRequest findedRequest2 = findedRequests.get(1);


        //expected order request2, request1
        assertThat(request1).usingRecursiveComparison().ignoringFields("id").isEqualTo(findedRequest2);
        assertThat(request2).usingRecursiveComparison().ignoringFields("id").isEqualTo(findedRequest1);
    }

    @Test
    void findAllByOtherRequestor_shouldReturnListOfItemRequestsNotOfRequestor() {
        String name = "Some Name";
        String description = "Some description";
        String email = RandomUtils.getRandomEmail();
        long itemId = 12L;
        long requestId = 11L;
        long userId = 1L;
        LocalDateTime createdDate = LocalDateTime.now();
        String created = dateTimeFormatter.format(createdDate);

        User requestor = new User(0L, name, email);
        requestor = userRepository.save(requestor);
        assertNotNull(requestor);
        assertThat(requestor.getId() > 0);

        User user = new User(0L, name, RandomUtils.getRandomEmail());
        user = userRepository.save(user);
        assertNotNull(user);
        assertThat(user.getId() > 0);

        ItemRequest request1 = new ItemRequest(0L, requestor, description, createdDate);
        ItemRequest request2 = new ItemRequest(0L, user, description, createdDate.plusDays(1));

        ItemRequest savedRequest1 = itemRequestRepository.save(request1);
        assertNotNull(savedRequest1);
        assertTrue(savedRequest1.getId() > 0);

        ItemRequest savedRequest2 = itemRequestRepository.save(request2);
        assertNotNull(savedRequest2);
        assertTrue(savedRequest2.getId() > 0);

        //expected order request2, request1
        assertThat(request1).usingRecursiveComparison().ignoringFields("id").isEqualTo(savedRequest1);
        assertThat(request2).usingRecursiveComparison().ignoringFields("id").isEqualTo(savedRequest2);

        List<ItemRequest> findedRequests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(requestor.getId());
        assertNotNull(findedRequests);
        assertThat(findedRequests.size() == 1);
        ItemRequest findedRequest = findedRequests.get(0);


        //expected only request2
        assertThat(request2).usingRecursiveComparison().ignoringFields("id").isEqualTo(findedRequest);
    }
}
