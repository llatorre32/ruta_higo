package com.cisasmendi.sistemastock.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CarritoResponse {
    private Long id;
    private Long usuarioId;
    private List<ItemCarritoResponse> items;
    private Double total;
    private LocalDateTime fechaActualizacion;

    public CarritoResponse() {}

    public CarritoResponse(Long id, Long usuarioId, List<ItemCarritoResponse> items, Double total, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.items = items;
        this.total = total;
        this.fechaActualizacion = fechaActualizacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<ItemCarritoResponse> getItems() {
        return items;
    }

    public void setItems(List<ItemCarritoResponse> items) {
        this.items = items;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public static class ItemCarritoResponse {
        private Long id;
        private UUID productoId;
        private String nombreProducto;
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal;
        private LocalDateTime fechaExpiracionReserva;

        public ItemCarritoResponse() {}

        public ItemCarritoResponse(Long id, UUID productoId, String nombreProducto, Integer cantidad, 
                                 Double precioUnitario, Double subtotal, LocalDateTime fechaExpiracionReserva) {
            this.id = id;
            this.productoId = productoId;
            this.nombreProducto = nombreProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
            this.fechaExpiracionReserva = fechaExpiracionReserva;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public UUID getProductoId() {
            return productoId;
        }

        public void setProductoId(UUID productoId) {
            this.productoId = productoId;
        }

        public String getNombreProducto() {
            return nombreProducto;
        }

        public void setNombreProducto(String nombreProducto) {
            this.nombreProducto = nombreProducto;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public Double getPrecioUnitario() {
            return precioUnitario;
        }

        public void setPrecioUnitario(Double precioUnitario) {
            this.precioUnitario = precioUnitario;
        }

        public Double getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(Double subtotal) {
            this.subtotal = subtotal;
        }

        public LocalDateTime getFechaExpiracionReserva() {
            return fechaExpiracionReserva;
        }

        public void setFechaExpiracionReserva(LocalDateTime fechaExpiracionReserva) {
            this.fechaExpiracionReserva = fechaExpiracionReserva;
        }
    }
}
