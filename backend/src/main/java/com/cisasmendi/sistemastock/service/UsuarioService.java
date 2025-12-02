package com.cisasmendi.sistemastock.service;

import com.cisasmendi.sistemastock.dto.request.RegistroRequest;
import com.cisasmendi.sistemastock.dto.request.UsuarioUpdateRequest;
import com.cisasmendi.sistemastock.dto.response.UsuarioResponse;
import com.cisasmendi.sistemastock.model.Role;
import com.cisasmendi.sistemastock.model.Usuario;
import com.cisasmendi.sistemastock.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    @PostConstruct
    public void initializeAdminUser() {
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@sistema.com");
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
            admin.setRole(Role.ADMIN);
            admin.setActivo(true);
            admin.setEmailVerificado(true); // Admin no necesita verificar email
            usuarioRepository.save(admin);
        }
    }

    @Transactional
    public UsuarioResponse registrarUsuario(RegistroRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }
        
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        BeanUtils.copyProperties(request, usuario);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRole(Role.COMPRADOR);
        usuario.setActivo(true);

        Usuario savedUsuario = usuarioRepository.save(usuario);
        
        // Enviar email de verificación
        verificationService.crearTokenVerificacionEmail(savedUsuario);
        
        return convertToResponse(savedUsuario);
    }

    @Transactional
    public UsuarioResponse crearUsuario(RegistroRequest request, Role role) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }
        
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        BeanUtils.copyProperties(request, usuario);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRole(role);
        usuario.setActivo(true);

        Usuario savedUsuario = usuarioRepository.save(usuario);
        return convertToResponse(savedUsuario);
    }

    public List<UsuarioResponse> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public UsuarioResponse obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return convertToResponse(usuario);
    }

    @Transactional
    public UsuarioResponse actualizarUsuario(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getEmail().equals(request.getEmail()) && 
            usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado por otro usuario");
        }

        usuario.setEmail(request.getEmail());
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setTelefono(request.getTelefono());
        usuario.setDireccion(request.getDireccion());
        
        if (request.getRole() != null) {
            usuario.setRole(request.getRole());
        }
        
        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }

        Usuario savedUsuario = usuarioRepository.save(usuario);
        return convertToResponse(savedUsuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (usuario.getRole() == Role.ADMIN) {
            throw new RuntimeException("No se puede eliminar un usuario administrador");
        }
        
        usuarioRepository.delete(usuario);
    }

    @Transactional
    public UsuarioResponse cambiarEstadoUsuario(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (usuario.getRole() == Role.ADMIN) {
            throw new RuntimeException("No se puede desactivar un usuario administrador");
        }
        
        usuario.setActivo(activo);
        Usuario savedUsuario = usuarioRepository.save(usuario);
        return convertToResponse(savedUsuario);
    }

    public List<UsuarioResponse> obtenerUsuariosPorRole(Role role) {
        return usuarioRepository.findByRole(role).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private UsuarioResponse convertToResponse(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();
        BeanUtils.copyProperties(usuario, response);
        return response;
    }
}