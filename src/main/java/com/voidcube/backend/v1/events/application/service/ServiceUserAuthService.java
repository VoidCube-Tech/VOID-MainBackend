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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ServiceUserAuthService {

    private final ServiceUserRepository serviceUserRepository;
    private final LegacyUserReferenceRepository legacyUserReferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public ServiceUserAuthService(
            ServiceUserRepository serviceUserRepository,
            LegacyUserReferenceRepository legacyUserReferenceRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider
    ) {
        this.serviceUserRepository = serviceUserRepository;
        this.legacyUserReferenceRepository = legacyUserReferenceRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Transactional(readOnly = true)
    public ServiceUserAuthResponse login(ServiceUserLoginRequest request, UUID resolvedCompanyId) {
        UUID companyId = resolvedCompanyId != null ? resolvedCompanyId : request.companyId();
        if (companyId == null) {
            throw new BusinessException(
                    "COMPANY_CONTEXT_REQUIRED",
                    "O cabeçalho X-Company-ID ou companyId na requisição é obrigatório.",
                    HttpStatus.BAD_REQUEST
            );
        }

        ServiceUser serviceUser = findServiceUserByIdentifier(companyId, request.identifier())
                .orElseThrow(() -> new BusinessException(
                        "INVALID_CREDENTIALS",
                        "Identificador ou senha inválidos para a empresa informada.",
                        HttpStatus.UNAUTHORIZED
                ));

        if (serviceUser.getDeletedAt() != null || serviceUser.getStatus() != ServiceUserStatus.ACTIVE) {
            throw new BusinessException(
                    "USER_INACTIVE",
                    "O usuário está inativo ou desativado na plataforma.",
                    HttpStatus.FORBIDDEN
            );
        }

        if (serviceUser.getCredentialHash() == null || !passwordEncoder.matches(request.password(), serviceUser.getCredentialHash())) {
            throw new BusinessException(
                    "INVALID_CREDENTIALS",
                    "Identificador ou senha inválidos para a empresa informada.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        boolean isTemporary = Boolean.TRUE.equals(serviceUser.getTemporaryPassword());
        String token = jwtProvider.generateAccessToken(
                serviceUser.getId(),
                serviceUser.getEmail() != null ? serviceUser.getEmail() : request.identifier(),
                companyId,
                isTemporary,
                "SERVICE_USER"
        );

        return ServiceUserAuthResponse.of(
                token,
                serviceUser.getId(),
                serviceUser.getName(),
                serviceUser.getEmail(),
                companyId,
                isTemporary
        );
    }

    @Transactional
    public ServiceUserAuthResponse changePassword(UUID serviceUserId, ServiceUserChangePasswordRequest request) {
        ServiceUser serviceUser = serviceUserRepository.findById(serviceUserId)
                .orElseThrow(() -> new BusinessException(
                        "USER_NOT_FOUND",
                        "Usuário de serviço não encontrado.",
                        HttpStatus.NOT_FOUND
                ));

        if (serviceUser.getDeletedAt() != null || serviceUser.getStatus() != ServiceUserStatus.ACTIVE) {
            throw new BusinessException(
                    "USER_INACTIVE",
                    "O usuário está inativo ou desativado na plataforma.",
                    HttpStatus.FORBIDDEN
            );
        }

        if (serviceUser.getCredentialHash() == null || !passwordEncoder.matches(request.currentPassword(), serviceUser.getCredentialHash())) {
            throw new BusinessException(
                    "INVALID_CREDENTIALS",
                    "A senha atual informada está incorreta.",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (passwordEncoder.matches(request.newPassword(), serviceUser.getCredentialHash())) {
            throw new BusinessException(
                    "SAME_PASSWORD",
                    "A nova senha não pode ser igual à senha anterior.",
                    HttpStatus.BAD_REQUEST
            );
        }

        serviceUser.setCredentialHash(passwordEncoder.encode(request.newPassword()));
        serviceUser.setTemporaryPassword(false);
        serviceUserRepository.save(serviceUser);

        String newToken = jwtProvider.generateAccessToken(
                serviceUser.getId(),
                serviceUser.getEmail() != null ? serviceUser.getEmail() : "",
                serviceUser.getCompanyId(),
                false,
                "SERVICE_USER"
        );

        return ServiceUserAuthResponse.of(
                newToken,
                serviceUser.getId(),
                serviceUser.getName(),
                serviceUser.getEmail(),
                serviceUser.getCompanyId(),
                false
        );
    }

    private Optional<ServiceUser> findServiceUserByIdentifier(UUID companyId, String identifier) {
        // 1. Tenta resolver por matrícula (LegacyUserReference) no escopo da empresa
        Optional<LegacyUserReference> legacyRef = legacyUserReferenceRepository.findByCompanyIdAndRegistrationCode(companyId, identifier);
        if (legacyRef.isPresent()) {
            return Optional.of(legacyRef.get().getServiceUser());
        }

        // 2. Se não encontrar por matrícula, busca por e-mail no escopo da empresa
        return serviceUserRepository.findActiveByCompanyIdAndEmail(companyId, identifier);
    }
}