package com.cisasmendi.sistemastock.controller;

import com.cisasmendi.sistemastock.dto.request.LoginRequest;
import com.cisasmendi.sistemastock.dto.request.RecuperarPasswordRequest;
import com.cisasmendi.sistemastock.dto.request.RegistroRequest;
import com.cisasmendi.sistemastock.dto.request.RestablecerPasswordRequest;
import com.cisasmendi.sistemastock.dto.response.JwtResponse;
import com.cisasmendi.sistemastock.dto.response.MessageResponse;
import com.cisasmendi.sistemastock.dto.response.UsuarioResponse;
import com.cisasmendi.sistemastock.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación y registro de usuarios")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario", description = "Permite a un usuario registrarse como COMPRADOR. Se enviará un email de verificación.")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        try {
            UsuarioResponse response = authService.registrar(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT. Requiere email verificado.")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            JwtResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/verificar-email")
    @Operation(summary = "Verificar email", description = "Verifica el email del usuario usando el token recibido por email")
    public ResponseEntity<MessageResponse> verificarEmail(@RequestParam String token) {
        try {
            MessageResponse response = authService.verificarEmail(token);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/recuperar-password")
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Envía un email con token para restablecer la contraseña")
    public ResponseEntity<MessageResponse> solicitarRecuperacionPassword(@Valid @RequestBody RecuperarPasswordRequest request) {
        try {
            MessageResponse response = authService.solicitarRecuperacionPassword(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/restablecer-password")
    @Operation(summary = "Restablecer contraseña", description = "Restablece la contraseña usando el token recibido por email")
    public ResponseEntity<MessageResponse> restablecerPassword(@Valid @RequestBody RestablecerPasswordRequest request) {
        try {
            MessageResponse response = authService.restablecerPassword(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/reenviar-verificacion")
    @Operation(summary = "Reenviar email de verificación", description = "Reenvía el email de verificación si no se ha recibido")
    public ResponseEntity<MessageResponse> reenviarVerificacion(@RequestParam String email) {
        try {
            MessageResponse response = authService.reenviarVerificacion(email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}