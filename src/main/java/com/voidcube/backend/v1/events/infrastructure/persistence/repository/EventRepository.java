package com.voidcube.backend.v1.events.infrastructure.persistence.repository;

import com.voidcube.backend.v1.events.domain.EventStatus;
import com.voidcube.backend.v1.events.infrastructure.persistence.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    Optional<Event> findByIdAndCompanyIdAndDeletedAtIsNull(UUID id, UUID companyId);

    @Query("SELECT e FROM Event e WHERE e.companyId = :companyId AND e.status = :status AND e.deletedAt IS NULL")
    List<Event> findAllByCompanyIdAndStatus(
            @Param("companyId") UUID companyId,
            @Param("status") EventStatus status
    );
}