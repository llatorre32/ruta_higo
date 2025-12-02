package com.cisasmendi.sistemastock.service;

import com.cisasmendi.sistemastock.dto.response.ReporteVentasResponse;
import com.cisasmendi.sistemastock.model.EstadoPedido;
import com.cisasmendi.sistemastock.model.Pedido;
import com.cisasmendi.sistemastock.repository.ItemPedidoRepository;
import com.cisasmendi.sistemastock.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    @Transactional(readOnly = true)
    public ReporteVentasResponse generarReporteVentas(LocalDateTime inicio, LocalDateTime fin) {
        List<Pedido> pedidos = pedidoRepository.findPedidosPorRangoFecha(inicio, fin);

        ReporteVentasResponse reporte = new ReporteVentasResponse();
        reporte.setFechaInicio(inicio);
        reporte.setFechaFin(fin);
        reporte.setCantidadPedidos(pedidos.size());

        // Calcular totales por estado
        long pagados = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.PAGADO || 
                           p.getEstado() == EstadoPedido.EN_DESPACHO || 
                           p.getEstado() == EstadoPedido.ENTREGADO)
                .count();
        
        long pendientes = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.PENDIENTE_PAGO)
                .count();
        
        long cancelados = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.CANCELADO)
                .count();

        reporte.setCantidadPedidosPagados((int) pagados);
        reporte.setCantidadPedidosPendientes((int) pendientes);
        reporte.setCantidadPedidosCancelados((int) cancelados);

        // Calcular total de ventas (solo pedidos pagados y completados)
        Double totalVentas = pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.PAGADO || 
                           p.getEstado() == EstadoPedido.EN_DESPACHO || 
                           p.getEstado() == EstadoPedido.ENTREGADO)
                .mapToDouble(Pedido::getTotal)
                .sum();
        
        reporte.setTotalVentas(totalVentas);

        // Productos más vendidos
        List<Object[]> productosMasVendidos = itemPedidoRepository
                .findProductosMasVendidos(inicio, fin);
        
        List<ReporteVentasResponse.ProductoMasVendido> productos = productosMasVendidos.stream()
                .limit(10)
                .map(obj -> new ReporteVentasResponse.ProductoMasVendido(
                        (String) obj[0], 
                        ((Number) obj[1]).intValue()
                ))
                .collect(Collectors.toList());
        
        reporte.setProductosMasVendidos(productos);

        // Ventas por día
        Map<String, ReporteVentasResponse.VentaPorDia> ventasPorDiaMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        pedidos.stream()
                .filter(p -> p.getEstado() == EstadoPedido.PAGADO || 
                           p.getEstado() == EstadoPedido.EN_DESPACHO || 
                           p.getEstado() == EstadoPedido.ENTREGADO)
                .forEach(pedido -> {
                    String fecha = pedido.getFechaCreacion().format(formatter);
                    ReporteVentasResponse.VentaPorDia ventaDia = ventasPorDiaMap
                            .getOrDefault(fecha, new ReporteVentasResponse.VentaPorDia(fecha, 0.0, 0));
                    
                    ventaDia.setTotal(ventaDia.getTotal() + pedido.getTotal());
                    ventaDia.setCantidad(ventaDia.getCantidad() + 1);
                    ventasPorDiaMap.put(fecha, ventaDia);
                });

        reporte.setVentasPorDia(new ArrayList<>(ventasPorDiaMap.values()));

        return reporte;
    }
}
