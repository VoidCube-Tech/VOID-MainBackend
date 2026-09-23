package com.voidcube.backend.core.security;

import com.voidcube.backend.core.context.CompanyContextHolder;
import com.voidcube.backend.core.context.SupportContextHolder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        CompanyContextHolder.clear();
        SupportContextHolder.clear();
    }

    @Test
    @DisplayName("Deve autenticar com token Bearer válido e preencher UserPrincipal e contexto")
    void doFilterInternal_AuthenticatesValidToken() throws ServletException, IOException {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        String token = "valid.jwt.token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        request.addHeader("X-Company-ID", companyId.toString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = Jwts.claims()
                .subject(userId.toString())
                .add("email", "usuario@empresa.com")
                .add("company_id", companyId.toString())
                .add("temporary_password", true)
                .add("user_type", "SERVICE_USER")
                .build();

        when(jwtProvider.validateToken(token)).thenReturn(true);
        when(jwtProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtProvider.getEmailFromToken(token)).thenReturn("usuario@empresa.com");
        when(jwtProvider.getClaims(token)).thenReturn(claims);

        FilterChain captureChain = (req, res) -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getPrincipal()).isInstanceOf(UserPrincipal.class);

            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            assertThat(principal.id()).isEqualTo(userId);
            assertThat(principal.email()).isEqualTo("usuario@empresa.com");
            assertThat(principal.temporaryPassword()).isTrue();
            assertThat(principal.userType()).isEqualTo("SERVICE_USER");

            assertThat(CompanyContextHolder.getCompanyId()).contains(companyId);
        };

        filter.doFilterInternal(request, response, captureChain);

        // Garante que o bloco finally limpou o ThreadLocal
        assertThat(CompanyContextHolder.getCompanyId()).isEmpty();
    }

    @Test
    @DisplayName("Deve preencher SupportContextHolder quando X-Support-Session-ID estiver presente")
    void doFilterInternal_ExtractsSupportSession() throws ServletException, IOException {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID supportSessionId = UUID.randomUUID();
        String token = "valid.jwt.token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        request.addHeader("X-Company-ID", companyId.toString());
        request.addHeader("X-Support-Session-ID", supportSessionId.toString());
        request.addHeader("X-Support-Emergency", "true");
        request.addHeader("X-Support-Reason", "Resolução de incidente crítico");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = Jwts.claims()
                .subject(userId.toString())
                .add("email", "suporte@voidcube.com")
                .build();

        when(jwtProvider.validateToken(token)).thenReturn(true);
        when(jwtProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtProvider.getEmailFromToken(token)).thenReturn("suporte@voidcube.com");
        when(jwtProvider.getClaims(token)).thenReturn(claims);

        FilterChain captureChain = (req, res) -> {
            assertThat(SupportContextHolder.isSupportModeActive()).isTrue();
            assertThat(SupportContextHolder.getSession()).isPresent();
            assertThat(SupportContextHolder.getSession().get().isEmergency()).isTrue();
            assertThat(SupportContextHolder.getSession().get().reason()).isEqualTo("Resolução de incidente crítico");
        };

        filter.doFilterInternal(request, response, captureChain);

        // Bloco finally limpa os contextos
        assertThat(SupportContextHolder.isSupportModeActive()).isFalse();
    }
}