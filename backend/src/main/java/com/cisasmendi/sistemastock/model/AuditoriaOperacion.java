package com.cisasmendi.sistemastock.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auditoria_operaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AuditoriaOperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private String accion;

    @Column(name = "entidad_tipo")
    private String entidadTipo;

    @Column(name = "entidad_id")
    private String entidadId;

    @Column(length = 2000)
    private String detalles;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDateTime fechaOperacion = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        fechaOperacion = LocalDateTime.now();
    }
}
