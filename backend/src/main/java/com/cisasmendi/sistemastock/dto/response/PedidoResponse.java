package com.cisasmendi.sistemastock.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponse {
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private List<ItemPedidoResponse> items;
    private Double total;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaDespacho;
    private LocalDateTime fechaEntrega;
    private LocalDateTime fechaExpiracionReserva;
    private String codigoEnvio;
    private String comprobantePago;
    private String direccionEnvio;
    private String telefonoContacto;
    private String notas;
    private Boolean esVentaPresencial;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemPedidoResponse {
        private Long id;
        private UUID productoId;
        private String nombreProducto;
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal;
    }
}
