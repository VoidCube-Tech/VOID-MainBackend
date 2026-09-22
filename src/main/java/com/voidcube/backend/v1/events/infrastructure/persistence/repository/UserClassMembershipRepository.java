package com.voidcube.backend.v1.events.infrastructure.persistence.repository;

import com.voidcube.backend.v1.events.infrastructure.persistence.model.UserClassMembership;
import com.voidcube.backend.v1.events.infrastructure.persistence.model.UserClassMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserClassMembershipRepository extends JpaRepository<UserClassMembership, UserClassMembershipId> {

    List<UserClassMembership> findAllByServiceUserId(UUID serviceUserId);

    List<UserClassMembership> findAllByUserClassId(UUID classId);
}