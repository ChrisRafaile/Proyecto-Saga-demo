package com.sagafalabella.inventario.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sagafalabella.inventario.model.Usuario;
import com.sagafalabella.inventario.repository.UsuarioRepository;

/**
 * Servicio para gestión de usuarios y autenticación
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Service
@Transactional
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Implementación de UserDetailsService para Spring Security
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        
        // Actualizar fecha de último acceso
        usuario.setFechaUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);
        
        return usuario;
    }

    /**
     * Registrar nuevo empleado (solo para administradores)
     */
    public Usuario registrarEmpleado(Usuario usuario) {
        // Validar que el usuario no existe
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }
        
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        // Configurar como usuario interno activo
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    /**
     * Registrar cliente con usuario (para registro público)
     */
    public Usuario registrarCliente(Usuario usuario) {
        // Validar que el usuario no existe
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }
        
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Configurar como cliente
        usuario.setRol(Usuario.RolUsuario.CLIENTE);
        usuario.setTipoUsuario(Usuario.TipoUsuario.EXTERNO);
        
        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        // Configurar como activo
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    /**
     * Buscar usuario por ID
     */
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Buscar usuario por username
     */
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsernameAndActivoTrue(username);
    }

    /**
     * Buscar usuario por email
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmailAndActivoTrue(email);
    }

    /**
     * Listar todos los usuarios activos
     */
    public List<Usuario> listarUsuariosActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    /**
     * Listar empleados (usuarios internos)
     */
    public List<Usuario> listarEmpleados() {
        return usuarioRepository.findByTipoUsuarioAndActivoTrue(Usuario.TipoUsuario.INTERNO);
    }

    /**
     * Listar clientes (usuarios externos)
     */
    public List<Usuario> listarClientes() {
        return usuarioRepository.findByTipoUsuarioAndActivoTrue(Usuario.TipoUsuario.EXTERNO);
    }

    /**
     * Activar/Desactivar usuario
     */
    public Usuario cambiarEstadoUsuario(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }

    /**
     * Verificar si un username está disponible
     */
    public boolean isUsernameDisponible(String username) {
        return !usuarioRepository.existsByUsername(username);
    }

    /**
     * Verificar si un email está disponible
     */
    public boolean isEmailDisponible(String email) {
        return !usuarioRepository.existsByEmail(email);
    }

    /**
     * Cambiar contraseña de usuario
     */
    public void cambiarContrasena(Long usuarioId, String nuevaContrasena) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setPassword(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);
    }

    /**
     * Buscar usuarios por rol
     */
    public List<Usuario> buscarPorRol(Usuario.RolUsuario rol) {
        return usuarioRepository.findByRolAndActivoTrue(rol);
    }

    /**
     * Contar usuarios por tipo
     */
    public long contarUsuariosPorTipo(Usuario.TipoUsuario tipo) {
        return usuarioRepository.countByTipoUsuarioAndActivoTrue(tipo);
    }    /**
     * Contar total de usuarios activos
     */
    public long contarUsuariosActivos() {
        return usuarioRepository.countByActivoTrue();
    }
    
    // ==================== MÉTODOS CRUD PARA ADMINISTRACIÓN ====================
    
    /**
     * Obtener todos los usuarios del sistema
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }
    
    /**
     * Guardar o actualizar usuario (para administración)
     */
    public Usuario guardarUsuario(Usuario usuario) {
        // Si es un nuevo usuario, encriptar la contraseña
        if (usuario.getId() == null && usuario.getPassword() != null) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        
        // Establecer valores por defecto
        if (usuario.getFechaCreacion() == null) {
            usuario.setFechaCreacion(LocalDateTime.now());
        }
        
        return usuarioRepository.save(usuario);
    }
    
    /**
     * Eliminar usuario (soft delete)
     */
    public void eliminarUsuario(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            u.setActivo(false);
            usuarioRepository.save(u);
        }
    }

    /**
     * Activar usuario
     */
    public void activarUsuario(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            u.setActivo(true);
            usuarioRepository.save(u);
        }
    }
    
    /**
     * Cambiar contraseña de usuario
     */
    public void cambiarPassword(Usuario usuario, String nuevaPassword) {
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }
    
    /**
     * Verificar contraseña actual
     */
    public boolean verificarPasswordActual(Usuario usuario, String passwordActual) {
        return passwordEncoder.matches(passwordActual, usuario.getPassword());
    }
}
