package com.voidcube.backend.v1.events.application.dto.response;

import com.voidcube.backend.v1.events.domain.SessionStatus;
import com.voidcube.backend.v1.events.infrastructure.persistence.model.EventSession;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        UUID eventId,
        UUID companyId,
        String title,
        String location,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        Integer capacity,
        SessionStatus status,
        Long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static SessionResponse fromEntity(EventSession session) {
        return new SessionResponse(
                session.getId(),
                session.getEvent() != null ? session.getEvent().getId() : null,
                session.getCompanyId(),
                session.getTitle(),
                session.getLocation(),
                session.getStartAt(),
                session.getEndAt(),
                session.getCapacity(),
                session.getStatus(),
                session.getVersion(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}