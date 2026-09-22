package com.voidcube.backend.v1.events.infrastructure.persistence.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode 
public class UserClassMembershipId {
    
    @Column(name = "service_user_id")
    private UUID serviceUserId;

    @Column(name = "class_id")
    private UUID classId;
}
