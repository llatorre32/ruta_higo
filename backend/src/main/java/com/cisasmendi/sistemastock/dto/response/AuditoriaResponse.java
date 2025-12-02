package com.cisasmendi.sistemastock.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaResponse {
    private Long id;
    private String usuario;
    private String accion;
    private String entidadTipo;
    private String entidadId;
    private String detalles;
    private String ipAddress;
    private LocalDateTime fechaOperacion;
}
