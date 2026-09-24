package com.voidcube.backend.v1.events.infrastructure.persistence.repository;

import com.voidcube.backend.v1.events.infrastructure.persistence.model.EventSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventSessionRepository extends JpaRepository<EventSession, UUID> {

    Optional<EventSession> findByIdAndCompanyIdAndDeletedAtIsNull(UUID id, UUID companyId);

    Optional<EventSession> findByIdAndEventIdAndCompanyIdAndDeletedAtIsNull(UUID id, UUID eventId, UUID companyId);

    @Query("SELECT s FROM EventSession s WHERE s.event.id = :eventId AND s.deletedAt IS NULL ORDER BY s.startAt ASC")
    List<EventSession> findAllActiveByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT s FROM EventSession s WHERE s.event.id = :eventId AND s.companyId = :companyId AND s.deletedAt IS NULL ORDER BY s.startAt ASC")
    List<EventSession> findAllByEventIdAndCompanyIdAndDeletedAtIsNullOrderByStartAtAsc(
            @Param("eventId") UUID eventId,
            @Param("companyId") UUID companyId
    );
}