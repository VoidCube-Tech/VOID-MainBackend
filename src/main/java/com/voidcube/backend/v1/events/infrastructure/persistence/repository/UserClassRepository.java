package com.voidcube.backend.v1.events.infrastructure.persistence.repository;

import com.voidcube.backend.v1.events.infrastructure.persistence.model.UserClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserClassRepository extends JpaRepository<UserClass, UUID> {

    @Query("SELECT c FROM UserClass c WHERE c.companyId = :companyId AND c.deletedAt IS NULL")
    List<UserClass> findAllActiveByCompanyId(@Param("companyId") UUID companyId);

    Optional<UserClass> findByIdAndCompanyIdAndDeletedAtIsNull(UUID id, UUID companyId);
}