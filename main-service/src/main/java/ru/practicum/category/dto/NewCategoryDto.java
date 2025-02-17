package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


@Data
@ToString
@RequiredArgsConstructor
public class NewCategoryDto {

    @Size(min = 1, max = 50)
    @NotNull
    @NotBlank
    private String name;
}