package com.voidcube.backend.v1.events.infrastructure.persistence.repository;

import com.voidcube.backend.v1.events.domain.ReservationStatus;
import com.voidcube.backend.v1.events.infrastructure.persistence.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.session.id = :sessionId
          AND r.serviceUser.id = :serviceUserId
          AND r.status = 'CONFIRMED'
        """)
    Optional<Reservation> findActiveReservation(
            @Param("sessionId") UUID sessionId,
            @Param("serviceUserId") UUID serviceUserId
    );

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.session.id = :sessionId AND r.status = 'CONFIRMED'")
    long countConfirmedReservationsBySessionId(@Param("sessionId") UUID sessionId);

    List<Reservation> findAllByServiceUserIdAndStatus(UUID serviceUserId, ReservationStatus status);
}