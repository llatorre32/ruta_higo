package com.cisasmendi.sistemastock.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cisasmendi.sistemastock.service.VerificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupTask {

    private final VerificationService verificationService;

    @Scheduled(fixedRate = 3600000) // Ejecutar cada hora
    public void cleanupExpiredTokens() {
        log.info("Iniciando limpieza de tokens expirados");
        try {
            verificationService.limpiarTokensExpirados();
            log.info("Limpieza de tokens completada");
        } catch (Exception e) {
            log.error("Error durante la limpieza de tokens", e);
        }
    }
}