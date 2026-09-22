package com.voidcube.backend.v1.events.infrastructure.persistence.repository;

import com.voidcube.backend.v1.events.infrastructure.persistence.model.LegacyUserReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LegacyUserReferenceRepository extends JpaRepository<LegacyUserReference, UUID> {

    @Query("""
        SELECT r FROM LegacyUserReference r
        WHERE r.companyId = :companyId AND r.registrationCode = :registrationCode
        """)
    Optional<LegacyUserReference> findByCompanyIdAndRegistrationCode(
            @Param("companyId") UUID companyId,
            @Param("registrationCode") String registrationCode
    );
}