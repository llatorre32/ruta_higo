package com.cisasmendi.sistemastock.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cisasmendi.sistemastock.model.ImagenProducto;

@Repository
public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, UUID> {
    List<ImagenProducto> findByProductoId(UUID productoId);
    void deleteByProductoId(UUID productoId);
}
