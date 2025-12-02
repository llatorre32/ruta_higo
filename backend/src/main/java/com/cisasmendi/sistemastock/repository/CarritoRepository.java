package com.cisasmendi.sistemastock.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cisasmendi.sistemastock.model.Carrito;
import com.cisasmendi.sistemastock.model.Usuario;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Optional<Carrito> findByUsuario(Usuario usuario);
    Optional<Carrito> findByUsuarioId(Long usuarioId);
    boolean existsByUsuario(Usuario usuario);
}
