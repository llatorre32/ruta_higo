package com.cisasmendi.sistemastock.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cisasmendi.sistemastock.model.EstadoPedido;
import com.cisasmendi.sistemastock.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioId(Long usuarioId);
    List<Pedido> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findByEstadoOrderByFechaCreacionDesc(EstadoPedido estado);
    
    @Query("SELECT p FROM Pedido p WHERE p.fechaExpiracionReserva < :fecha AND p.estado = :estado")
    List<Pedido> findPedidosConReservaExpirada(LocalDateTime fecha, EstadoPedido estado);
    
    @Query("SELECT p FROM Pedido p WHERE p.fechaCreacion BETWEEN :inicio AND :fin")
    List<Pedido> findPedidosPorRangoFecha(@Param("inicio") LocalDateTime inicio, 
                                          @Param("fin") LocalDateTime fin);
    
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.estado = :estado")
    Long countByEstado(EstadoPedido estado);
    
    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.estado = :estado AND p.fechaCreacion BETWEEN :inicio AND :fin")
    Double sumTotalByEstadoAndFechaCreacionBetween(EstadoPedido estado, 
                                                    LocalDateTime inicio, 
                                                    LocalDateTime fin);
}
