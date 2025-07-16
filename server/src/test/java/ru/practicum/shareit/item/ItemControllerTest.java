package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.dto.LastBookingDto;
import ru.practicum.shareit.item.dto.*;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemControllerTest {
    @MockBean
    ItemService itemService;

    private final ApplicationContext applicationContext;

    @Autowired
    private final ObjectMapper mapper;
    @Autowired
    private final MockMvc mockMvc;

    @Test
    @SneakyThrows
    void testCreateItem() {
        String name = "Some Name";
        String description = "Some description";
        Boolean available = true;
        Set<CommentDto> comments = Set.of();
        LastBookingDto lastBookingDto = null;
        LastBookingDto nextBookingDto = null;
        long requestId = 15L;
        long id = 12L;
        long userId = 10L;

        NewItemRequest newItemRequest = NewItemRequest.builder().name(name).description(description).requestId(requestId).available(available).build();
        ItemDto itemDto = new ItemDto(id, name, description, available, lastBookingDto, nextBookingDto, requestId, comments);
        when(itemService.addItem(userId, newItemRequest))
                .thenReturn(itemDto);

        MvcResult mvcResult =
                mockMvc.perform(post("/items")
                            .content(mapper.writeValueAsString(newItemRequest))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        ItemDto savedItemDto = mapper.readValue(responseBody, ItemDto.class);

        assertNotNull(savedItemDto.getId());
        assertEquals(id, savedItemDto.getId());
        assertThat(savedItemDto).usingRecursiveComparison().ignoringFields("id").isEqualTo(itemDto);

        verify(itemService).addItem(userId, newItemRequest);
    }

    @Test
    @SneakyThrows
    void getItem() {
        String name = "Some Name";
        String description = "Some description";
        Boolean available = true;
        Set<CommentDto> comments = Set.of();
        LastBookingDto lastBookingDto = null;
        LastBookingDto nextBookingDto = null;
        long requestId = 15L;
        long id = 12L;
        long userId = 10L;

        ItemDto itemDto = new ItemDto(id, name, description, available, lastBookingDto, nextBookingDto, requestId, comments);

        when(itemService.getItemById(anyLong()))
                .thenReturn(itemDto);

        MvcResult mvcResult =
                mockMvc.perform(get("/items/" + id)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        ItemDto actualItemDto = mapper.readValue(responseBody, ItemDto.class);

        assertNotNull(actualItemDto.getId());
        assertEquals(id, actualItemDto.getId());
        assertThat(actualItemDto).usingRecursiveComparison().isEqualTo(itemDto);

        verify(itemService).getItemById(anyLong());
    }

    @Test
    @SneakyThrows
    void updateItem() {
        String name = "Some Name";
        String description = "Some description";
        Boolean available = true;
        Set<CommentDto> comments = Set.of();
        LastBookingDto lastBookingDto = null;
        LastBookingDto nextBookingDto = null;
        long requestId = 15L;
        long id = 12L;
        long userId = 10L;

        String newName = name + "upd";
        String newDescription = description + "upd";
        Boolean newAvailable = !available;

        ItemDto itemDto = new ItemDto(id, name, description, available, lastBookingDto, nextBookingDto, requestId, comments);
        UpdateItemRequest updateItemRequest = new UpdateItemRequest(newName, newDescription, newAvailable);

        when(itemService.updateItem(userId, id, updateItemRequest))
                .thenReturn(itemDto);

        MvcResult mvcResult =
                mockMvc.perform(patch("/items/" + id)
                                .content(mapper.writeValueAsString(updateItemRequest))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        ItemDto actualItemDto = mapper.readValue(responseBody, ItemDto.class);

        assertNotNull(actualItemDto.getId());
        assertThat(actualItemDto).usingRecursiveComparison().isEqualTo(itemDto);

        verify(itemService).updateItem(userId, id, updateItemRequest);
    }

    @Test
    @SneakyThrows
    void getItemByOwner() {
        String name = "Some Name";
        String description = "Some description";
        Boolean available = true;
        Set<CommentDto> comments = Set.of();
        LastBookingDto lastBookingDto = null;
        LastBookingDto nextBookingDto = null;
        long requestId = 15L;
        long id = 12L;
        long userId = 10L;

        ItemDto itemDto = new ItemDto(id, name, description, available, lastBookingDto, nextBookingDto, requestId, comments);

        when(itemService.getItemsByOwnerId(anyLong()))
                .thenReturn(List.of(itemDto));

        MvcResult mvcResult =
                mockMvc.perform(get("/items")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        Collection<ItemDto> actualItemDtos = mapper.readValue(responseBody, new TypeReference<List<ItemDto>>() {});

        assertNotNull(actualItemDtos);
        assertEquals(1, actualItemDtos.size());
        ItemDto actualItem = actualItemDtos.stream().toList().getFirst();
        assertNotNull(actualItem);
        assertThat(actualItem).usingRecursiveComparison().isEqualTo(itemDto);

        verify(itemService).getItemsByOwnerId(anyLong());
    }

    @Test
    @SneakyThrows
    void getItemByName() {
        String name = "Some Name";
        String description = "Some description";
        Boolean available = true;
        Set<CommentDto> comments = Set.of();
        LastBookingDto lastBookingDto = null;
        LastBookingDto nextBookingDto = null;
        long requestId = 15L;
        long id = 12L;
        long userId = 10L;

        ItemDto itemDto = new ItemDto(id, name, description, available, lastBookingDto, nextBookingDto, requestId, comments);

        when(itemService.getItemsByPattern(anyString()))
                .thenReturn(List.of(itemDto));

        MvcResult mvcResult =
                mockMvc.perform(get("/items/search")
                                .param("text", anyString())
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        Collection<ItemDto> actualItemDtos = mapper.readValue(responseBody, new TypeReference<List<ItemDto>>() {});

        assertNotNull(actualItemDtos);
        assertEquals(1, actualItemDtos.size());
        ItemDto actualItem = actualItemDtos.stream().toList().getFirst();
        assertNotNull(actualItem);
        assertThat(actualItem).usingRecursiveComparison().isEqualTo(itemDto);

        verify(itemService).getItemsByPattern(anyString());
    }

    @Test
    @SneakyThrows
    void testAddComment() {
        long itemId = 12L;
        long userId = 10L;
        String text = "Some comment text";
        String authorName = "Some Author";
        long commentId = 14L;
        String created = "01.01.2022T15:45:45";

        NewCommentRequest newCommentRequest = new NewCommentRequest(text);
        CommentDto commentDto = new CommentDto(commentId, authorName, text, created);
        when(itemService.addComment(newCommentRequest, userId, itemId))
                .thenReturn(commentDto);

        MvcResult mvcResult =
                mockMvc.perform(post("/items/" + itemId + "/comment")
                                .content(mapper.writeValueAsString(newCommentRequest))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        CommentDto savedCommentDto = mapper.readValue(responseBody, CommentDto.class);

        assertNotNull(savedCommentDto.getId());
        assertEquals(commentId, savedCommentDto.getId());
        assertThat(savedCommentDto).usingRecursiveComparison().isEqualTo(commentDto);

        verify(itemService).addComment(newCommentRequest, userId, itemId);
    }
}
