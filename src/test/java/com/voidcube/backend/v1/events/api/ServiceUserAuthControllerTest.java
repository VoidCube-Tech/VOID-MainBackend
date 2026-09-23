package com.voidcube.backend.v1.events.api;

import com.voidcube.backend.core.exception.BusinessException;
import com.voidcube.backend.core.exception.GlobalExceptionHandler;
import com.voidcube.backend.core.security.UserPrincipal;
import com.voidcube.backend.v1.events.application.dto.request.ServiceUserChangePasswordRequest;
import com.voidcube.backend.v1.events.application.dto.request.ServiceUserLoginRequest;
import com.voidcube.backend.v1.events.application.dto.response.ServiceUserAuthResponse;
import com.voidcube.backend.v1.events.application.service.ServiceUserAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ServiceUserAuthControllerTest {

    @Mock
    private ServiceUserAuthService authService;

    @InjectMocks
    private ServiceUserAuthController controller;

    private MockMvc mockMvc;
    private UserPrincipal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = new UserPrincipal(
                UUID.randomUUID(),
                "colaborador@voidcube.com",
                UUID.randomUUID(),
                false,
                "SERVICE_USER"
        );

        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return mockPrincipal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    @DisplayName("POST /v1/auth/service-users/login - Deve retornar 200 com token e flag de troca obrigatória")
    void login_Returns200_WithTemporaryPassword() throws Exception {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ServiceUserAuthResponse response = ServiceUserAuthResponse.of(
                "token.jwt.mock",
                userId,
                "Colaborador Teste",
                "colaborador@voidcube.com",
                companyId,
                true
        );

        when(authService.login(any(ServiceUserLoginRequest.class), eq(companyId))).thenReturn(response);

        String jsonBody = """
                {
                    "identifier": "MATR-001",
                    "password": "SenhaTemp123",
                    "companyId": "%s"
                }
                """.formatted(companyId);

        mockMvc.perform(post("/v1/auth/service-users/login")
                        .header("X-Company-ID", companyId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token.jwt.mock"))
                .andExpect(jsonPath("$.temporaryPassword").value(true))
                .andExpect(jsonPath("$.passwordChangeRequired").value(true))
                .andExpect(jsonPath("$.serviceUserId").value(userId.toString()));
    }

    @Test
    @DisplayName("POST /v1/auth/service-users/login - Deve retornar 400 quando campos obrigatórios estiverem vazios")
    void login_Returns400_WhenValidationFails() throws Exception {
        String invalidBody = """
                {
                    "identifier": "",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/v1/auth/service-users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_fields.identifier").exists())
                .andExpect(jsonPath("$.invalid_fields.password").exists());
    }

    @Test
    @DisplayName("POST /v1/auth/service-users/login - Deve retornar 401 com ProblemDetail quando credenciais forem inválidas")
    void login_Returns401_WhenInvalidCredentials() throws Exception {
        UUID companyId = UUID.randomUUID();

        when(authService.login(any(ServiceUserLoginRequest.class), any()))
                .thenThrow(new BusinessException("INVALID_CREDENTIALS", "Identificador ou senha inválidos.", HttpStatus.UNAUTHORIZED));

        String jsonBody = """
                {
                    "identifier": "MATR-001",
                    "password": "senhaErrada",
                    "companyId": "%s"
                }
                """.formatted(companyId);

        mockMvc.perform(post("/v1/auth/service-users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.detail").value("Identificador ou senha inválidos."));
    }

    @Test
    @DisplayName("POST /v1/auth/service-users/change-password - Deve retornar 200 ao alterar senha com sucesso")
    void changePassword_Returns200_WhenValid() throws Exception {
        UUID userId = mockPrincipal.id();
        ServiceUserAuthResponse response = ServiceUserAuthResponse.of(
                "token.refreshed.mock",
                userId,
                "Colaborador Teste",
                "colaborador@voidcube.com",
                mockPrincipal.companyId(),
                false
        );

        when(authService.changePassword(eq(userId), any(ServiceUserChangePasswordRequest.class)))
                .thenReturn(response);

        String jsonBody = """
                {
                    "currentPassword": "SenhaTemp123",
                    "newPassword": "NovaSenhaForte@2026"
                }
                """;

        mockMvc.perform(post("/v1/auth/service-users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token.refreshed.mock"))
                .andExpect(jsonPath("$.temporaryPassword").value(false));
    }
}