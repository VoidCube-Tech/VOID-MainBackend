package com.voidcube.backend.v1.events.application.service;

import com.voidcube.backend.core.exception.BusinessException;
import com.voidcube.backend.core.security.JwtProvider;
import com.voidcube.backend.v1.events.application.dto.request.ServiceUserChangePasswordRequest;
import com.voidcube.backend.v1.events.application.dto.request.ServiceUserLoginRequest;
import com.voidcube.backend.v1.events.application.dto.response.ServiceUserAuthResponse;
import com.voidcube.backend.v1.events.domain.ServiceUserStatus;
import com.voidcube.backend.v1.events.infrastructure.persistence.model.LegacyUserReference;
import com.voidcube.backend.v1.events.infrastructure.persistence.model.ServiceUser;
import com.voidcube.backend.v1.events.infrastructure.persistence.repository.LegacyUserReferenceRepository;
import com.voidcube.backend.v1.events.infrastructure.persistence.repository.ServiceUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceUserAuthServiceTest {

    @Mock
    private ServiceUserRepository serviceUserRepository;

    @Mock
    private LegacyUserReferenceRepository legacyUserReferenceRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private ServiceUserAuthService authService;

    private UUID companyId;
    private ServiceUser activeUser;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        activeUser = ServiceUser.builder()
                .companyId(companyId)
                .name("Colaborador Voidcube")
                .email("colaborador@voidcube.com")
                .credentialHash("encodedHash")
                .temporaryPassword(true)
                .status(ServiceUserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Deve autenticar com sucesso via matrícula e identificar senha temporária")
    void login_Success_WithRegistrationCode_TemporaryPasswordTrue() {
        String matricula = "MATR-12345";
        LegacyUserReference ref = LegacyUserReference.builder()
                .companyId(companyId)
                .registrationCode(matricula)
                .serviceUser(activeUser)
                .build();

        when(legacyUserReferenceRepository.findByCompanyIdAndRegistrationCode(companyId, matricula))
                .thenReturn(Optional.of(ref));
        when(passwordEncoder.matches("tempPass123", "encodedHash")).thenReturn(true);
        when(jwtProvider.generateAccessToken(eq(activeUser.getId()), eq("colaborador@voidcube.com"), eq(companyId), eq(true), eq("SERVICE_USER")))
                .thenReturn("mocked.jwt.token");

        ServiceUserLoginRequest request = new ServiceUserLoginRequest(matricula, "tempPass123", companyId);
        ServiceUserAuthResponse response = authService.login(request, companyId);

        assertThat(response.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.temporaryPassword()).isTrue();
        assertThat(response.passwordChangeRequired()).isTrue();
        assertThat(response.serviceUserId()).isEqualTo(activeUser.getId());
    }

    @Test
    @DisplayName("Deve autenticar com sucesso via e-mail e identificar senha definitiva")
    void login_Success_WithEmail_NormalPassword() {
        activeUser.setTemporaryPassword(false);

        when(legacyUserReferenceRepository.findByCompanyIdAndRegistrationCode(companyId, "colaborador@voidcube.com"))
                .thenReturn(Optional.empty());
        when(serviceUserRepository.findActiveByCompanyIdAndEmail(companyId, "colaborador@voidcube.com"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("defPass456", "encodedHash")).thenReturn(true);
        when(jwtProvider.generateAccessToken(eq(activeUser.getId()), eq("colaborador@voidcube.com"), eq(companyId), eq(false), eq("SERVICE_USER")))
                .thenReturn("valid.token");

        ServiceUserLoginRequest request = new ServiceUserLoginRequest("colaborador@voidcube.com", "defPass456", null);
        ServiceUserAuthResponse response = authService.login(request, companyId);

        assertThat(response.accessToken()).isEqualTo("valid.token");
        assertThat(response.temporaryPassword()).isFalse();
        assertThat(response.passwordChangeRequired()).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar login se companyId não for provido")
    void login_Failure_MissingCompanyContext() {
        ServiceUserLoginRequest request = new ServiceUserLoginRequest("qualquer@voidcube.com", "123456", null);

        assertThatThrownBy(() -> authService.login(request, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("COMPANY_CONTEXT_REQUIRED");
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    @DisplayName("Deve rejeitar login com senha incorreta")
    void login_Failure_InvalidPassword() {
        when(legacyUserReferenceRepository.findByCompanyIdAndRegistrationCode(companyId, "colaborador@voidcube.com"))
                .thenReturn(Optional.empty());
        when(serviceUserRepository.findActiveByCompanyIdAndEmail(companyId, "colaborador@voidcube.com"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongPass", "encodedHash")).thenReturn(false);

        ServiceUserLoginRequest request = new ServiceUserLoginRequest("colaborador@voidcube.com", "wrongPass", companyId);

        assertThatThrownBy(() -> authService.login(request, companyId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("INVALID_CREDENTIALS");
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }

    @Test
    @DisplayName("Deve alterar senha temporária com sucesso, definindo temporaryPassword como false")
    void changePassword_Success() {
        UUID userId = activeUser.getId();
        when(serviceUserRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("oldPass", "encodedHash")).thenReturn(true);
        when(passwordEncoder.matches("newPass123", "encodedHash")).thenReturn(false);
        when(passwordEncoder.encode("newPass123")).thenReturn("newEncodedHash");
        when(jwtProvider.generateAccessToken(eq(userId), eq("colaborador@voidcube.com"), eq(companyId), eq(false), eq("SERVICE_USER")))
                .thenReturn("new.refreshed.token");

        ServiceUserChangePasswordRequest request = new ServiceUserChangePasswordRequest("oldPass", "newPass123");
        ServiceUserAuthResponse response = authService.changePassword(userId, request);

        assertThat(response.accessToken()).isEqualTo("new.refreshed.token");
        assertThat(response.temporaryPassword()).isFalse();
        assertThat(activeUser.getCredentialHash()).isEqualTo("newEncodedHash");
        assertThat(activeUser.getTemporaryPassword()).isFalse();
        verify(serviceUserRepository).save(activeUser);
    }

    @Test
    @DisplayName("Deve rejeitar alteração de senha quando a nova senha é igual à atual")
    void changePassword_Failure_SamePassword() {
        UUID userId = activeUser.getId();
        when(serviceUserRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("samePass", "encodedHash")).thenReturn(true);

        ServiceUserChangePasswordRequest request = new ServiceUserChangePasswordRequest("samePass", "samePass");

        assertThatThrownBy(() -> authService.changePassword(userId, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo("SAME_PASSWORD");
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }
}