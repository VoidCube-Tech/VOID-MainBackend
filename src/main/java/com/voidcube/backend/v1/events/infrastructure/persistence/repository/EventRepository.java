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

    List<Event> findAllByCompanyIdAndDeletedAtIsNull(UUID companyId);

    List<Event> findAllByCompanyIdAndStatusAndDeletedAtIsNull(UUID companyId, EventStatus status);
}