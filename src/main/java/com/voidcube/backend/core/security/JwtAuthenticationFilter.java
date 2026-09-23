package com.voidcube.backend.core.security;

import com.voidcube.backend.core.context.CompanyContextHolder;
import com.voidcube.backend.core.context.CorrelationContextHolder;
import com.voidcube.backend.core.context.SupportContextHolder;
import com.voidcube.backend.core.security.support.SupportSessionContext;
import com.voidcube.backend.core.utils.UuidUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = request.getHeader("X-Correlation-ID");
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UuidUtils.generateV7().toString();
        }
        CorrelationContextHolder.setCorrelationId(correlationId);
        response.setHeader("X-Correlation-ID", correlationId);

        String token = resolveToken(request);
        UUID authenticatedUserId = null;

        UUID companyId = null;
        String companyHeader = request.getHeader("X-Company-ID");
        if (StringUtils.hasText(companyHeader)) {
            try {
                companyId = UUID.fromString(companyHeader.trim());
                CompanyContextHolder.setCompanyId(companyId);
            } catch (IllegalArgumentException ignored) {
                // Formato de UUID inválido ignorado na resolução automática
            }
        }

        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
            authenticatedUserId = jwtProvider.getUserIdFromToken(token);
            String email = jwtProvider.getEmailFromToken(token);
            Claims claims = jwtProvider.getClaims(token);

            Boolean temporaryPassword = claims.get("temporary_password", Boolean.class);
            String userType = claims.get("user_type", String.class);
            String tokenCompanyIdStr = claims.get("company_id", String.class);
            UUID tokenCompanyId = null;
            if (StringUtils.hasText(tokenCompanyIdStr)) {
                try {
                    tokenCompanyId = UUID.fromString(tokenCompanyIdStr);
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (companyId == null && tokenCompanyId != null) {
                companyId = tokenCompanyId;
                CompanyContextHolder.setCompanyId(companyId);
            }

            UserPrincipal principal = new UserPrincipal(
                    authenticatedUserId,
                    email,
                    tokenCompanyId != null ? tokenCompanyId : companyId,
                    Boolean.TRUE.equals(temporaryPassword),
                    userType != null ? userType : "PLATFORM"
            );

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    Collections.emptyList()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        String supportSessionHeader = request.getHeader("X-Support-Session-ID");
        if (StringUtils.hasText(supportSessionHeader) && companyId != null && authenticatedUserId != null) {
            try {
                UUID sessionId = UUID.fromString(supportSessionHeader.trim());
                boolean isEmergency = "true".equalsIgnoreCase(request.getHeader("X-Support-Emergency"));
                String reason = request.getHeader("X-Support-Reason");

                SupportSessionContext supportContext = new SupportSessionContext(
                        sessionId,
                        authenticatedUserId,
                        companyId,
                        isEmergency,
                        reason != null ? reason : "Operação de suporte em contexto de empresa",
                        Instant.now().plus(1, ChronoUnit.HOURS)
                );
                SupportContextHolder.setSession(supportContext);
            } catch (IllegalArgumentException ignored) {
                // Formato de UUID inválido ignorado
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            CompanyContextHolder.clear();
            SupportContextHolder.clear();
            CorrelationContextHolder.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}