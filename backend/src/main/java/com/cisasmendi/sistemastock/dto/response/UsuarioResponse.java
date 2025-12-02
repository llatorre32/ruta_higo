package com.cisasmendi.sistemastock.dto.response;

import com.cisasmendi.sistemastock.model.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsuarioResponse {
    private Long id;
    private String username;
    private String email;
    private String nombre;
    private String apellido;
    private String telefono;
    private String direccion;
    private Role role;
    private Boolean activo;
    private Boolean emailVerificado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}