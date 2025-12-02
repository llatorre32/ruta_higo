package com.cisasmendi.sistemastock.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // Puede ser null para ventas presenciales

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> items = new ArrayList<>();

    @NotNull(message = "El total es obligatorio")
    @Column(nullable = false)
    private Double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado = EstadoPedido.PENDIENTE_PAGO;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "fecha_despacho")
    private LocalDateTime fechaDespacho;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "fecha_expiracion_reserva")
    private LocalDateTime fechaExpiracionReserva;

    @Column(name = "codigo_envio")
    private String codigoEnvio;

    @Column(name = "comprobante_pago")
    private String comprobantePago; // Path al archivo del comprobante

    @Column(name = "direccion_envio", length = 500)
    private String direccionEnvio;

    @Column(name = "telefono_contacto")
    private String telefonoContacto;

    @Column(name = "notas", length = 1000)
    private String notas;

    @Column(name = "es_venta_presencial")
    private Boolean esVentaPresencial = false;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (!esVentaPresencial) {
            // Reserva por 3 días para pedidos online
            fechaExpiracionReserva = LocalDateTime.now().plusDays(3);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // Método auxiliar para agregar items
    public void agregarItem(ItemPedido item) {
        items.add(item);
        item.setPedido(this);
    }

    // Método para verificar si la reserva está expirada
    public boolean isReservaExpirada() {
        return fechaExpiracionReserva != null && 
               LocalDateTime.now().isAfter(fechaExpiracionReserva) &&
               estado == EstadoPedido.PENDIENTE_PAGO;
    }
}
