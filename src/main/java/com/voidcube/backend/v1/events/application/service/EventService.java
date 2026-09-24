package com.voidcube.backend.v1.events.application.service;

import com.voidcube.backend.core.exception.BusinessException;
import com.voidcube.backend.v1.events.application.dto.request.CreateEventRequest;
import com.voidcube.backend.v1.events.application.dto.request.UpdateEventRequest;
import com.voidcube.backend.v1.events.application.dto.response.EventResponse;
import com.voidcube.backend.v1.events.application.dto.response.SessionResponse;
import com.voidcube.backend.v1.events.domain.EventStatus;
import com.voidcube.backend.v1.events.domain.SessionStatus;
import com.voidcube.backend.v1.events.infrastructure.persistence.model.Event;
import com.voidcube.backend.v1.events.infrastructure.persistence.model.EventSession;
import com.voidcube.backend.v1.events.infrastructure.persistence.repository.EventRepository;
import com.voidcube.backend.v1.events.infrastructure.persistence.repository.EventSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventSessionRepository eventSessionRepository;

    public EventService(EventRepository eventRepository, EventSessionRepository eventSessionRepository) {
        this.eventRepository = eventRepository;
        this.eventSessionRepository = eventSessionRepository;
    }

    @Transactional
    public EventResponse createEvent(CreateEventRequest request, UUID companyId, UUID managerId) {
        if (companyId == null) {
            throw new BusinessException("COMPANY_REQUIRED", "O identificador da empresa é obrigatório.", HttpStatus.BAD_REQUEST);
        }
        if (managerId == null) {
            throw new BusinessException("MANAGER_REQUIRED", "O identificador do gestor é obrigatório.", HttpStatus.BAD_REQUEST);
        }

        Event event = Event.builder()
                .companyId(companyId)
                .ownerManagerId(managerId)
                .title(request.title().trim())
                .description(request.description())
                .status(EventStatus.DRAFT)
                .build();

        Event saved = eventRepository.save(event);
        return EventResponse.fromEntity(saved, List.of());
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(UUID eventId, UUID companyId) {
        Event event = findActiveEvent(eventId, companyId);
        List<SessionResponse> sessions = loadActiveSessionResponses(eventId, companyId);
        return EventResponse.fromEntity(event, sessions);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> listEvents(UUID companyId, EventStatus status) {
        if (companyId == null) {
            throw new BusinessException("COMPANY_REQUIRED", "O identificador da empresa é obrigatório.", HttpStatus.BAD_REQUEST);
        }

        List<Event> events;
        if (status != null) {
            events = eventRepository.findAllByCompanyIdAndStatusAndDeletedAtIsNull(companyId, status);
        } else {
            events = eventRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId);
        }

        return events.stream()
                .map(EventResponse::fromEntity)
                .toList();
    }

    @Transactional
    public EventResponse updateEvent(UUID eventId, UpdateEventRequest request, UUID companyId) {
        Event event = findActiveEvent(eventId, companyId);

        if (event.getStatus() == EventStatus.CANCELLED
                || event.getStatus() == EventStatus.FINISHED
                || event.getStatus() == EventStatus.ARCHIVED) {
            throw new BusinessException(
                    "EVENT_NOT_MODIFIABLE",
                    "Eventos cancelados, finalizados ou arquivados não podem ser alterados.",
                    HttpStatus.CONFLICT
            );
        }

        if (request.version() != null && !request.version().equals(event.getVersion())) {
            throw new BusinessException(
                    "EVENT_VERSION_CONFLICT",
                    "O evento foi modificado concorrentemente por outro usuário.",
                    HttpStatus.CONFLICT
            );
        }

        event.setTitle(request.title().trim());
        event.setDescription(request.description());

        Event saved = eventRepository.save(event);
        List<SessionResponse> sessions = loadActiveSessionResponses(eventId, companyId);
        return EventResponse.fromEntity(saved, sessions);
    }

    @Transactional
    public EventResponse publishEvent(UUID eventId, UUID companyId) {
        Event event = findActiveEvent(eventId, companyId);

        if (event.getStatus() == EventStatus.PUBLISHED) {
            List<SessionResponse> sessions = loadActiveSessionResponses(eventId, companyId);
            return EventResponse.fromEntity(event, sessions);
        }

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessException(
                    "EVENT_INVALID_STATE",
                    "Apenas eventos em rascunho (DRAFT) podem ser publicados.",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<EventSession> sessions = eventSessionRepository.findAllByEventIdAndCompanyIdAndDeletedAtIsNullOrderByStartAtAsc(eventId, companyId);
        if (sessions.isEmpty()) {
            throw new BusinessException(
                    "EVENT_HAS_NO_SESSIONS",
                    "O evento deve possuir pelo menos uma sessão ativa para ser publicado.",
                    HttpStatus.BAD_REQUEST
            );
        }

        event.setStatus(EventStatus.PUBLISHED);
        Event saved = eventRepository.save(event);

        List<SessionResponse> sessionResponses = sessions.stream()
                .map(SessionResponse::fromEntity)
                .toList();
        return EventResponse.fromEntity(saved, sessionResponses);
    }

    @Transactional
    public EventResponse cancelEvent(UUID eventId, UUID companyId) {
        Event event = findActiveEvent(eventId, companyId);

        if (event.getStatus() == EventStatus.CANCELLED) {
            List<SessionResponse> sessions = loadActiveSessionResponses(eventId, companyId);
            return EventResponse.fromEntity(event, sessions);
        }

        if (event.getStatus() == EventStatus.FINISHED || event.getStatus() == EventStatus.ARCHIVED) {
            throw new BusinessException(
                    "EVENT_INVALID_STATE",
                    "Eventos finalizados ou arquivados não podem ser cancelados.",
                    HttpStatus.BAD_REQUEST
            );
        }

        event.setStatus(EventStatus.CANCELLED);
        Event saved = eventRepository.save(event);

        List<EventSession> sessions = eventSessionRepository.findAllByEventIdAndCompanyIdAndDeletedAtIsNullOrderByStartAtAsc(eventId, companyId);
        for (EventSession session : sessions) {
            if (session.getStatus() != SessionStatus.CANCELLED && session.getStatus() != SessionStatus.COMPLETED) {
                session.setStatus(SessionStatus.CANCELLED);
                eventSessionRepository.save(session);
            }
        }

        List<SessionResponse> sessionResponses = loadActiveSessionResponses(eventId, companyId);
        return EventResponse.fromEntity(saved, sessionResponses);
    }

    @Transactional
    public void deleteEvent(UUID eventId, UUID companyId) {
        Event event = findActiveEvent(eventId, companyId);
        OffsetDateTime now = OffsetDateTime.now();

        event.setDeletedAt(now);
        eventRepository.save(event);

        List<EventSession> sessions = eventSessionRepository.findAllByEventIdAndCompanyIdAndDeletedAtIsNullOrderByStartAtAsc(eventId, companyId);
        for (EventSession session : sessions) {
            session.setDeletedAt(now);
            eventSessionRepository.save(session);
        }
    }

    private Event findActiveEvent(UUID eventId, UUID companyId) {
        if (companyId == null) {
            throw new BusinessException("COMPANY_REQUIRED", "O identificador da empresa é obrigatório.", HttpStatus.BAD_REQUEST);
        }
        return eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId)
                .orElseThrow(() -> new BusinessException("EVENT_NOT_FOUND", "Evento não encontrado.", HttpStatus.NOT_FOUND));
    }

    private List<SessionResponse> loadActiveSessionResponses(UUID eventId, UUID companyId) {
        return eventSessionRepository.findAllByEventIdAndCompanyIdAndDeletedAtIsNullOrderByStartAtAsc(eventId, companyId)
                .stream()
                .map(SessionResponse::fromEntity)
                .toList();
    }
}