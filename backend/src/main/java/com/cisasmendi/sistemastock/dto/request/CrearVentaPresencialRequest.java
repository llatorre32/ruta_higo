package com.cisasmendi.sistemastock.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearVentaPresencialRequest {
    
    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    private List<ItemVentaRequest> items;
    
    private String notasVenta;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemVentaRequest {
        @NotNull(message = "El ID del producto es obligatorio")
        private UUID productoId;
        
        @NotNull(message = "La cantidad es obligatoria")
        private Integer cantidad;
    }
}
