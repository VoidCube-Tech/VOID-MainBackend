package com.voidcube.backend.v1.events.application.service;

import com.voidcube.backend.core.exception.BusinessException;
import com.voidcube.backend.v1.events.application.dto.request.CreateSessionRequest;
import com.voidcube.backend.v1.events.application.dto.request.UpdateSessionRequest;
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
public class EventSessionService {

    private final EventRepository eventRepository;
    private final EventSessionRepository eventSessionRepository;

    public EventSessionService(EventRepository eventRepository, EventSessionRepository eventSessionRepository) {
        this.eventRepository = eventRepository;
        this.eventSessionRepository = eventSessionRepository;
    }

    @Transactional
    public SessionResponse createSession(UUID eventId, CreateSessionRequest request, UUID companyId) {
        Event event = findActiveEvent(eventId, companyId);
        validateEventModifiable(event);

        if (!request.endAt().isAfter(request.startAt())) {
            throw new BusinessException(
                    "INVALID_SESSION_DATES",
                    "A data/hora de término deve ser posterior à data/hora de início.",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (request.capacity() < 0) {
            throw new BusinessException(
                    "INVALID_CAPACITY",
                    "A capacidade não pode ser negativa.",
                    HttpStatus.BAD_REQUEST
            );
        }

        EventSession session = EventSession.builder()
                .event(event)
                .companyId(companyId)
                .title(request.title().trim())
                .location(request.location().trim())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .capacity(request.capacity())
                .status(SessionStatus.SCHEDULED)
                .build();

        EventSession saved = eventSessionRepository.save(session);
        return SessionResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> listSessions(UUID eventId, UUID companyId) {
        findActiveEvent(eventId, companyId);
        return eventSessionRepository.findAllByEventIdAndCompanyIdAndDeletedAtIsNullOrderByStartAtAsc(eventId, companyId)
                .stream()
                .map(SessionResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionById(UUID eventId, UUID sessionId, UUID companyId) {
        findActiveEvent(eventId, companyId);
        EventSession session = findActiveSession(sessionId, eventId, companyId);
        return SessionResponse.fromEntity(session);
    }

    @Transactional
    public SessionResponse updateSession(UUID eventId, UUID sessionId, UpdateSessionRequest request, UUID companyId) {
        Event event = findActiveEvent(eventId, companyId);
        validateEventModifiable(event);

        EventSession session = findActiveSession(sessionId, eventId, companyId);

        if (session.getStatus() == SessionStatus.CANCELLED || session.getStatus() == SessionStatus.COMPLETED) {
            throw new BusinessException(
                    "SESSION_NOT_MODIFIABLE",
                    "Sessões canceladas ou concluídas não podem ser alteradas.",
                    HttpStatus.CONFLICT
            );
        }

        if (request.version() != null && !request.version().equals(session.getVersion())) {
            throw new BusinessException(
                    "EVENT_VERSION_CONFLICT",
                    "A sessão foi modificada concorrentemente por outro usuário.",
                    HttpStatus.CONFLICT
            );
        }

        if (!request.endAt().isAfter(request.startAt())) {
            throw new BusinessException(
                    "INVALID_SESSION_DATES",
                    "A data/hora de término deve ser posterior à data/hora de início.",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (request.capacity() < 0) {
            throw new BusinessException(
                    "INVALID_CAPACITY",
                    "A capacidade não pode ser negativa.",
                    HttpStatus.BAD_REQUEST
            );
        }

        session.setTitle(request.title().trim());
        session.setLocation(request.location().trim());
        session.setStartAt(request.startAt());
        session.setEndAt(request.endAt());
        session.setCapacity(request.capacity());

        EventSession saved = eventSessionRepository.save(session);
        return SessionResponse.fromEntity(saved);
    }

    @Transactional
    public SessionResponse cancelSession(UUID eventId, UUID sessionId, UUID companyId) {
        findActiveEvent(eventId, companyId);
        EventSession session = findActiveSession(sessionId, eventId, companyId);

        if (session.getStatus() == SessionStatus.CANCELLED) {
            return SessionResponse.fromEntity(session);
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new BusinessException(
                    "SESSION_INVALID_STATE",
                    "Sessões concluídas não podem ser canceladas.",
                    HttpStatus.BAD_REQUEST
            );
        }

        session.setStatus(SessionStatus.CANCELLED);
        EventSession saved = eventSessionRepository.save(session);
        return SessionResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteSession(UUID eventId, UUID sessionId, UUID companyId) {
        findActiveEvent(eventId, companyId);
        EventSession session = findActiveSession(sessionId, eventId, companyId);

        session.setDeletedAt(OffsetDateTime.now());
        eventSessionRepository.save(session);
    }

    private Event findActiveEvent(UUID eventId, UUID companyId) {
        if (companyId == null) {
            throw new BusinessException("COMPANY_REQUIRED", "O identificador da empresa é obrigatório.", HttpStatus.BAD_REQUEST);
        }
        return eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId)
                .orElseThrow(() -> new BusinessException("EVENT_NOT_FOUND", "Evento não encontrado.", HttpStatus.NOT_FOUND));
    }

    private EventSession findActiveSession(UUID sessionId, UUID eventId, UUID companyId) {
        return eventSessionRepository.findByIdAndEventIdAndCompanyIdAndDeletedAtIsNull(sessionId, eventId, companyId)
                .orElseThrow(() -> new BusinessException("SESSION_NOT_FOUND", "Sessão não encontrada.", HttpStatus.NOT_FOUND));
    }

    private void validateEventModifiable(Event event) {
        if (event.getStatus() == EventStatus.CANCELLED
                || event.getStatus() == EventStatus.FINISHED
                || event.getStatus() == EventStatus.ARCHIVED) {
            throw new BusinessException(
                    "EVENT_NOT_MODIFIABLE",
                    "Não é possível adicionar ou modificar sessões em eventos cancelados, finalizados ou arquivados.",
                    HttpStatus.CONFLICT
            );
        }
    }
}