package com.voidcube.backend.v1.company.infrastructure.persistence.repository;

import com.voidcube.backend.v1.company.infrastructure.persistence.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    @Query("SELECT r FROM Role r WHERE r.company.id = :companyId AND r.deletedAt IS NULL")
    List<Role> findAllActiveByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT r FROM Role r WHERE r.company.id = :companyId AND r.name = :name AND r.deletedAt IS NULL")
    Optional<Role> findActiveByCompanyIdAndName(@Param("companyId") UUID companyId, @Param("name") String name);
}