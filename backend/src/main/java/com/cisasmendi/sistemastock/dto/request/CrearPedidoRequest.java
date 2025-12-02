package com.cisasmendi.sistemastock.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPedidoRequest {
    
    @NotBlank(message = "La dirección de envío es obligatoria")
    private String direccionEnvio;
    
    @NotBlank(message = "El teléfono de contacto es obligatorio")
    private String telefonoContacto;
    
    private String notas;
}
