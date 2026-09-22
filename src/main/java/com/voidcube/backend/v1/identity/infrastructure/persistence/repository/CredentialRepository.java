package com.voidcube.backend.v1.identity.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voidcube.backend.v1.identity.infrastructure.persistence.model.Credential;

import java.util.Optional;
import java.util.UUID;

public interface CredentialRepository extends JpaRepository<Credential, UUID> {

    @Query("SELECT c FROM Credential c WHERE c.userIdentity.id = :userIdentityId")
    Optional<Credential> findByUserIdentityId(@Param("userIdentityId") UUID userIdentityId);
}
