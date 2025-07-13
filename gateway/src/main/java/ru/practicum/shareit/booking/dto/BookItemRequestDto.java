package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {
	@NotNull(message = "Item id should be not empty")
	@Positive(message = "Item id should be positive")
	private Long itemId;

	@NotNull(message = "Invalid booking start date")
	@FutureOrPresent(message = "Invalid booking start date")
	private LocalDateTime start;

	@NotNull(message = "Invalid booking end date")
	@Future(message = "Invalid booking end date")
	private LocalDateTime end;
}