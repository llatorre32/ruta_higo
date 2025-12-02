package com.cisasmendi.sistemastock.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cisasmendi.sistemastock.service.CarritoService;
import com.cisasmendi.sistemastock.service.PedidoService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservaCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(ReservaCleanupTask.class);
    
    private final CarritoService carritoService;
    private final PedidoService pedidoService;

    // Ejecutar cada hora
    @Scheduled(cron = "0 0 * * * *")
    public void limpiarReservasExpiradas() {
        log.info("Iniciando limpieza de reservas expiradas");
        
        try {
            carritoService.limpiarItemsExpirados();
            log.info("Items de carrito expirados limpiados");
        } catch (Exception e) {
            log.error("Error limpiando items de carrito expirados", e);
        }

        try {
            pedidoService.limpiarPedidosExpirados();
            log.info("Pedidos expirados limpiados");
        } catch (Exception e) {
            log.error("Error limpiando pedidos expirados", e);
        }
    }
}
