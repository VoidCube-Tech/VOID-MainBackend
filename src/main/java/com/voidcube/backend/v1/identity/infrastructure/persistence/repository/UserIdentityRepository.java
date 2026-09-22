package com.voidcube.backend.v1.identity.infrastructure.persistence.repository;

import com.voidcube.backend.v1.identity.infrastructure.persistence.model.UserIdentity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {

    @Query("SELECT u FROM UserIdentity u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<UserIdentity> findActiveByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM UserIdentity u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsActiveByEmail(@Param("email") String email);

    Optional<UserIdentity> findByIdAndDeletedAtIsNull(UUID id);
}