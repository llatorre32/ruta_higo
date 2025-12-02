package com.cisasmendi.sistemastock.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cisasmendi.sistemastock.model.ItemCarrito;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
    List<ItemCarrito> findByCarritoId(Long carritoId);
    Optional<ItemCarrito> findByCarritoIdAndProductoId(Long carritoId, UUID productoId);
    
    @Query("SELECT ic FROM ItemCarrito ic WHERE ic.fechaExpiracionReserva < :fecha")
    List<ItemCarrito> findItemsConReservaExpirada(LocalDateTime fecha);
    
    void deleteByCarritoId(Long carritoId);
}
