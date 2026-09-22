package com.voidcube.backend.v1.events.infrastructure.persistence.repository;

import com.voidcube.backend.v1.events.infrastructure.persistence.model.ServiceUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ServiceUserRepository extends JpaRepository<ServiceUser, UUID> {

    Optional<ServiceUser> findByIdAndCompanyIdAndDeletedAtIsNull(UUID id, UUID companyId);

    @Query("SELECT s FROM ServiceUser s WHERE s.companyId = :companyId AND s.email = :email AND s.deletedAt IS NULL")
    Optional<ServiceUser> findActiveByCompanyIdAndEmail(@Param("companyId") UUID companyId, @Param("email") String email);
}