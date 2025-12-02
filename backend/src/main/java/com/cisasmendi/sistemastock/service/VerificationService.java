package com.cisasmendi.sistemastock.service;

import com.cisasmendi.sistemastock.model.Usuario;
import com.cisasmendi.sistemastock.model.VerificationToken;
import com.cisasmendi.sistemastock.repository.UsuarioRepository;
import com.cisasmendi.sistemastock.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void crearTokenVerificacionEmail(Usuario usuario) {
        // Eliminar tokens anteriores del mismo tipo
        tokenRepository.deleteByUsuario(usuario);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUsuario(usuario);
        verificationToken.setTipo(VerificationToken.TokenType.EMAIL_VERIFICATION);
        verificationToken.setFechaExpiracion(LocalDateTime.now().plusHours(24));

        tokenRepository.save(verificationToken);

        // Enviar email
        emailService.enviarEmailVerificacion(
                usuario.getEmail(),
                usuario.getNombre(),
                token
        );
    }

    @Transactional
    public void crearTokenRecuperacionPassword(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con ese email"));

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUsuario(usuario);
        verificationToken.setTipo(VerificationToken.TokenType.PASSWORD_RESET);
        verificationToken.setFechaExpiracion(LocalDateTime.now().plusHours(1));

        tokenRepository.save(verificationToken);

        // Enviar email
        emailService.enviarEmailRecuperacion(
                usuario.getEmail(),
                usuario.getNombre(),
                token
        );
    }

    @Transactional
    public boolean verificarEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndUsadoFalse(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o ya utilizado"));

        if (verificationToken.isExpired()) {
            throw new RuntimeException("El token ha expirado");
        }

        if (verificationToken.getTipo() != VerificationToken.TokenType.EMAIL_VERIFICATION) {
            throw new RuntimeException("Token no válido para verificación de email");
        }

        Usuario usuario = verificationToken.getUsuario();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        verificationToken.setUsado(true);
        tokenRepository.save(verificationToken);

        return true;
    }

    @Transactional
    public boolean restablecerPassword(String token, String nuevaPassword) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndUsadoFalse(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o ya utilizado"));

        if (verificationToken.isExpired()) {
            throw new RuntimeException("El token ha expirado");
        }

        if (verificationToken.getTipo() != VerificationToken.TokenType.PASSWORD_RESET) {
            throw new RuntimeException("Token no válido para restablecimiento de contraseña");
        }

        Usuario usuario = verificationToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        verificationToken.setUsado(true);
        tokenRepository.save(verificationToken);

        return true;
    }

    @Transactional
    public void reenviarVerificacionEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con ese email"));

        if (usuario.getEmailVerificado()) {
            throw new RuntimeException("El email ya está verificado");
        }

        crearTokenVerificacionEmail(usuario);
    }

    // Método para limpiar tokens expirados (se puede ejecutar con un scheduler)
    @Transactional
    public void limpiarTokensExpirados() {
        tokenRepository.deleteByFechaExpiracionBefore(LocalDateTime.now());
    }
}