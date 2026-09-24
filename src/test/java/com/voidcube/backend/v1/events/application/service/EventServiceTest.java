package com.voidcube.backend.v1.events.application.service;

import com.voidcube.backend.core.exception.BusinessException;
import com.voidcube.backend.v1.events.application.dto.request.CreateEventRequest;
import com.voidcube.backend.v1.events.application.dto.request.UpdateEventRequest;
import com.voidcube.backend.v1.events.application.dto.response.EventResponse;
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
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventSessionRepository eventSessionRepository;

    @InjectMocks
    private EventService eventService;

    private UUID companyId;
    private UUID managerId;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        eventId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve criar evento com status DRAFT e persistir corretamente")
    void createEvent_success() {
        CreateEventRequest request = new CreateEventRequest("Tech Summit 2026", "Maior conferência de tecnologia");

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.createEvent(request, companyId, managerId);

        assertNotNull(response);
        assertEquals("Tech Summit 2026", response.title());
        assertEquals("Maior conferência de tecnologia", response.description());
        assertEquals(EventStatus.DRAFT, response.status());
        assertEquals(companyId, response.companyId());
        assertEquals(managerId, response.ownerManagerId());

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        Event captured = captor.getValue();
        assertEquals(EventStatus.DRAFT, captured.getStatus());
        assertEquals("Tech Summit 2026", captured.getTitle());
    }

    @Test
    @DisplayName("Deve lançar exceção se companyId for nulo na criação")
    void createEvent_missingCompanyId_throwsException() {
        CreateEventRequest request = new CreateEventRequest("Tech Summit", "Desc");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                eventService.createEvent(request, null, managerId)
        );

        assertEquals("COMPANY_REQUIRED", ex.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção se managerId for nulo na criação")
    void createEvent_missingManagerId_throwsException() {
        CreateEventRequest request = new CreateEventRequest("Tech Summit", "Desc");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                eventService.createEvent(request, companyId, null)
        );

        assertEquals("MANAGER_REQUIRED", ex.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("Deve buscar evento ativo por ID e carregar sessões associadas")
    void getEventById_success() {
        Event event = Event.builder()
                .id(eventId)
                .companyId(companyId)
                .ownerManagerId(managerId)
                .title("Tech Summit")
                .status(EventStatus.DRAFT)
                .build();

        EventSession session = EventSession.builder()
                .id(UUID.randomUUID())
                .event(event)
                .companyId(companyId)
                .title("Palestra de Abertura")
                .location("Auditório 1")
                .startAt(OffsetDateTime.now().plusDays(1))
                .endAt(OffsetDateTime.now().plusDays(1).plusHours(2))
                .capacity(100)
                .status(SessionStatus.SCHEDULED)
                .build();

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(event));
        when(eventSessionRepository.findAllByEventIdAndCompanyIdAndDeletedAtIsNullOrderByStartAtAsc(eventId, companyId))
                .thenReturn(List.of(session));

        EventResponse response = eventService.getEventById(eventId, companyId);

        assertNotNull(response);
        assertEquals(eventId, response.id());
        assertEquals(1, response.sessions().size());
        assertEquals("Palestra de Abertura", response.sessions().get(0).title());
    }

    @Test
    @DisplayName("Deve lançar NOT_FOUND quando evento não for encontrado")
    void getEventById_notFound_throwsException() {
        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                eventService.getEventById(eventId, companyId)
        );

        assertEquals("EVENT_NOT_FOUND", ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("Deve listar eventos da empresa sem filtro de status")
    void listEvents_all() {
        Event event1 = Event.builder().companyId(companyId).title("E1").build();
        Event event2 = Event.builder().companyId(companyId).title("E2").build();

        when(eventRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId))
                .thenReturn(List.of(event1, event2));

        List<EventResponse> result = eventService.listEvents(companyId, null);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Deve listar eventos da empresa filtrados por status")
    void listEvents_filteredByStatus() {
        Event event = Event.builder().companyId(companyId).title("E1").status(EventStatus.PUBLISHED).build();

        when(eventRepository.findAllByCompanyIdAndStatusAndDeletedAtIsNull(companyId, EventStatus.PUBLISHED))
                .thenReturn(List.of(event));

        List<EventResponse> result = eventService.listEvents(companyId, EventStatus.PUBLISHED);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Deve atualizar título e descrição de evento com sucesso")
    void updateEvent_success() {
        Event event = Event.builder()
                .id(eventId)
                .companyId(companyId)
                .ownerManagerId(managerId)
                .title("Título Antigo")
                .description("Descrição Antiga")
                .status(EventStatus.DRAFT)
                .version(1L)
                .build();

        UpdateEventRequest request = new UpdateEventRequest("Novo Título", "Nova Descrição", 1L);

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.updateEvent(eventId, request, companyId);

        assertEquals("Novo Título", response.title());
        assertEquals("Nova Descrição", response.description());
    }

    @Test
    @DisplayName("Deve lançar conflito quando versão otimista não bater")
    void updateEvent_versionConflict() {
        Event event = Event.builder()
                .id(eventId)
                .companyId(companyId)
                .title("Título")
                .status(EventStatus.DRAFT)
                .version(2L)
                .build();

        UpdateEventRequest request = new UpdateEventRequest("Novo Título", "Nova Descrição", 1L);

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(event));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                eventService.updateEvent(eventId, request, companyId)
        );

        assertEquals("EVENT_VERSION_CONFLICT", ex.getCode());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    @DisplayName("Deve lançar conflito se tentar alterar evento CANCELLED")
    void updateEvent_cancelled_throwsException() {
        Event event = Event.builder()
                .id(eventId)
                .companyId(companyId)
                .title("Título")
                .status(EventStatus.CANCELLED)
                .build();

        UpdateEventRequest request = new UpdateEventRequest("Novo Título", "Nova Descrição", null);

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(event));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                eventService.updateEvent(eventId, request, companyId)
        );

        assertEquals("EVENT_NOT_MODIFIABLE", ex.getCode());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    @DisplayName("Deve publicar evento DRAFT que possua ao menos uma sessão")
    void publishEvent_success() {
        Event event = Event.builder()
                .id(eventId)
                .companyId(companyId)
                .status(EventStatus.DRAFT)
                .build();

        EventSession session = EventSession.builder()
                .id(UUID.randomUUID())
                .event(event)
                .companyId(companyId)
                .title("Sessão 1")
                .build();

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(event));
        when(eventSessionRepository.findAllByEventIdAndCompanyIdAndDeletedAtIsNullOrderByStartAtAsc(eventId, companyId))
                .thenReturn(List.of(session));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.publishEvent(eventId, companyId);

        assertEquals(EventStatus.PUBLISHED, response.status());
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar publicar evento sem sessões")
    void publishEvent_withoutSessions_throwsException() {
        Event event = Event.builder()
                .id(eventId)
                .companyId(companyId)
                .status(EventStatus.DRAFT)
                .build();

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(event));
        when(eventSessionRepository.findAllByEventIdAndCompanyIdAndDeletedAtIsNullOrderByStartAtAsc(eventId, companyId))
                .thenReturn(List.of());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                eventService.publishEvent(eventId, companyId)
        );

        assertEquals("EVENT_HAS_NO_SESSIONS", ex.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("Deve cancelar evento e colocar todas as sessões ativas em CANCELLED")
    void cancelEvent_success() {
        Event event = Event.builder()
                .id(eventId)
                .companyId(companyId)
                .status(EventStatus.PUBLISHED)
                .build();

        EventSession session1 = EventSession.builder()
                .id(UUID.randomUUID())
                .event(event)
                .companyId(companyId)
                .status(SessionStatus.SCHEDULED)
                .build();

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(event));
        when(eventSessionRepository.findAllByEventIdAndCompanyIdAndDeletedAtIsNullOrderByStartAtAsc(eventId, companyId))
                .thenReturn(List.of(session1));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.cancelEvent(eventId, companyId);

        assertEquals(EventStatus.CANCELLED, response.status());
        verify(eventSessionRepository).save(session1);
        assertEquals(SessionStatus.CANCELLED, session1.getStatus());
    }

    @Test
    @DisplayName("Deve realizar soft delete em evento e suas sessões")
    void deleteEvent_success() {
        Event event = Event.builder()
                .id(eventId)
                .companyId(companyId)
                .status(EventStatus.DRAFT)
                .build();

        EventSession session = EventSession.builder()
                .id(UUID.randomUUID())
                .event(event)
                .companyId(companyId)
                .build();

        when(eventRepository.findByIdAndCompanyIdAndDeletedAtIsNull(eventId, companyId))
                .thenReturn(Optional.of(event));
        when(eventSessionRepository.findAllByEventIdAndCompanyIdAndDeletedAtIsNullOrderByStartAtAsc(eventId, companyId))
                .thenReturn(List.of(session));

        eventService.deleteEvent(eventId, companyId);

        assertNotNull(event.getDeletedAt());
        assertNotNull(session.getDeletedAt());
        verify(eventRepository).save(event);
        verify(eventSessionRepository).save(session);
    }
}