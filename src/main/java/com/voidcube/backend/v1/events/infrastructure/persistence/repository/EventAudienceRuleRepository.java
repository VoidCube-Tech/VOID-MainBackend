package com.voidcube.backend.v1.events.infrastructure.persistence.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.voidcube.backend.v1.events.infrastructure.persistence.model.EventAudienceRule;

import java.util.List;
import java.util.UUID;

public interface EventAudienceRuleRepository extends JpaRepository<EventAudienceRule, UUID> {

    List<EventAudienceRule> findAllByEventId(UUID eventId);
}