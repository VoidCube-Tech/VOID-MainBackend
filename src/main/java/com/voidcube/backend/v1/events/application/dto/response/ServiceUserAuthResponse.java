package com.voidcube.backend.v1.events.application.dto.response;

import java.util.UUID;

public record ServiceUserAuthResponse(
        String accessToken,
        String tokenType,
        UUID serviceUserId,
        String name,
        String email,
        UUID companyId,
        boolean temporaryPassword,
        boolean passwordChangeRequired,
        String message
) {
    public static ServiceUserAuthResponse of(
            String accessToken,
            UUID serviceUserId,
            String name,
            String email,
            UUID companyId,
            boolean temporaryPassword
    ) {
        String msg = temporaryPassword
                ? "Troca de senha obrigatória no primeiro acesso antes de realizar reservas."
                : "Autenticação realizada com sucesso.";
        return new ServiceUserAuthResponse(
                accessToken,
                "Bearer",
                serviceUserId,
                name,
                email,
                companyId,
                temporaryPassword,
                temporaryPassword,
                msg
        );
    }
}