package com.cisasmendi.sistemastock.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cisasmendi.sistemastock.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    Optional<Producto> findByNombre(String nombre);
    
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Producto> buscarProductos(@Param("busqueda") String busqueda);
    
    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo")
    List<Producto> findProductosConStockBajo();
    
    @Query("SELECT p FROM Producto p WHERE p.stockActual > 0")
    List<Producto> findProductosDisponibles();
    
    boolean existsByNombre(String nombre);
}
