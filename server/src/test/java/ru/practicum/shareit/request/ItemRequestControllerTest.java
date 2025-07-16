package ru.practicum.shareit.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.RandomUtils;
import ru.practicum.shareit.item.dto.ItemDtoAnswer;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(ItemRequestController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestControllerTest {
    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

    @MockBean
     ItemRequestService itemRequestService;

    private final ApplicationContext applicationContext;

    @Autowired
    private final ObjectMapper mapper;
    @Autowired
    private final MockMvc mockMvc;

    @Test
    @SneakyThrows
    void testCreateRequest() {
        String name = "Some Name";
        String description = "Some description";
        String email = RandomUtils.getRandomEmail();
        Set<ItemDtoAnswer> answers = Set.of();
        String created = dateTimeFormatter.format(LocalDateTime.now().plusSeconds(1));
        long id = 12L;
        long userId = 10L;
        UserDto userDto = UserDto.builder().id(userId).name(name).email(email).build();
        NewItemRequest newItemRequest = new NewItemRequest(description);
        ItemRequestDto itemRequestDto = new ItemRequestDto(id, userDto, description, created, answers);
        when(itemRequestService.addRequest(userId, newItemRequest))
                .thenReturn(itemRequestDto);

        MvcResult mvcResult =
                mockMvc.perform(post("/requests")
                                .content(mapper.writeValueAsString(newItemRequest))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        ItemRequestDto saveditemdto = mapper.readValue(responseBody, ItemRequestDto.class);

        assertNotNull(saveditemdto.getId());
        assertEquals(id, saveditemdto.getId());
        assertThat(saveditemdto).usingRecursiveComparison().isEqualTo(itemRequestDto);

        verify(itemRequestService).addRequest(userId, newItemRequest);
    }

    @Test
    @SneakyThrows
    void getRequest() {
        String name = "Some Name";
        String description = "Some description";
        String email = RandomUtils.getRandomEmail();
        Set<ItemDtoAnswer> answers = Set.of();
        String created = dateTimeFormatter.format(LocalDateTime.now().plusSeconds(1));
        long id = 12L;
        long userId = 10L;

        UserDto userDto = UserDto.builder().id(userId).name(name).email(email).build();
        ItemRequestDto itemRequestDto = new ItemRequestDto(id, userDto, description, created, answers);


        when(itemRequestService.getRequest(userId, id))
                .thenReturn(itemRequestDto);

        MvcResult mvcResult =
                mockMvc.perform(get("/requests/" + id)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        ItemRequestDto actualItemRequestDto = mapper.readValue(responseBody, ItemRequestDto.class);

        assertNotNull(actualItemRequestDto.getId());
        assertEquals(id, actualItemRequestDto.getId());
        assertThat(actualItemRequestDto).usingRecursiveComparison().isEqualTo(itemRequestDto);

        verify(itemRequestService).getRequest(userId, id);
    }

    @Test
    @SneakyThrows
    void getRequestByRequestor() {
        String name = "Some Name";
        String description = "Some description";
        String email = RandomUtils.getRandomEmail();
        Set<ItemDtoAnswer> answers = Set.of();
        String created = dateTimeFormatter.format(LocalDateTime.now().plusSeconds(1));
        long id = 12L;
        long userId = 10L;

        UserDto userDto = UserDto.builder().id(userId).name(name).email(email).build();
        ItemRequestDto itemRequestDto = new ItemRequestDto(id, userDto, description, created, answers);

        when(itemRequestService.getRequestsByRequestor(anyLong()))
                .thenReturn(List.of(itemRequestDto));

        MvcResult mvcResult =
                mockMvc.perform(get("/requests")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        Collection<ItemRequestDto> actualItemRequestDtos = mapper.readValue(responseBody, new TypeReference<List<ItemRequestDto>>() {});

        assertNotNull(actualItemRequestDtos);
        assertEquals(1, actualItemRequestDtos.size());
        ItemRequestDto actualItemRequest = actualItemRequestDtos.stream().toList().getFirst();
        assertNotNull(actualItemRequest);
        assertThat(actualItemRequest).usingRecursiveComparison().isEqualTo(itemRequestDto);

        verify(itemRequestService).getRequestsByRequestor(anyLong());
    }

    @Test
    @SneakyThrows
    void getAllRequests() {
        String name = "Some Name";
        String description = "Some description";
        String email = RandomUtils.getRandomEmail();
        Set<ItemDtoAnswer> answers = Set.of();
        String created = dateTimeFormatter.format(LocalDateTime.now().plusSeconds(1));
        long id = 12L;
        long userId = 10L;

        UserDto userDto = UserDto.builder().id(userId).name(name).email(email).build();
        ItemRequestDto itemRequestDto = new ItemRequestDto(id, userDto, description, created, answers);

        when(itemRequestService.getAll(userId))
                .thenReturn(List.of(itemRequestDto));

        MvcResult mvcResult =
                mockMvc.perform(get("/requests/all")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        Collection<ItemRequestDto> actualItemRequestsDtos = mapper.readValue(responseBody, new TypeReference<List<ItemRequestDto>>() {});

        assertNotNull(actualItemRequestsDtos);
        assertEquals(1, actualItemRequestsDtos.size());
        ItemRequestDto actualItemRequest = actualItemRequestsDtos.stream().toList().getFirst();
        assertNotNull(actualItemRequest);
        assertThat(actualItemRequest).usingRecursiveComparison().isEqualTo(itemRequestDto);

        verify(itemRequestService).getAll(userId);
    }
}
