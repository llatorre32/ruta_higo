package com.cisasmendi.sistemastock.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {
    private UUID id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stockActual;
    private Integer stockMinimo;
    private Boolean disponible;
    private List<ImagenResponse> imagenes;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImagenResponse {
        private UUID id;
        private String nombreArchivo;
        private String rutaArchivo;
        private String tipoMime;
        private Long tamañoArchivo;
    }
}
