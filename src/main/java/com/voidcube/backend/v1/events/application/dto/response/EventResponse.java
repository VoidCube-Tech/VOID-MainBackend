package com.voidcube.backend.v1.events.application.dto.response;

import com.voidcube.backend.v1.events.domain.EventStatus;
import com.voidcube.backend.v1.events.infrastructure.persistence.model.Event;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record EventResponse(
        UUID id,
        UUID companyId,
        UUID ownerManagerId,
        String title,
        String description,
        EventStatus status,
        Long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<SessionResponse> sessions
) {
    public static EventResponse fromEntity(Event event, List<SessionResponse> sessions) {
        return new EventResponse(
                event.getId(),
                event.getCompanyId(),
                event.getOwnerManagerId(),
                event.getTitle(),
                event.getDescription(),
                event.getStatus(),
                event.getVersion(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                sessions != null ? sessions : List.of()
        );
    }

    public static EventResponse fromEntity(Event event) {
        return fromEntity(event, List.of());
    }
}