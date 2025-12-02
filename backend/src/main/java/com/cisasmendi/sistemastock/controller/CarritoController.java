package com.cisasmendi.sistemastock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cisasmendi.sistemastock.dto.request.ActualizarItemCarritoRequest;
import com.cisasmendi.sistemastock.dto.request.AgregarItemCarritoRequest;
import com.cisasmendi.sistemastock.dto.response.CarritoResponse;
import com.cisasmendi.sistemastock.dto.response.MessageResponse;
import com.cisasmendi.sistemastock.model.Usuario;
import com.cisasmendi.sistemastock.service.CarritoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
@Tag(name = "Carrito", description = "Gestión del carrito de compras")
@SecurityRequirement(name = "bearerAuth")
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    @PreAuthorize("hasRole('COMPRADOR')")
    @Operation(summary = "Obtener mi carrito")
    public ResponseEntity<CarritoResponse> obtenerMiCarrito(@AuthenticationPrincipal Usuario usuario) {
        CarritoResponse carrito = carritoService.obtenerCarrito(usuario.getId());
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('COMPRADOR')")
    @Operation(summary = "Agregar producto al carrito")
    public ResponseEntity<CarritoResponse> agregarItem(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody AgregarItemCarritoRequest request) {
        CarritoResponse carrito = carritoService.agregarItem(usuario.getId(), request);
        return ResponseEntity.ok(carrito);
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('COMPRADOR')")
    @Operation(summary = "Actualizar cantidad de un item")
    public ResponseEntity<CarritoResponse> actualizarItem(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long itemId,
            @Valid @RequestBody ActualizarItemCarritoRequest request) {
        CarritoResponse carrito = carritoService.actualizarItem(usuario.getId(), itemId, request);
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('COMPRADOR')")
    @Operation(summary = "Eliminar item del carrito")
    public ResponseEntity<CarritoResponse> eliminarItem(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long itemId) {
        CarritoResponse carrito = carritoService.eliminarItem(usuario.getId(), itemId);
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('COMPRADOR')")
    @Operation(summary = "Vaciar el carrito")
    public ResponseEntity<MessageResponse> vaciarCarrito(@AuthenticationPrincipal Usuario usuario) {
        carritoService.vaciarCarrito(usuario.getId());
        return ResponseEntity.ok(new MessageResponse("Carrito vaciado exitosamente"));
    }
}
