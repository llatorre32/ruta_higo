package com.cisasmendi.sistemastock.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignarCodigoEnvioRequest {
    
    @NotBlank(message = "El código de envío es obligatorio")
    private String codigoEnvio;
}
