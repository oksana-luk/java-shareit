package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequest;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ShareItTests {
	private final TestRestTemplate restTemplate;

	@Value("${shareit-server.url}")
	private String shareitServerUrl;

	private static final DateTimeFormatter dateTimeFormatter =
			DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);

	@Test
	void contextLoads() {
	}

	@Test
	void createUser_fullIntegrationTest() {
		String name = "Some Name";
		String email = RandomUtils.getRandomEmail();

		NewUserRequest userToSave = NewUserRequest.builder().name(name).email(email).build();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<NewUserRequest> request = new HttpEntity<>(userToSave, headers);

		ResponseEntity<UserDto> response = restTemplate.exchange(shareitServerUrl + "/users", HttpMethod.POST, request, UserDto.class);

		assertNotNull(response);
		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		UserDto actualUser = response.getBody();
		assertNotNull(actualUser.getId());

		assertThat(actualUser).usingRecursiveComparison().ignoringFields("id").isEqualTo(userToSave);
	}

	@Test
	void getItemsOfOwner_fullIntegrationTest() {
		UserDto actualUser = createUser();
		Long ownerId = actualUser.getId();

		String itemName = "Some item";
		String description = "Some item with some function in some place without some details";
		Boolean available = true;

		ru.practicum.shareit.item.dto.NewItemRequest itemToSave = ru.practicum.shareit.item.dto.NewItemRequest.builder().name(itemName).description(description).available(available).build();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("X-Sharer-User-Id", ownerId.toString());
		HttpEntity<ru.practicum.shareit.item.dto.NewItemRequest> itemRequest = new HttpEntity<>(itemToSave, headers);
		ResponseEntity<ItemDto> itemResponse = restTemplate.exchange(shareitServerUrl + "/items",
				HttpMethod.POST, itemRequest, ItemDto.class);

		assertNotNull(itemResponse);
		assertEquals(200, itemResponse.getStatusCode().value());
		assertNotNull(itemResponse.getBody());

		ItemDto actualItem = itemResponse.getBody();
		assertNotNull(actualItem.getId());
		assertNotEquals(0, actualItem.getItemRequest());

		assertEquals(itemName, actualItem.getName());
		assertEquals(description, actualItem.getDescription());
		assertEquals(available, actualItem.isAvailable());

		HttpEntity<Void> itemsRequest = new HttpEntity<>(headers);
		ResponseEntity<ItemDto[]> itemsResponse = restTemplate.exchange(shareitServerUrl + "/items",
				HttpMethod.GET, itemsRequest, ItemDto[].class);

		assertNotNull(itemsResponse);
		assertEquals(200, itemResponse.getStatusCode().value());
		assertNotNull(itemsResponse.getBody());

		ItemDto[] actualsItems = itemsResponse.getBody();
        assertEquals(1, actualsItems.length);

		ItemDto actualItemByOwner = actualsItems[0];
		assertNotNull(actualItemByOwner);
		assertNotNull(actualItemByOwner.getId());

		assertThat(actualItemByOwner).usingRecursiveComparison().isEqualTo(actualItem);
	}

	@Test
	void getBookingsByOwner_fullIntegrationTest() {
		UserDto owner = createUser();
		UserDto booker = createUser();
		Long ownerId = owner.getId();
		Long bookerId = booker.getId();
		ItemDto item = createItem(ownerId);
		Long itemId = item.getId();

		String start = dateTimeFormatter.format(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MICROS));
		String end = dateTimeFormatter.format(LocalDateTime.now().plusDays(5).truncatedTo(ChronoUnit.MICROS));

		NewBookingRequest bookingToSave = new NewBookingRequest(itemId, start, end);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("X-Sharer-User-Id", bookerId.toString());
		HttpEntity<NewBookingRequest> bookingRequest = new HttpEntity<>(bookingToSave, headers);
		ResponseEntity<BookingDto> bookingResponse = restTemplate.exchange(shareitServerUrl + "/bookings",
				HttpMethod.POST, bookingRequest, BookingDto.class);

		assertNotNull(bookingResponse);
		assertEquals(200, bookingResponse.getStatusCode().value());
		assertNotNull(bookingResponse.getBody());

		BookingDto actualBooking = bookingResponse.getBody();
		assertNotNull(actualBooking);
		assertNotNull(actualBooking.getId());
		assertNotEquals(0, actualBooking.getId());
		assertNotNull(actualBooking.getItem());
		assertNotNull(actualBooking.getItem().getId());
		assertNotEquals(0, actualBooking.getItem().getId());

		assertEquals(itemId, actualBooking.getItem().getId());
		assertEquals(start, actualBooking.getStart());
		assertEquals(end, actualBooking.getEnd());
		assertEquals(BookingState.WAITING, actualBooking.getStatus());
		Long bookingId = actualBooking.getId();

		headers.set("X-Sharer-User-Id", ownerId.toString());
		HttpEntity<Void> bookingsByOwnerRequest = new HttpEntity<>(headers);
		URI uri = UriComponentsBuilder.fromHttpUrl(shareitServerUrl + "/bookings/owner")
				.queryParam("state", BookingState.WAITING.name())
				.build()
				.toUri();
		ResponseEntity<BookingDto[]> biikingByOwnerResponse = restTemplate.exchange(uri, HttpMethod.GET, bookingRequest, BookingDto[].class);

		assertNotNull(biikingByOwnerResponse);
		assertEquals(200, biikingByOwnerResponse.getStatusCode().value());
		assertNotNull(biikingByOwnerResponse.getBody());

		BookingDto[] bookings = biikingByOwnerResponse.getBody();
		assertNotNull(bookings);
		assertEquals(1, bookings.length);
		BookingDto booking = bookings[0];
		assertNotNull(booking);
		assertNotNull(booking.getId());
		assertEquals(BookingState.WAITING, booking.getStatus());

		assertThat(booking).usingRecursiveComparison().isEqualTo(actualBooking);
	}

	@Test
	void getRequest_fullIntegrationTest() {
		UserDto user = createUser();
		Long userId = user.getId();

		String description = "some description";
		NewItemRequest itemRequestToSave = new NewItemRequest();
		itemRequestToSave.setDescription(description);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("X-Sharer-User-Id", userId.toString());
		HttpEntity<NewItemRequest> itemRequest = new HttpEntity<>(itemRequestToSave, headers);
		ResponseEntity<ItemRequestDto> itemResponse = restTemplate.exchange(shareitServerUrl + "/requests",
				HttpMethod.POST, itemRequest, ItemRequestDto.class);

		assertNotNull(itemResponse);
		assertEquals(200, itemResponse.getStatusCode().value());
		assertNotNull(itemResponse.getBody());

		ItemRequestDto actualItemRequest = itemResponse.getBody();
		assertNotNull(actualItemRequest);
		assertNotNull(actualItemRequest.getId());
		assertNotEquals(0, actualItemRequest.getId());
		Long itemRequestId = actualItemRequest.getId();

		//prove all fields
		assertEquals(description, actualItemRequest.getDescription());
		assertNotNull(actualItemRequest.getRequestor());
		assertNotNull(actualItemRequest.getRequestor().getId());
		assertNotEquals(0, actualItemRequest.getRequestor().getId());
		assertEquals(userId, actualItemRequest.getRequestor().getId());
		assertNotNull(actualItemRequest.getCreated());

		HttpEntity<Void> requestById = new HttpEntity<>(headers);
		ResponseEntity<ItemRequestDto> itemResponseById = restTemplate.exchange(shareitServerUrl + "/requests/" + itemRequestId,
				HttpMethod.GET, requestById, ItemRequestDto.class);


		//get requests by id
		assertNotNull(itemResponseById);
		assertEquals(200, itemResponseById.getStatusCode().value());
		assertNotNull(itemResponseById.getBody());

		ItemRequestDto actualItemRequestById = itemResponseById.getBody();
		assertNotNull(actualItemRequestById);
		assertNotNull(actualItemRequestById.getId());
		assertNotEquals(0, actualItemRequestById.getId());

		assertThat(actualItemRequestById).usingRecursiveComparison().ignoringFields("created", "items").isEqualTo(actualItemRequest);
	}

	private UserDto createUser() {
		String name = "Some Name";
		String email = RandomUtils.getRandomEmail();

		NewUserRequest userToSave = NewUserRequest.builder().name(name).email(email).build();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<NewUserRequest> userRequest = new HttpEntity<>(userToSave, headers);

		ResponseEntity<UserDto> userResponse = restTemplate.exchange(shareitServerUrl + "/users",
				HttpMethod.POST, userRequest, UserDto.class);

		assertNotNull(userResponse);
		assertEquals(200, userResponse.getStatusCode().value());
		assertNotNull(userResponse.getBody());
		UserDto actualUser = userResponse.getBody();
		assertNotNull(actualUser.getId());
		assertNotEquals(0, actualUser.getId());

		return actualUser;
	}

	private ItemDto createItem(Long ownerId) {
		String itemName = "Some item";
		String description = "Some item with some function in some place without some details";
		Boolean available = true;

		ru.practicum.shareit.item.dto.NewItemRequest itemToSave = ru.practicum.shareit.item.dto.NewItemRequest.builder().name(itemName).description(description).available(available).build();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("X-Sharer-User-Id", ownerId.toString());
		HttpEntity<ru.practicum.shareit.item.dto.NewItemRequest> itemRequest = new HttpEntity<>(itemToSave, headers);
		ResponseEntity<ItemDto> itemResponse = restTemplate.exchange(shareitServerUrl + "/items",
				HttpMethod.POST, itemRequest, ItemDto.class);

		assertNotNull(itemResponse);
		assertEquals(200, itemResponse.getStatusCode().value());
		assertNotNull(itemResponse.getBody());

		ItemDto actualItem = itemResponse.getBody();
		assertNotNull(actualItem.getId());
		assertNotEquals(0, actualItem.getItemRequest());

		return actualItem;
	}
}
