package com.cisasmendi.sistemastock.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cisasmendi.sistemastock.dto.request.RegistroRequest;
import com.cisasmendi.sistemastock.dto.request.UsuarioUpdateRequest;
import com.cisasmendi.sistemastock.dto.response.AuditoriaResponse;
import com.cisasmendi.sistemastock.dto.response.ProductoResponse;
import com.cisasmendi.sistemastock.dto.response.ReporteVentasResponse;
import com.cisasmendi.sistemastock.dto.response.UsuarioResponse;
import com.cisasmendi.sistemastock.model.Role;
import com.cisasmendi.sistemastock.service.AuditoriaService;
import com.cisasmendi.sistemastock.service.ProductoService;
import com.cisasmendi.sistemastock.service.ReporteService;
import com.cisasmendi.sistemastock.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administración", description = "Endpoints para administración de usuarios (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UsuarioService usuarioService;
    private final ReporteService reporteService;
    private final AuditoriaService auditoriaService;
    private final ProductoService productoService;

    @GetMapping("/usuarios")
    @Operation(summary = "Obtener todos los usuarios", description = "Lista todos los usuarios del sistema")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> obtenerTodosLosUsuarios() {
        List<UsuarioResponse> usuarios = usuarioService.obtenerTodosLosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/usuarios/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene los detalles de un usuario específico")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(@PathVariable Long id) {
        try {
            UsuarioResponse usuario = usuarioService.obtenerUsuarioPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/usuarios")
    @Operation(summary = "Crear nuevo usuario", description = "Crea un nuevo usuario con rol específico")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> crearUsuario(
            @Valid @RequestBody RegistroRequest request,
            @RequestParam(defaultValue = "COMPRADOR") Role role) {
        try {
            UsuarioResponse response = usuarioService.crearUsuario(request, role);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/usuarios/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request) {
        try {
            UsuarioResponse response = usuarioService.actualizarUsuario(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/usuarios/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/usuarios/{id}/estado")
    @Operation(summary = "Cambiar estado de usuario", description = "Activa o desactiva un usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> cambiarEstadoUsuario(
            @PathVariable Long id,
            @RequestParam boolean activo) {
        try {
            UsuarioResponse response = usuarioService.cambiarEstadoUsuario(id, activo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/usuarios/role/{role}")
    @Operation(summary = "Obtener usuarios por rol", description = "Lista usuarios filtrados por rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> obtenerUsuariosPorRole(@PathVariable Role role) {
        List<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosPorRole(role);
        return ResponseEntity.ok(usuarios);
    }

    // Reportes y auditoría

    @GetMapping("/reportes/ventas")
    @Operation(summary = "Generar reporte de ventas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReporteVentasResponse> generarReporteVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        ReporteVentasResponse reporte = reporteService.generarReporteVentas(inicio, fin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/auditoria")
    @Operation(summary = "Obtener registros de auditoría por rango de fechas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditoriaResponse>> obtenerAuditoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<AuditoriaResponse> auditoria = auditoriaService.obtenerPorRangoFecha(inicio, fin);
        return ResponseEntity.ok(auditoria);
    }

    @GetMapping("/auditoria/usuario/{usuarioId}")
    @Operation(summary = "Obtener auditoría por usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditoriaResponse>> obtenerAuditoriaPorUsuario(@PathVariable Long usuarioId) {
        List<AuditoriaResponse> auditoria = auditoriaService.obtenerPorUsuario(usuarioId);
        return ResponseEntity.ok(auditoria);
    }

    @GetMapping("/productos/stock-bajo")
    @Operation(summary = "Obtener productos con stock bajo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductoResponse>> obtenerProductosStockBajo() {
        List<ProductoResponse> productos = productoService.obtenerProductosConStockBajo();
        return ResponseEntity.ok(productos);
    }
}