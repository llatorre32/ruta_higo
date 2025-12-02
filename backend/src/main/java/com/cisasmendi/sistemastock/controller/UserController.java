package com.cisasmendi.sistemastock.controller;

import com.cisasmendi.sistemastock.dto.request.UsuarioUpdateRequest;
import com.cisasmendi.sistemastock.dto.response.UsuarioResponse;
import com.cisasmendi.sistemastock.model.Usuario;
import com.cisasmendi.sistemastock.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "Usuario", description = "Endpoints para usuarios autenticados")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UsuarioService usuarioService;

    @GetMapping("/perfil")
    @Operation(summary = "Obtener perfil del usuario", description = "Obtiene el perfil del usuario autenticado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioResponse> obtenerPerfil(Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            UsuarioResponse response = usuarioService.obtenerUsuarioPorId(usuario.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/perfil")
    @Operation(summary = "Actualizar perfil del usuario", description = "Permite al usuario actualizar su propio perfil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(
            @Valid @RequestBody UsuarioUpdateRequest request,
            Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            // No permitir cambio de rol desde este endpoint
            request.setRole(null);
            request.setActivo(null);
            
            UsuarioResponse response = usuarioService.actualizarUsuario(usuario.getId(), request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}