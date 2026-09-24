package com.voidcube.backend.v1.events.api;

import com.voidcube.backend.core.exception.BusinessException;
import com.voidcube.backend.core.exception.GlobalExceptionHandler;
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
import com.voidcube.backend.v1.events.domain.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @Mock
    private EventSessionService eventSessionService;

    @Spy
    private ServiceUserSecurityGuard securityGuard = new ServiceUserSecurityGuard();

    private EventController controller;
    private MockMvc mockMvc;

    private UserPrincipal currentPrincipal;
    private UUID companyId;
    private UUID managerId;
    private UUID eventId;
    private UUID sessionId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        sessionId = UUID.randomUUID();

        currentPrincipal = new UserPrincipal(
                managerId,
                "gestor@empresa.com",
                companyId,
                false,
                "PLATFORM"
        );

        controller = new EventController(eventService, eventSessionService, securityGuard);

        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return currentPrincipal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    @DisplayName("POST /v1/events - Gestor cria evento com sucesso (201 CREATED)")
    void createEvent_asManager_returns201() throws Exception {
        EventResponse response = new EventResponse(
                eventId,
                companyId,
                managerId,
                "Conferência Global",
                "Descrição do evento",
                EventStatus.DRAFT,
                0L,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                List.of()
        );

        when(eventService.createEvent(any(CreateEventRequest.class), eq(companyId), eq(managerId)))
                .thenReturn(response);

        String json = """
                {
                    "title": "Conferência Global",
                    "description": "Descrição do evento"
                }
                """;

        mockMvc.perform(post("/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.title").value("Conferência Global"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("POST /v1/events - Rejeita título vazio com 400 VALIDATION_FAILED")
    void createEvent_invalidTitle_returns400() throws Exception {
        String json = """
                {
                    "title": "",
                    "description": "Descrição"
                }
                """;

        mockMvc.perform(post("/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /v1/events - Rejeita criação por SERVICE_USER com 403 EVENT_MANAGEMENT_DENIED")
    void createEvent_asServiceUser_returns403() throws Exception {
        currentPrincipal = new UserPrincipal(
                UUID.randomUUID(),
                "usuario@empresa.com",
                companyId,
                false,
                "SERVICE_USER"
        );

        String json = """
                {
                    "title": "Hackathon",
                    "description": "Evento"
                }
                """;

        mockMvc.perform(post("/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error_code").value("EVENT_MANAGEMENT_DENIED"));
    }

    @Test
    @DisplayName("GET /v1/events - Lista eventos com sucesso (200 OK)")
    void listEvents_returns200() throws Exception {
        EventResponse response = new EventResponse(
                eventId,
                companyId,
                managerId,
                "Conferência Global",
                "Descrição",
                EventStatus.PUBLISHED,
                0L,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                List.of()
        );

        when(eventService.listEvents(eq(companyId), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$[0].title").value("Conferência Global"));
    }

    @Test
    @DisplayName("GET /v1/events/{id} - SERVICE_USER não acessa evento em DRAFT (404 EVENT_NOT_AVAILABLE)")
    void getEvent_asServiceUser_draftEvent_returns404() throws Exception {
        currentPrincipal = new UserPrincipal(
                UUID.randomUUID(),
                "usuario@empresa.com",
                companyId,
                false,
                "SERVICE_USER"
        );

        EventResponse draftResponse = new EventResponse(
                eventId,
                companyId,
                managerId,
                "Conferência em Rascunho",
                "Descrição",
                EventStatus.DRAFT,
                0L,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                List.of()
        );

        when(eventService.getEventById(eventId, companyId)).thenReturn(draftResponse);

        mockMvc.perform(get("/v1/events/" + eventId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value("EVENT_NOT_AVAILABLE"));
    }

    @Test
    @DisplayName("PUT /v1/events/{id} - Atualiza evento com sucesso (200 OK)")
    void updateEvent_returns200() throws Exception {
        EventResponse updated = new EventResponse(
                eventId,
                companyId,
                managerId,
                "Título Atualizado",
                "Nova Descrição",
                EventStatus.DRAFT,
                1L,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                List.of()
        );

        when(eventService.updateEvent(eq(eventId), any(UpdateEventRequest.class), eq(companyId)))
                .thenReturn(updated);

        String json = """
                {
                    "title": "Título Atualizado",
                    "description": "Nova Descrição",
                    "version": 0
                }
                """;

        mockMvc.perform(put("/v1/events/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Título Atualizado"));
    }

    @Test
    @DisplayName("PATCH /v1/events/{id}/publish - Publica evento com sucesso (200 OK)")
    void publishEvent_returns200() throws Exception {
        EventResponse published = new EventResponse(
                eventId,
                companyId,
                managerId,
                "Título",
                "Descrição",
                EventStatus.PUBLISHED,
                1L,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                List.of()
        );

        when(eventService.publishEvent(eventId, companyId)).thenReturn(published);

        mockMvc.perform(patch("/v1/events/" + eventId + "/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("PATCH /v1/events/{id}/cancel - Cancela evento com sucesso (200 OK)")
    void cancelEvent_returns200() throws Exception {
        EventResponse cancelled = new EventResponse(
                eventId,
                companyId,
                managerId,
                "Título",
                "Descrição",
                EventStatus.CANCELLED,
                1L,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                List.of()
        );

        when(eventService.cancelEvent(eventId, companyId)).thenReturn(cancelled);

        mockMvc.perform(patch("/v1/events/" + eventId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("DELETE /v1/events/{id} - Exclui evento logicamente com sucesso (204 NO_CONTENT)")
    void deleteEvent_returns204() throws Exception {
        doNothing().when(eventService).deleteEvent(eventId, companyId);

        mockMvc.perform(delete("/v1/events/" + eventId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /v1/events/{eventId}/sessions - Cria sessão com sucesso (201 CREATED)")
    void createSession_returns201() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().plusDays(2);
        OffsetDateTime end = start.plusHours(2);

        SessionResponse sessionResponse = new SessionResponse(
                sessionId,
                eventId,
                companyId,
                "Sessão 1",
                "Auditório Central",
                start,
                end,
                50,
                SessionStatus.SCHEDULED,
                0L,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(eventSessionService.createSession(eq(eventId), any(CreateSessionRequest.class), eq(companyId)))
                .thenReturn(sessionResponse);

        String json = String.format("""
                {
                    "title": "Sessão 1",
                    "location": "Auditório Central",
                    "startAt": "%s",
                    "endAt": "%s",
                    "capacity": 50
                }
                """, start, end);

        mockMvc.perform(post("/v1/events/" + eventId + "/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.title").value("Sessão 1"))
                .andExpect(jsonPath("$.capacity").value(50));
    }

    @Test
    @DisplayName("GET /v1/events/{eventId}/sessions - Lista sessões do evento (200 OK)")
    void listSessions_returns200() throws Exception {
        SessionResponse sessionResponse = new SessionResponse(
                sessionId,
                eventId,
                companyId,
                "Sessão 1",
                "Auditório Central",
                OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(1),
                50,
                SessionStatus.SCHEDULED,
                0L,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(eventSessionService.listSessions(eventId, companyId)).thenReturn(List.of(sessionResponse));

        mockMvc.perform(get("/v1/events/" + eventId + "/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sessionId.toString()));
    }

    @Test
    @DisplayName("PATCH /v1/events/{eventId}/sessions/{sessionId}/cancel - Cancela sessão (200 OK)")
    void cancelSession_returns200() throws Exception {
        SessionResponse cancelledSession = new SessionResponse(
                sessionId,
                eventId,
                companyId,
                "Sessão 1",
                "Auditório Central",
                OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(1),
                50,
                SessionStatus.CANCELLED,
                1L,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(eventSessionService.cancelSession(eventId, sessionId, companyId)).thenReturn(cancelledSession);

        mockMvc.perform(patch("/v1/events/" + eventId + "/sessions/" + sessionId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("DELETE /v1/events/{eventId}/sessions/{sessionId} - Exclui sessão logicamente (204 NO_CONTENT)")
    void deleteSession_returns204() throws Exception {
        doNothing().when(eventSessionService).deleteSession(eventId, sessionId, companyId);

        mockMvc.perform(delete("/v1/events/" + eventId + "/sessions/" + sessionId))
                .andExpect(status().isNoContent());
    }
}