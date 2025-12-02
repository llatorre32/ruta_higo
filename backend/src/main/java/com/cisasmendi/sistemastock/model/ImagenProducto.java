package com.cisasmendi.sistemastock.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "imagenes_producto")
public class ImagenProducto {
    
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;
    
    @NotBlank(message = "El nombre del archivo es obligatorio")
    @Size(max = 255, message = "El nombre del archivo no puede exceder los 255 caracteres")
    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;
    
    @NotBlank(message = "La ruta del archivo es obligatoria")
    @Size(max = 500, message = "La ruta del archivo no puede exceder los 500 caracteres")
    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;
    
    @Size(max = 100, message = "El tipo MIME no puede exceder los 100 caracteres")
    @Column(name = "tipo_mime", length = 100)
    private String tipoMime;
    
    @Column(name = "tamaño_archivo")
    private Long tamañoArchivo;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}