package com.voidcube.backend.v1.events.api;

import com.voidcube.backend.core.context.CompanyContextHolder;
import com.voidcube.backend.core.exception.BusinessException;
import com.voidcube.backend.core.security.UserPrincipal;
import com.voidcube.backend.v1.events.application.dto.request.CreateEventRequest;
import com.voidcube.backend.v1.events.application.dto.request.CreateSessionRequest;
import com.voidcube.backend.v1.events.application.dto.request.UpdateEventRequest;
import com.voidcube.backend.v1.events.application.dto.request.UpdateSessionRequest;
import com.voidcube.backend.v1.events.application.dto.response.EventResponse;
import com.voidcube.backend.v1.events.application.dto.response.SessionResponse;
import com.voidcube.backend.v1.events.application.service.EventService;
import com.voidcube.backend.v1.events.application.service.EventSessionService;
import com.voidcube.backend.v1.events.application.service.ServiceUserSecurityGuard;
import com.voidcube.backend.v1.events.domain.EventStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/v1/events", "/api/v1/events"})
public class EventController {

    private final EventService eventService;
    private final EventSessionService eventSessionService;
    private final ServiceUserSecurityGuard securityGuard;

    public EventController(
            EventService eventService,
            EventSessionService eventSessionService,
            ServiceUserSecurityGuard securityGuard
    ) {
        this.eventService = eventService;
        this.eventSessionService = eventSessionService;
        this.securityGuard = securityGuard;
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        securityGuard.ensureManagerAccess(principal);
        UUID companyId = resolveCompanyId(principal);
        EventResponse response = eventService.createEvent(request, companyId, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> listEvents(
            @RequestParam(required = false) EventStatus status,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        securityGuard.ensurePasswordChangeNotRequired(principal);
        UUID companyId = resolveCompanyId(principal);

        EventStatus effectiveStatus = status;
        if (principal != null && "SERVICE_USER".equalsIgnoreCase(principal.userType())) {
            if (effectiveStatus == null || effectiveStatus == EventStatus.DRAFT) {
                effectiveStatus = EventStatus.PUBLISHED;
            }
        }

        List<EventResponse> response = eventService.listEvents(companyId, effectiveStatus);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        securityGuard.ensurePasswordChangeNotRequired(principal);
        UUID companyId = resolveCompanyId(principal);
        EventResponse response = eventService.getEventById(id, companyId);

        if (principal != null && "SERVICE_USER".equalsIgnoreCase(principal.userType())
                && response.status() == EventStatus.DRAFT) {
            throw new BusinessException("EVENT_NOT_AVAILABLE", "Evento não disponível.", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        securityGuard.ensureManagerAccess(principal);
        UUID companyId = resolveCompanyId(principal);
        EventResponse response = eventService.updateEvent(id, request, companyId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<EventResponse> publishEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        securityGuard.ensureManagerAccess(principal);
        UUID companyId = resolveCompanyId(principal);
        EventResponse response = eventService.publishEvent(id, companyId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<EventResponse> cancelEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        securityGuard.ensureManagerAccess(principal);
        UUID companyId = resolveCompanyId(principal);
        EventResponse response = eventService.cancelEvent(id, companyId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        securityGuard.ensureManagerAccess(principal);
        UUID companyId = resolveCompanyId(principal);
        eventService.deleteEvent(id, companyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/sessions")
    public ResponseEntity<SessionResponse> createSession(
            @PathVariable UUID eventId,
            @Valid @RequestBody CreateSessionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        securityGuard.ensureManagerAccess(principal);
        UUID companyId = resolveCompanyId(principal);
        SessionResponse response = eventSessionService.createSession(eventId, request, companyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{eventId}/sessions")
    public ResponseEntity<List<SessionResponse>> listSessions(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        securityGuard.ensurePasswordChangeNotRequired(principal);
        UUID companyId = resolveCompanyId(principal);
        List<SessionResponse> response = eventSessionService.listSessions(eventId, companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}/sessions/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(
            @PathVariable UUID eventId,
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        securityGuard.ensurePasswordChangeNotRequired(principal);
        UUID companyId = resolveCompanyId(principal);
        SessionResponse response = eventSessionService.getSessionById(eventId, sessionId, companyId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{eventId}/sessions/{sessionId}")
    public ResponseEntity<SessionResponse> updateSession(
            @PathVariable UUID eventId,
            @PathVariable UUID sessionId,
            @Valid @RequestBody UpdateSessionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        securityGuard.ensureManagerAccess(principal);
        UUID companyId = resolveCompanyId(principal);
        SessionResponse response = eventSessionService.updateSession(eventId, sessionId, request, companyId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{eventId}/sessions/{sessionId}/cancel")
    public ResponseEntity<SessionResponse> cancelSession(
            @PathVariable UUID eventId,
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        securityGuard.ensureManagerAccess(principal);
        UUID companyId = resolveCompanyId(principal);
        SessionResponse response = eventSessionService.cancelSession(eventId, sessionId, companyId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eventId}/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable UUID eventId,
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        securityGuard.ensureManagerAccess(principal);
        UUID companyId = resolveCompanyId(principal);
        eventSessionService.deleteSession(eventId, sessionId, companyId);
        return ResponseEntity.noContent().build();
    }

    private UUID resolveCompanyId(UserPrincipal principal) {
        if (principal != null && principal.companyId() != null) {
            return principal.companyId();
        }
        return CompanyContextHolder.requireCompanyId();
    }
}