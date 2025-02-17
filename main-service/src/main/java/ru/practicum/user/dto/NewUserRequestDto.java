package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class NewUserRequestDto {

    @NotNull
    @NotBlank
    @Size(min = 2, max = 250)
    private String name;

    @NotNull
    @Email
    @Size(min = 6, max = 254)
    private String email;
}