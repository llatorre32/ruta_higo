package com.cisasmendi.sistemastock.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarProductoRequest {
    
    private String nombre;
    private String descripcion;
    
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double precio;
    
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stockActual;
    
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;
}
