package com.cisasmendi.sistemastock.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVentasResponse {
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Double totalVentas;
    private Integer cantidadPedidos;
    private Integer cantidadPedidosPagados;
    private Integer cantidadPedidosPendientes;
    private Integer cantidadPedidosCancelados;
    private List<ProductoMasVendido> productosMasVendidos;
    private List<VentaPorDia> ventasPorDia;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoMasVendido {
        private String nombreProducto;
        private Integer cantidadVendida;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VentaPorDia {
        private String fecha;
        private Double total;
        private Integer cantidad;
    }
}
