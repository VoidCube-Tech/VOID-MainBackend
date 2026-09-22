package com.voidcube.backend.v1.identity.infrastructure.persistence.repository;

import com.voidcube.backend.v1.identity.infrastructure.persistence.model.RefreshSession;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, UUID> {

    @Query("""
        SELECT r FROM RefreshSession r
        WHERE r.tokenHash = :tokenHash
          AND r.revokedAt IS NULL
          AND r.expiresAt > :now
        """)
    Optional<RefreshSession> findValidByTokenHash(
            @Param("tokenHash") String tokenHash,
            @Param("now") OffsetDateTime now
    );

    @Modifying
    @Query("""
        UPDATE RefreshSession r SET r.revokedAt = :now
        WHERE r.userIdentity.id = :userIdentityId AND r.revokedAt IS NULL
        """)
    int revokeAllByUserIdentityId(
            @Param("userIdentityId") UUID userIdentityId,
            @Param("now") OffsetDateTime now
    );
}