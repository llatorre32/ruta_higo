package com.cisasmendi.sistemastock.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cisasmendi.sistemastock.model.ItemPedido;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
    List<ItemPedido> findByPedidoId(Long pedidoId);
    
    @Query("SELECT ip FROM ItemPedido ip WHERE ip.producto.id = :productoId")
    List<ItemPedido> findByProductoId(@Param("productoId") UUID productoId);
    
    @Query("SELECT ip.nombreProducto, SUM(ip.cantidad) as total FROM ItemPedido ip " +
           "JOIN ip.pedido p WHERE p.fechaCreacion BETWEEN :inicio AND :fin " +
           "GROUP BY ip.nombreProducto ORDER BY total DESC")
    List<Object[]> findProductosMasVendidos(@Param("inicio") LocalDateTime inicio, 
                                             @Param("fin") LocalDateTime fin);
}
