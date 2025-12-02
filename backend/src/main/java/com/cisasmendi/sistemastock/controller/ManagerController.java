package com.cisasmendi.sistemastock.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cisasmendi.sistemastock.dto.request.AsignarCodigoEnvioRequest;
import com.cisasmendi.sistemastock.dto.request.ConfirmarPagoRequest;
import com.cisasmendi.sistemastock.dto.request.CrearVentaPresencialRequest;
import com.cisasmendi.sistemastock.dto.response.PedidoResponse;
import com.cisasmendi.sistemastock.dto.response.UsuarioResponse;
import com.cisasmendi.sistemastock.model.EstadoPedido;
import com.cisasmendi.sistemastock.model.Role;
import com.cisasmendi.sistemastock.model.Usuario;
import com.cisasmendi.sistemastock.service.PedidoService;
import com.cisasmendi.sistemastock.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@Tag(name = "Manager", description = "Endpoints para usuarios con rol MANAGER")
@SecurityRequirement(name = "bearerAuth")
public class ManagerController {

    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;

    @GetMapping("/usuarios/compradores")
    @Operation(summary = "Obtener usuarios compradores")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> obtenerCompradores() {
        List<UsuarioResponse> compradores = usuarioService.obtenerUsuariosPorRole(Role.COMPRADOR);
        return ResponseEntity.ok(compradores);
    }

    @GetMapping("/usuarios/{id}")
    @Operation(summary = "Obtener usuario comprador por ID")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> obtenerCompradorPorId(@PathVariable Long id) {
        try {
            UsuarioResponse usuario = usuarioService.obtenerUsuarioPorId(id);
            if (usuario.getRole() != Role.COMPRADOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/usuarios/{id}/estado")
    @Operation(summary = "Cambiar estado de usuario comprador")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> cambiarEstadoComprador(
            @PathVariable Long id,
            @RequestParam boolean activo) {
        try {
            UsuarioResponse usuario = usuarioService.obtenerUsuarioPorId(id);
            if (usuario.getRole() != Role.COMPRADOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            UsuarioResponse response = usuarioService.cambiarEstadoUsuario(id, activo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Gestión de Pedidos
    
    @GetMapping("/pedidos")
    @Operation(summary = "Obtener todos los pedidos")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<PedidoResponse>> obtenerTodosPedidos() {
        List<PedidoResponse> pedidos = pedidoService.obtenerTodosPedidos();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/pedidos/estado/{estado}")
    @Operation(summary = "Obtener pedidos por estado")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<PedidoResponse>> obtenerPedidosPorEstado(@PathVariable EstadoPedido estado) {
        List<PedidoResponse> pedidos = pedidoService.obtenerPedidosPorEstado(estado);
        return ResponseEntity.ok(pedidos);
    }

    @PostMapping("/pedidos/{pedidoId}/confirmar-pago")
    @Operation(summary = "Confirmar pago de un pedido")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> confirmarPago(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long pedidoId,
            @Valid @RequestBody ConfirmarPagoRequest request) {
        PedidoResponse pedido = pedidoService.confirmarPago(pedidoId, usuario.getId(), request);
        return ResponseEntity.ok(pedido);
    }

    @PostMapping("/pedidos/{pedidoId}/codigo-envio")
    @Operation(summary = "Asignar código de envío")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> asignarCodigoEnvio(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long pedidoId,
            @Valid @RequestBody AsignarCodigoEnvioRequest request) {
        PedidoResponse pedido = pedidoService.asignarCodigoEnvio(pedidoId, usuario.getId(), request);
        return ResponseEntity.ok(pedido);
    }

    @PostMapping("/pedidos/{pedidoId}/marcar-entregado")
    @Operation(summary = "Marcar pedido como entregado")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> marcarComoEntregado(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long pedidoId) {
        PedidoResponse pedido = pedidoService.marcarComoEntregado(pedidoId, usuario.getId());
        return ResponseEntity.ok(pedido);
    }

    @PostMapping("/ventas-presenciales")
    @Operation(summary = "Registrar venta presencial")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> registrarVentaPresencial(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody CrearVentaPresencialRequest request) {
        PedidoResponse pedido = pedidoService.crearVentaPresencial(usuario.getId(), request);
        return ResponseEntity.ok(pedido);
    }
}