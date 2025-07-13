package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewRequestDto {
    @NotBlank(message = "Description should not be empty")
    @Size(max = 500, message = "Comment should not be longer than 500 characters")
    private String description;
}
