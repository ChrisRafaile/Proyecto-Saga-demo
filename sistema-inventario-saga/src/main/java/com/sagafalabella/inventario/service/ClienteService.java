package com.sagafalabella.inventario.service;

import com.sagafalabella.inventario.model.Cliente;
import com.sagafalabella.inventario.model.Usuario;
import com.sagafalabella.inventario.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de clientes
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Service
@Transactional
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Registrar cliente con usuario asociado
     */
    public Cliente registrarClienteConUsuario(Cliente cliente, Usuario usuario) {
        // Registrar el usuario primero
        Usuario usuarioRegistrado = usuarioService.registrarCliente(usuario);
        
        // Asociar el usuario al cliente
        cliente.setUsuario(usuarioRegistrado);
        
        // Guardar el cliente
        return clienteRepository.save(cliente);
    }

    /**
     * Registrar cliente sin usuario (solo datos básicos)
     */
    public Cliente registrarCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    /**
     * Buscar cliente por ID
     */
    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    /**
     * Buscar cliente por email
     */
    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmailAndActivoTrue(email);
    }

    /**
     * Listar todos los clientes activos
     */
    public List<Cliente> listarClientesActivos() {
        return clienteRepository.findByActivoTrue();
    }

    /**
     * Buscar clientes por nombre
     */
    public List<Cliente> buscarPorNombre(String nombre) {
        return clienteRepository.buscarPorNombreOApellido(nombre);
    }

    /**
     * Activar/Desactivar cliente
     */
    public Cliente cambiarEstadoCliente(Long id, boolean activo) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        
        cliente.setActivo(activo);
        
        // Si el cliente tiene usuario, también cambiar su estado
        if (cliente.tieneUsuario()) {
            usuarioService.cambiarEstadoUsuario(cliente.getUsuario().getId(), activo);
        }
        
        return clienteRepository.save(cliente);
    }

    /**
     * Actualizar información del cliente
     */
    public Cliente actualizarCliente(Cliente cliente) {
        Cliente clienteExistente = clienteRepository.findById(cliente.getId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        
        // Actualizar campos
        clienteExistente.setNombre(cliente.getNombre());
        clienteExistente.setApellido(cliente.getApellido());
        clienteExistente.setTelefono(cliente.getTelefono());
        clienteExistente.setDireccion(cliente.getDireccion());
        clienteExistente.setCiudad(cliente.getCiudad());
        clienteExistente.setCodigoPostal(cliente.getCodigoPostal());
        clienteExistente.setTipoCliente(cliente.getTipoCliente());
        
        return clienteRepository.save(clienteExistente);
    }

    /**
     * Contar clientes activos
     */
    public long contarClientesActivos() {
        return clienteRepository.countByActivoTrue();
    }

    /**
     * Buscar cliente por usuario
     */
    public Optional<Cliente> buscarPorUsuario(Usuario usuario) {
        return clienteRepository.findByUsuario(usuario);
    }

    /**
     * Listar clientes por tipo
     */
    public List<Cliente> listarPorTipo(Cliente.TipoCliente tipo) {
        return clienteRepository.findByTipoClienteAndActivoTrue(tipo);
    }

    /**
     * Verificar si un email ya está registrado
     */
    public boolean emailYaRegistrado(String email) {
        return clienteRepository.existsByEmail(email);
    }
}
