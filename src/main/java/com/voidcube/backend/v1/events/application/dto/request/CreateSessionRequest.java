package com.voidcube.backend.v1.events.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateSessionRequest(
        @NotBlank(message = "O título da sessão é obrigatório.")
        @Size(max = 255, message = "O título da sessão não pode exceder 255 caracteres.")
        String title,

        @NotBlank(message = "A localização da sessão é obrigatória.")
        @Size(max = 255, message = "A localização não pode exceder 255 caracteres.")
        String location,

        @NotNull(message = "A data/hora de início é obrigatória.")
        OffsetDateTime startAt,

        @NotNull(message = "A data/hora de término é obrigatória.")
        OffsetDateTime endAt,

        @NotNull(message = "A capacidade é obrigatória.")
        @Min(value = 0, message = "A capacidade não pode ser negativa.")
        Integer capacity
) {
}