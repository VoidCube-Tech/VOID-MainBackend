package com.voidcube.backend.v1.company.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voidcube.backend.v1.company.infrastructure.persistence.model.Company;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findByIdAndDeletedAtIsNull(UUID id);

    @Query("SELECT c FROM Company c WHERE c.document = :document AND c.deletedAt IS NULL")
    Optional<Company> findActiveByDocument(@Param("document") String document);

    @Query("SELECT COUNT(c) > 0 FROM Company c WHERE c.document = :document AND c.deletedAt IS NULL")
    boolean existsActiveByDocument(@Param("document") String document);
}