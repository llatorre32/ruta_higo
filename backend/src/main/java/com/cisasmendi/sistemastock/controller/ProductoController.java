package com.cisasmendi.sistemastock.controller;

import java.util.List;
import java.util.UUID;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cisasmendi.sistemastock.dto.request.ActualizarProductoRequest;
import com.cisasmendi.sistemastock.dto.request.CrearProductoRequest;
import com.cisasmendi.sistemastock.dto.response.MessageResponse;
import com.cisasmendi.sistemastock.dto.response.ProductoResponse;
import com.cisasmendi.sistemastock.model.Usuario;
import com.cisasmendi.sistemastock.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Endpoints para gestión de productos")
@SecurityRequirement(name = "bearerAuth")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    @Operation(summary = "Obtener todos los productos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductoResponse>> obtenerProductos() {
        List<ProductoResponse> productos = productoService.obtenerTodosProductos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/disponibles")
    @Operation(summary = "Obtener productos disponibles")
    public ResponseEntity<List<ProductoResponse>> obtenerProductosDisponibles() {
        List<ProductoResponse> productos = productoService.obtenerProductosDisponibles();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar productos")
    public ResponseEntity<List<ProductoResponse>> buscarProductos(@RequestParam String q) {
        List<ProductoResponse> productos = productoService.buscarProductos(q);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ProductoResponse> obtenerProducto(@PathVariable UUID id) {
        ProductoResponse producto = productoService.obtenerProducto(id);
        return ResponseEntity.ok(producto);
    }

    @PostMapping
    @Operation(summary = "Crear producto (solo MANAGER y ADMIN)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ProductoResponse> crearProducto(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody CrearProductoRequest request) {
        ProductoResponse producto = productoService.crearProducto(request, usuario.getId());
        return ResponseEntity.ok(producto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto (solo MANAGER y ADMIN)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarProductoRequest request) {
        ProductoResponse producto = productoService.actualizarProducto(id, request, usuario.getId());
        return ResponseEntity.ok(producto);
    }

    @PutMapping("/{id}/stock")
    @Operation(summary = "Actualizar stock de producto (solo MANAGER y ADMIN)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ProductoResponse> actualizarStock(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable UUID id,
            @RequestParam Integer stock) {
        ProductoResponse producto = productoService.actualizarStock(id, stock, usuario.getId());
        return ResponseEntity.ok(producto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> eliminarProducto(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable UUID id) {
        productoService.eliminarProducto(id, usuario.getId());
        return ResponseEntity.ok(new MessageResponse("Producto eliminado exitosamente"));
    }

    @GetMapping("/stock-bajo")
    @Operation(summary = "Obtener productos con stock bajo (solo MANAGER y ADMIN)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<ProductoResponse>> obtenerProductosConStockBajo() {
        List<ProductoResponse> productos = productoService.obtenerProductosConStockBajo();
        return ResponseEntity.ok(productos);
    }
    
    // Nota: Los endpoints de imágenes están en /api/productos/{productoId}/imagenes
    // Ver ImagenProductoController para gestión de imágenes
}