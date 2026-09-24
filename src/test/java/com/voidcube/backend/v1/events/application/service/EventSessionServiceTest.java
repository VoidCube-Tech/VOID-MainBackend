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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventSessionServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventSessionRepository eventSessionRepository;

    @InjectMocks
    private EventSessionService sessionService;

    private UUID companyId;
    private UUID eventId;
    private UUID sessionId;
    private Event activeEvent;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        activeEvent = Event.builder()
                .id(eventId)
                .companyId(companyId)
                .title("Workshop Cloud")
                .status(EventStatus.DRAFT)
                .build();
    }

    @Test
    @DisplayName("Deve criar sessão vinculada ao evento com status SCHEDULED")
    void createSession_success() {
        OffsetDateTime start = OffsetDateTime.now().plusDays(2);
        OffsetDateTime end = start.plusHours(3);
        CreateSessionRequest request = new CreateSessionRequest("Mesa Redonda", "Sala A", start, end, 50);

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(activeEvent));
        when(eventSessionRepository.save(any(EventSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessionResponse response = sessionService.createSession(eventId, request, companyId);

        assertNotNull(response);
        assertEquals("Mesa Redonda", response.title());
        assertEquals("Sala A", response.location());
        assertEquals(50, response.capacity());
        assertEquals(SessionStatus.SCHEDULED, response.status());

        ArgumentCaptor<EventSession> captor = ArgumentCaptor.forClass(EventSession.class);
        verify(eventSessionRepository).save(captor.capture());
        EventSession saved = captor.getValue();
        assertEquals(activeEvent, saved.getEvent());
        assertEquals(companyId, saved.getCompanyId());
    }

    @Test
    @DisplayName("Deve rejeitar criação de sessão com endAt anterior a startAt")
    void createSession_invalidDates_throwsException() {
        OffsetDateTime start = OffsetDateTime.now().plusDays(2);
        OffsetDateTime end = start.minusHours(1);
        CreateSessionRequest request = new CreateSessionRequest("Mesa Redonda", "Sala A", start, end, 50);

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(activeEvent));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                sessionService.createSession(eventId, request, companyId)
        );

        assertEquals("INVALID_SESSION_DATES", ex.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("Deve rejeitar criação de sessão com capacidade negativa")
    void createSession_negativeCapacity_throwsException() {
        OffsetDateTime start = OffsetDateTime.now().plusDays(2);
        OffsetDateTime end = start.plusHours(1);
        CreateSessionRequest request = new CreateSessionRequest("Mesa Redonda", "Sala A", start, end, -5);

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(activeEvent));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                sessionService.createSession(eventId, request, companyId)
        );

        assertEquals("INVALID_CAPACITY", ex.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("Deve rejeitar criação de sessão em evento CANCELLED")
    void createSession_eventCancelled_throwsException() {
        activeEvent.setStatus(EventStatus.CANCELLED);
        OffsetDateTime start = OffsetDateTime.now().plusDays(2);
        OffsetDateTime end = start.plusHours(1);
        CreateSessionRequest request = new CreateSessionRequest("Sessão", "Sala B", start, end, 20);

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(activeEvent));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                sessionService.createSession(eventId, request, companyId)
        );

        assertEquals("EVENT_NOT_MODIFIABLE", ex.getCode());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    @DisplayName("Deve listar sessões ativas do evento")
    void listSessions_success() {
        EventSession session = EventSession.builder()
                .id(sessionId)
                .event(activeEvent)
                .companyId(companyId)
                .title("Keynote")
                .startAt(OffsetDateTime.now().plusDays(1))
                .endAt(OffsetDateTime.now().plusDays(1).plusHours(1))
                .capacity(100)
                .build();

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(activeEvent));
        when(eventSessionRepository.findAllByEventIdAndCompanyIdAndDeletedAtIsNullOrderByStartAtAsc(eventId, companyId))
                .thenReturn(List.of(session));

        List<SessionResponse> result = sessionService.listSessions(eventId, companyId);

        assertEquals(1, result.size());
        assertEquals("Keynote", result.get(0).title());
    }

    @Test
    @DisplayName("Deve buscar sessão por ID com sucesso")
    void getSessionById_success() {
        EventSession session = EventSession.builder()
                .id(sessionId)
                .event(activeEvent)
                .companyId(companyId)
                .title("Keynote")
                .startAt(OffsetDateTime.now().plusDays(1))
                .endAt(OffsetDateTime.now().plusDays(1).plusHours(1))
                .capacity(100)
                .build();

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(activeEvent));
        when(eventSessionRepository.findByIdAndEventIdAndCompanyIdAndDeletedAtIsNull(sessionId, eventId, companyId))
                .thenReturn(Optional.of(session));

        SessionResponse response = sessionService.getSessionById(eventId, sessionId, companyId);

        assertEquals(sessionId, response.id());
        assertEquals("Keynote", response.title());
    }

    @Test
    @DisplayName("Deve lançar NOT_FOUND quando sessão não existir")
    void getSessionById_notFound() {
        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(activeEvent));
        when(eventSessionRepository.findByIdAndEventIdAndCompanyIdAndDeletedAtIsNull(sessionId, eventId, companyId))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                sessionService.getSessionById(eventId, sessionId, companyId)
        );

        assertEquals("SESSION_NOT_FOUND", ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("Deve atualizar sessão com sucesso")
    void updateSession_success() {
        EventSession session = EventSession.builder()
                .id(sessionId)
                .event(activeEvent)
                .companyId(companyId)
                .title("Título Original")
                .location("Sala 1")
                .startAt(OffsetDateTime.now().plusDays(1))
                .endAt(OffsetDateTime.now().plusDays(1).plusHours(1))
                .capacity(50)
                .version(1L)
                .status(SessionStatus.SCHEDULED)
                .build();

        OffsetDateTime newStart = OffsetDateTime.now().plusDays(3);
        OffsetDateTime newEnd = newStart.plusHours(2);
        UpdateSessionRequest request = new UpdateSessionRequest("Novo Título", "Sala VIP", newStart, newEnd, 80, 1L);

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(activeEvent));
        when(eventSessionRepository.findByIdAndEventIdAndCompanyIdAndDeletedAtIsNull(sessionId, eventId, companyId))
                .thenReturn(Optional.of(session));
        when(eventSessionRepository.save(any(EventSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessionResponse response = sessionService.updateSession(eventId, sessionId, request, companyId);

        assertEquals("Novo Título", response.title());
        assertEquals("Sala VIP", response.location());
        assertEquals(80, response.capacity());
    }

    @Test
    @DisplayName("Deve cancelar sessão agendada")
    void cancelSession_success() {
        EventSession session = EventSession.builder()
                .id(sessionId)
                .event(activeEvent)
                .companyId(companyId)
                .status(SessionStatus.SCHEDULED)
                .build();

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(activeEvent));
        when(eventSessionRepository.findByIdAndEventIdAndCompanyIdAndDeletedAtIsNull(sessionId, eventId, companyId))
                .thenReturn(Optional.of(session));
        when(eventSessionRepository.save(any(EventSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessionResponse response = sessionService.cancelSession(eventId, sessionId, companyId);

        assertEquals(SessionStatus.CANCELLED, response.status());
    }

    @Test
    @DisplayName("Deve realizar soft delete da sessão")
    void deleteSession_success() {
        EventSession session = EventSession.builder()
                .id(sessionId)
                .event(activeEvent)
                .companyId(companyId)
                .build();

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(activeEvent));
        when(eventSessionRepository.findByIdAndEventIdAndCompanyIdAndDeletedAtIsNull(sessionId, eventId, companyId))
                .thenReturn(Optional.of(session));

        sessionService.deleteSession(eventId, sessionId, companyId);

        assertNotNull(session.getDeletedAt());
        verify(eventSessionRepository).save(session);
    }
}