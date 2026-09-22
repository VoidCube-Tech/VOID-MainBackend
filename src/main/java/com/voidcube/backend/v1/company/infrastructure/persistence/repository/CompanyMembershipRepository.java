package com.voidcube.backend.v1.company.infrastructure.persistence.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voidcube.backend.v1.company.domain.MembershipStatus;
import com.voidcube.backend.v1.company.infrastructure.persistence.model.CompanyMembership;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, UUID> {

    @Query("""
        SELECT m FROM CompanyMembership m
        WHERE m.company.id = :companyId
          AND m.userIdentityId = :userIdentityId
          AND m.status = :status
        """)
    Optional<CompanyMembership> findByCompanyIdAndUserIdentityIdAndStatus(
            @Param("companyId") UUID companyId,
            @Param("userIdentityId") UUID userIdentityId,
            @Param("status") MembershipStatus status
    );

    @Query("SELECT m FROM CompanyMembership m WHERE m.userIdentityId = :userIdentityId AND m.status = 'ACTIVE'")
    List<CompanyMembership> findAllActiveByUserIdentityId(@Param("userIdentityId") UUID userIdentityId);
}