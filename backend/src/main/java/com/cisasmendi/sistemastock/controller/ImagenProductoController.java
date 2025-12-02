package com.cisasmendi.sistemastock.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cisasmendi.sistemastock.dto.response.MessageResponse;
import com.cisasmendi.sistemastock.model.Usuario;
import com.cisasmendi.sistemastock.service.ImagenProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/productos/{productoId}/imagenes")
@RequiredArgsConstructor
@Tag(name = "Imágenes de Productos", description = "Gestión de imágenes de productos")
@SecurityRequirement(name = "bearerAuth")
public class ImagenProductoController {

    private final ImagenProductoService imagenProductoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir imagen de producto (solo MANAGER y ADMIN)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> subirImagen(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable UUID productoId,
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = imagenProductoService.guardarImagen(productoId, file, usuario.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir múltiples imágenes (solo MANAGER y ADMIN)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> subirMultiplesImagenes(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable UUID productoId,
            @RequestParam("files") List<MultipartFile> files) {
        
        Map<String, Object> response = imagenProductoService.guardarMultiplesImagenes(productoId, files, usuario.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{imagenId}")
    @Operation(summary = "Descargar imagen de producto")
    public ResponseEntity<Resource> descargarImagen(
            @PathVariable UUID productoId,
            @PathVariable UUID imagenId) {
        
        Resource resource = imagenProductoService.cargarImagen(imagenId);
        String contentType = imagenProductoService.obtenerTipoMime(imagenId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{imagenId}")
    @Operation(summary = "Eliminar imagen de producto (solo MANAGER y ADMIN)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MessageResponse> eliminarImagen(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable UUID productoId,
            @PathVariable UUID imagenId) {
        
        imagenProductoService.eliminarImagen(imagenId, usuario.getId());
        return ResponseEntity.ok(new MessageResponse("Imagen eliminada exitosamente"));
    }

    @DeleteMapping
    @Operation(summary = "Eliminar todas las imágenes de un producto (solo MANAGER y ADMIN)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MessageResponse> eliminarTodasLasImagenes(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable UUID productoId) {
        
        imagenProductoService.eliminarTodasLasImagenes(productoId, usuario.getId());
        return ResponseEntity.ok(new MessageResponse("Todas las imágenes eliminadas exitosamente"));
    }
}
