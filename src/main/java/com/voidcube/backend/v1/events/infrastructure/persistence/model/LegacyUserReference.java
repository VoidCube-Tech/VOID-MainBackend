package com.voidcube.backend.v1.events.infrastructure.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import com.voidcube.backend.core.utils.UiidUtils;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "legacy_user_references", schema = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LegacyUserReference {

    @Id
    @Builder.Default
    private UUID id = UiidUtils.generateV7();

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_user_id", nullable = false)
    private ServiceUser serviceUser;

    @Column(name = "registration_code", nullable = false, length = 100)
    private String registrationCode;

    @Column(name = "source_type", nullable = false, length = 50)
    @Builder.Default
    private String sourceType = "LEGACY_MATRICULA";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}