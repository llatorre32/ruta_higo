package com.cisasmendi.sistemastock.service;

import com.cisasmendi.sistemastock.dto.response.AuditoriaResponse;
import com.cisasmendi.sistemastock.model.AuditoriaOperacion;
import com.cisasmendi.sistemastock.model.Usuario;
import com.cisasmendi.sistemastock.repository.AuditoriaOperacionRepository;
import com.cisasmendi.sistemastock.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaOperacionRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void registrar(Long usuarioId, String accion, String entidadTipo, 
                         String entidadId, String detalles, String ipAddress) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

        AuditoriaOperacion auditoria = new AuditoriaOperacion();
        auditoria.setUsuario(usuario);
        auditoria.setAccion(accion);
        auditoria.setEntidadTipo(entidadTipo);
        auditoria.setEntidadId(entidadId);
        auditoria.setDetalles(detalles);
        auditoria.setIpAddress(ipAddress);

        auditoriaRepository.save(auditoria);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> obtenerPorUsuario(Long usuarioId) {
        List<AuditoriaOperacion> auditorias = auditoriaRepository
                .findByUsuarioIdOrderByFechaOperacionDesc(usuarioId);
        return convertirAResponse(auditorias);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> obtenerPorRangoFecha(LocalDateTime inicio, LocalDateTime fin) {
        List<AuditoriaOperacion> auditorias = auditoriaRepository
                .findByFechaOperacionBetween(inicio, fin);
        return convertirAResponse(auditorias);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> obtenerPorAccion(String accion) {
        List<AuditoriaOperacion> auditorias = auditoriaRepository
                .findByAccionOrderByFechaOperacionDesc(accion);
        return convertirAResponse(auditorias);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> obtenerPorEntidad(String tipo, String id) {
        List<AuditoriaOperacion> auditorias = auditoriaRepository
                .findByEntidad(tipo, id);
        return convertirAResponse(auditorias);
    }

    private List<AuditoriaResponse> convertirAResponse(List<AuditoriaOperacion> auditorias) {
        return auditorias.stream()
                .map(auditoria -> {
                    AuditoriaResponse response = new AuditoriaResponse();
                    response.setId(auditoria.getId());
                    response.setUsuario(auditoria.getUsuario() != null ? 
                            auditoria.getUsuario().getUsername() : "Sistema");
                    response.setAccion(auditoria.getAccion());
                    response.setEntidadTipo(auditoria.getEntidadTipo());
                    response.setEntidadId(auditoria.getEntidadId());
                    response.setDetalles(auditoria.getDetalles());
                    response.setIpAddress(auditoria.getIpAddress());
                    response.setFechaOperacion(auditoria.getFechaOperacion());
                    return response;
                })
                .collect(Collectors.toList());
    }
}
