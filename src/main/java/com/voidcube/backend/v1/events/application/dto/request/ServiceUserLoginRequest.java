package com.voidcube.backend.v1.events.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ServiceUserLoginRequest(
        @NotBlank(message = "O identificador (e-mail ou matrícula) é obrigatório")
        String identifier,

        @NotBlank(message = "A senha é obrigatória")
        String password,

        UUID companyId
) {
}