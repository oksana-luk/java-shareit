package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.item.dto.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingControllerTest {
    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

    @MockBean
    BookingService bookingService;

    private final ApplicationContext applicationContext;

    @Autowired
    private final ObjectMapper mapper;
    @Autowired
    private final MockMvc mockMvc;

    @Test
    @SneakyThrows
    void testCreateBooking() {
        long id = 12L;
        long userId = 10L;
        BookerDto booker = new BookerDto(3L);
        ItemDtoAnswer item = new ItemDtoAnswer(1L, "some name", userId);
        String start = dateTimeFormatter.format(LocalDateTime.now().plusSeconds(1));
        String end = dateTimeFormatter.format(LocalDateTime.now().plusSeconds(5));


        NewBookingRequest newBookingRequest = new NewBookingRequest(1L, start, end);
        BookingDto bookingDto = new BookingDto(id, item, booker, BookingState.APPROVED, start, end);

        when(bookingService.addBooking(userId, newBookingRequest)).thenAnswer(invocation -> {
            System.out.println("dfdfd");
            log.info("mock bookService, addBooking {}", mockMvc);
            return bookingDto;
        });

        log.info("Serialized JSON reques {}", mapper.writeValueAsString(newBookingRequest));

        MvcResult mvcResult =
                mockMvc.perform(post("/bookings")
                                .content(mapper.writeValueAsString(newBookingRequest))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        BookingDto savedBookingDto = mapper.readValue(responseBody, BookingDto.class);

        assertNotNull(savedBookingDto.getId());
        assertEquals(id, savedBookingDto.getId());
        assertThat(savedBookingDto).usingRecursiveComparison().isEqualTo(bookingDto);

        verify(bookingService).addBooking(userId, newBookingRequest);
    }

    @Test
    @SneakyThrows
    void approveBooking() {
        String start = dateTimeFormatter.format(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MICROS));
        String end = dateTimeFormatter.format(LocalDateTime.now().plusDays(5).truncatedTo(ChronoUnit.MICROS));
        long itemId = 15L;
        long bookingId = 12L;
        long userId = 10L;
        ItemDtoAnswer itemDto = new ItemDtoAnswer(itemId, "some item name", userId);
        BookerDto bookerDto = new BookerDto(20L);
        BookingState state = BookingState.WAITING;
        Boolean approved = true;

        BookingDto bookingDto = new BookingDto(bookingId, itemDto, bookerDto, state, start, end);

        when(bookingService.approveBooking(userId, bookingId, approved))
                .thenReturn(bookingDto);

        MvcResult mvcResult =
                mockMvc.perform(patch("/bookings/" + bookingId)
                                .param("approved", approved.toString())
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        BookingDto actualBookingDto = mapper.readValue(responseBody, BookingDto.class);

        assertNotNull(actualBookingDto.getId());
        assertThat(actualBookingDto).usingRecursiveComparison().isEqualTo(bookingDto);

        verify(bookingService).approveBooking(userId, bookingId, approved);
    }

    @Test
    @SneakyThrows
    void getBooking() {
        String start = "01.01.2025T15:15:15";
        String end = "01.05.2025T15:15:15";
        long itemId = 15L;
        long bookingId = 12L;
        long userId = 10L;
        ItemDtoAnswer itemDto = new ItemDtoAnswer(itemId, "some item name", userId);
        BookerDto bookerDto = new BookerDto(20L);
        BookingState state = BookingState.WAITING;

        BookingDto bookingDto = new BookingDto(bookingId, itemDto, bookerDto, state, start, end);

        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(bookingDto);

        MvcResult mvcResult =
                mockMvc.perform(get("/bookings/" + bookingId)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        BookingDto actualBookingDto = mapper.readValue(responseBody, BookingDto.class);

        assertNotNull(actualBookingDto.getId());
        assertEquals(bookingId, actualBookingDto.getId());
        assertThat(actualBookingDto).usingRecursiveComparison().isEqualTo(bookingDto);

        verify(bookingService).getBookingById(anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    void getBookingByOwner() {
        String start = "01.01.2025T15:15:15";
        String end = "01.05.2025T15:15:15";
        long itemId = 15L;
        long bookingId = 12L;
        long userId = 10L;
        ItemDtoAnswer itemDto = new ItemDtoAnswer(itemId, "some item name", userId);
        BookerDto bookerDto = new BookerDto(20L);
        BookingState state = BookingState.WAITING;
        BookingStateFilter filter = BookingStateFilter.APPROVED;

        BookingDto bookingDto = new BookingDto(bookingId, itemDto, bookerDto, state, start, end);

        when(bookingService.getAllBookingByOwner(userId, filter))
                .thenReturn(List.of(bookingDto));

        MvcResult mvcResult =
                mockMvc.perform(get("/bookings/owner")
                                .param("state", filter.name())
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        Collection<BookingDto> actualIBookingDtos = mapper.readValue(responseBody, new TypeReference<List<BookingDto>>() {});

        assertNotNull(actualIBookingDtos);
        assertEquals(1, actualIBookingDtos.size());
        BookingDto actualBookingDto = actualIBookingDtos.stream().toList().getFirst();
        assertNotNull(actualBookingDto);
        assertThat(actualBookingDto).usingRecursiveComparison().isEqualTo(bookingDto);

        verify(bookingService).getAllBookingByOwner(userId, filter);
    }

    @Test
    @SneakyThrows
    void getBookingByUser() {
        String start = "01.01.2025T15:15:15";
        String end = "01.05.2025T15:15:15";
        long itemId = 15L;
        long bookingId = 12L;
        long userId = 10L;
        ItemDtoAnswer itemDto = new ItemDtoAnswer(itemId, "some item name", userId);
        BookerDto bookerDto = new BookerDto(20L);
        BookingState state = BookingState.WAITING;
        BookingStateFilter filter = BookingStateFilter.APPROVED;

        BookingDto bookingDto = new BookingDto(bookingId, itemDto, bookerDto, state, start, end);

        when(bookingService.getAllBookingsByUser(userId, filter))
                .thenReturn(List.of(bookingDto));

        MvcResult mvcResult =
                mockMvc.perform(get("/bookings")
                                .param("state", filter.name())
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", userId))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        Collection<BookingDto> actualIBookingDtos = mapper.readValue(responseBody, new TypeReference<List<BookingDto>>() {});

        assertNotNull(actualIBookingDtos);
        assertEquals(1, actualIBookingDtos.size());
        BookingDto actualBookingDto = actualIBookingDtos.stream().toList().getFirst();
        assertNotNull(actualBookingDto);
        assertThat(actualBookingDto).usingRecursiveComparison().isEqualTo(bookingDto);

        verify(bookingService).getAllBookingsByUser(userId, filter);
    }
}
