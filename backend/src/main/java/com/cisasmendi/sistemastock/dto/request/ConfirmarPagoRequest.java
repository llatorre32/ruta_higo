package com.cisasmendi.sistemastock.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmarPagoRequest {
    
    @NotBlank(message = "La ruta del comprobante es obligatoria")
    private String comprobantePago;
}
