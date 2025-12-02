package com.cisasmendi.sistemastock.service;

import com.cisasmendi.sistemastock.dto.request.LoginRequest;
import com.cisasmendi.sistemastock.dto.request.RecuperarPasswordRequest;
import com.cisasmendi.sistemastock.dto.request.RegistroRequest;
import com.cisasmendi.sistemastock.dto.request.RestablecerPasswordRequest;
import com.cisasmendi.sistemastock.dto.response.JwtResponse;
import com.cisasmendi.sistemastock.dto.response.MessageResponse;
import com.cisasmendi.sistemastock.dto.response.UsuarioResponse;
import com.cisasmendi.sistemastock.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationService verificationService;

    public UsuarioResponse registrar(RegistroRequest request) {
        return usuarioService.registrarUsuario(request);
    }

    public JwtResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            Usuario usuario = (Usuario) authentication.getPrincipal();
            String jwt = jwtService.generateToken(usuario);

            return new JwtResponse(jwt, usuario.getUsername(), usuario.getEmail(), usuario.getRole().name());
            
        } catch (DisabledException e) {
            throw new RuntimeException("La cuenta no está verificada. Por favor, verifica tu email.");
        }
    }

    public MessageResponse verificarEmail(String token) {
        boolean verificado = verificationService.verificarEmail(token);
        if (verificado) {
            return new MessageResponse("Email verificado correctamente. Ya puedes iniciar sesión.");
        } else {
            throw new RuntimeException("Error verificando el email");
        }
    }

    public MessageResponse solicitarRecuperacionPassword(RecuperarPasswordRequest request) {
        verificationService.crearTokenRecuperacionPassword(request.getEmail());
        return new MessageResponse("Se ha enviado un email con las instrucciones para recuperar tu contraseña.");
    }

    public MessageResponse restablecerPassword(RestablecerPasswordRequest request) {
        boolean restablecido = verificationService.restablecerPassword(request.getToken(), request.getNuevaPassword());
        if (restablecido) {
            return new MessageResponse("Contraseña restablecida correctamente. Ya puedes iniciar sesión con tu nueva contraseña.");
        } else {
            throw new RuntimeException("Error restableciendo la contraseña");
        }
    }

    public MessageResponse reenviarVerificacion(String email) {
        verificationService.reenviarVerificacionEmail(email);
        return new MessageResponse("Se ha reenviado el email de verificación.");
    }
}