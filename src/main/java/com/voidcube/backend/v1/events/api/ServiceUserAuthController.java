package com.voidcube.backend.v1.events.api;

import com.voidcube.backend.core.context.CompanyContextHolder;
import com.voidcube.backend.core.exception.BusinessException;
import com.voidcube.backend.core.security.UserPrincipal;
import com.voidcube.backend.v1.events.application.dto.request.ServiceUserChangePasswordRequest;
import com.voidcube.backend.v1.events.application.dto.request.ServiceUserLoginRequest;
import com.voidcube.backend.v1.events.application.dto.response.ServiceUserAuthResponse;
import com.voidcube.backend.v1.events.application.service.ServiceUserAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/auth/service-users")
public class ServiceUserAuthController {

    private final ServiceUserAuthService authService;

    public ServiceUserAuthController(ServiceUserAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ServiceUserAuthResponse> login(
            @Valid @RequestBody ServiceUserLoginRequest request,
            @RequestHeader(value = "X-Company-ID", required = false) UUID companyIdHeader
    ) {
        UUID companyId = companyIdHeader != null ? companyIdHeader : CompanyContextHolder.getCompanyId().orElse(null);
        ServiceUserAuthResponse response = authService.login(request, companyId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ServiceUserAuthResponse> changePassword(
            @Valid @RequestBody ServiceUserChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new BusinessException(
                    "UNAUTHORIZED",
                    "Autenticação requerida para alteração de senha.",
                    HttpStatus.UNAUTHORIZED
            );
        }
        ServiceUserAuthResponse response = authService.changePassword(principal.id(), request);
        return ResponseEntity.ok(response);
    }
}