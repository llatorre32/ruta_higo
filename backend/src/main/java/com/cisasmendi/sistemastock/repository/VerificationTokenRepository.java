package com.cisasmendi.sistemastock.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cisasmendi.sistemastock.model.Usuario;
import com.cisasmendi.sistemastock.model.VerificationToken;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    
    Optional<VerificationToken> findByToken(String token);
    
    List<VerificationToken> findByUsuario(Usuario usuario);
    
    List<VerificationToken> findByUsuarioAndTipo(Usuario usuario, VerificationToken.TokenType tipo);
    
    void deleteByUsuario(Usuario usuario);
    
    void deleteByFechaExpiracionBefore(LocalDateTime fecha);
    
    Optional<VerificationToken> findByTokenAndUsadoFalse(String token);
}