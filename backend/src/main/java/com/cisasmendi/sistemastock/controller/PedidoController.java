package com.cisasmendi.sistemastock.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cisasmendi.sistemastock.dto.request.CrearPedidoRequest;
import com.cisasmendi.sistemastock.dto.response.MessageResponse;
import com.cisasmendi.sistemastock.dto.response.PedidoResponse;
import com.cisasmendi.sistemastock.model.Usuario;
import com.cisasmendi.sistemastock.service.PedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gestión de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @PreAuthorize("hasRole('COMPRADOR')")
    @Operation(summary = "Crear pedido desde el carrito")
    public ResponseEntity<PedidoResponse> crearPedido(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody CrearPedidoRequest request) {
        PedidoResponse pedido = pedidoService.crearPedidoDesdeCarrito(usuario.getId(), request);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping
    @PreAuthorize("hasRole('COMPRADOR')")
    @Operation(summary = "Obtener mis pedidos")
    public ResponseEntity<List<PedidoResponse>> obtenerMisPedidos(@AuthenticationPrincipal Usuario usuario) {
        List<PedidoResponse> pedidos = pedidoService.obtenerMisPedidos(usuario.getId());
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{pedidoId}")
    @PreAuthorize("hasAnyRole('COMPRADOR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Obtener pedido por ID")
    public ResponseEntity<PedidoResponse> obtenerPedido(@PathVariable Long pedidoId) {
        PedidoResponse pedido = pedidoService.obtenerPedidoPorId(pedidoId);
        return ResponseEntity.ok(pedido);
    }

    @DeleteMapping("/{pedidoId}")
    @PreAuthorize("hasRole('COMPRADOR')")
    @Operation(summary = "Cancelar pedido")
    public ResponseEntity<MessageResponse> cancelarPedido(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long pedidoId) {
        pedidoService.cancelarPedido(pedidoId, usuario.getId());
        return ResponseEntity.ok(new MessageResponse("Pedido cancelado exitosamente"));
    }
}
