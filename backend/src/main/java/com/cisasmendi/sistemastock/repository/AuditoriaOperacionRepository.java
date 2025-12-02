package com.cisasmendi.sistemastock.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cisasmendi.sistemastock.model.AuditoriaOperacion;

@Repository
public interface AuditoriaOperacionRepository extends JpaRepository<AuditoriaOperacion, Long> {
    List<AuditoriaOperacion> findByUsuarioIdOrderByFechaOperacionDesc(Long usuarioId);
    
    @Query("SELECT a FROM AuditoriaOperacion a WHERE a.fechaOperacion BETWEEN :inicio AND :fin ORDER BY a.fechaOperacion DESC")
    List<AuditoriaOperacion> findByFechaOperacionBetween(@Param("inicio") LocalDateTime inicio, 
                                                          @Param("fin") LocalDateTime fin);
    
    List<AuditoriaOperacion> findByAccionOrderByFechaOperacionDesc(String accion);
    
    @Query("SELECT a FROM AuditoriaOperacion a WHERE a.entidadTipo = :tipo AND a.entidadId = :id ORDER BY a.fechaOperacion DESC")
    List<AuditoriaOperacion> findByEntidad(@Param("tipo") String tipo, @Param("id") String id);
}
