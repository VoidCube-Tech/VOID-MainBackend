package com.voidcube.backend.v1.events.infrastructure.persistence.repository;

import com.voidcube.backend.v1.events.infrastructure.persistence.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, UUID> {

    @Query("""
        SELECT w FROM WaitlistEntry w
        WHERE w.session.id = :sessionId
          AND w.serviceUser.id = :serviceUserId
          AND w.status = 'WAITING'
        """)
    Optional<WaitlistEntry> findWaitingEntry(
            @Param("sessionId") UUID sessionId,
            @Param("serviceUserId") UUID serviceUserId
    );

    @Query("""
        SELECT w FROM WaitlistEntry w
        WHERE w.session.id = :sessionId
          AND w.status = 'WAITING'
        ORDER BY w.position ASC
        """)
    List<WaitlistEntry> findAllWaitingBySessionIdOrdered(@Param("sessionId") UUID sessionId);

    @Query("SELECT MAX(w.position) FROM WaitlistEntry w WHERE w.session.id = :sessionId AND w.status = 'WAITING'")
    Optional<Integer> findMaxPositionBySessionId(@Param("sessionId") UUID sessionId);
}