package com.sagafalabella.inventario.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sagafalabella.inventario.model.Proveedor;
import com.sagafalabella.inventario.repository.ProveedorRepository;

/**
 * Servicio para gestión de proveedores
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Service
@Transactional
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    /**
     * Obtener todos los proveedores
     */
    public List<Proveedor> obtenerTodosLosProveedores() {
        return proveedorRepository.findAll();
    }

    /**
     * Obtener proveedores activos
     */
    public List<Proveedor> obtenerProveedoresActivos() {
        return proveedorRepository.findByActivoTrue();
    }

    /**
     * Buscar proveedor por ID
     */
    public Optional<Proveedor> buscarPorId(Long id) {
        return proveedorRepository.findById(id);
    }

    /**
     * Buscar proveedor por RUC
     */
    public Optional<Proveedor> buscarPorRuc(String ruc) {
        return proveedorRepository.findByRuc(ruc);
    }

    /**
     * Verificar si RUC está disponible
     */
    public boolean isRucDisponible(String ruc) {
        return !proveedorRepository.existsByRuc(ruc);
    }

    /**
     * Guardar proveedor
     */
    public Proveedor guardarProveedor(Proveedor proveedor) {
        if (proveedor.getFechaRegistro() == null) {
            proveedor.setFechaRegistro(LocalDateTime.now());
        }
        proveedor.setFechaActualizacion(LocalDateTime.now());
        
        return proveedorRepository.save(proveedor);
    }

    /**
     * Actualizar proveedor existente
     */
    public Proveedor actualizarProveedor(Proveedor proveedor) {
        if (proveedor.getId() == null) {
            throw new IllegalArgumentException("El ID del proveedor es requerido para la actualización");
        }
        
        // Verificar que el proveedor existe
        Optional<Proveedor> proveedorExistente = proveedorRepository.findById(proveedor.getId());
        if (proveedorExistente.isEmpty()) {
            throw new RuntimeException("Proveedor no encontrado con ID: " + proveedor.getId());
        }
        
        // Mantener la fecha de registro original
        Proveedor existing = proveedorExistente.get();
        proveedor.setFechaRegistro(existing.getFechaRegistro());
        proveedor.setFechaActualizacion(LocalDateTime.now());
        
        return proveedorRepository.save(proveedor);
    }

    /**
     * Eliminar proveedor (soft delete)
     */
    public void eliminarProveedor(Long id) {
        Optional<Proveedor> proveedor = proveedorRepository.findById(id);
        if (proveedor.isPresent()) {
            Proveedor p = proveedor.get();
            p.setActivo(false);
            p.setFechaActualizacion(LocalDateTime.now());
            proveedorRepository.save(p);
        }
    }

    /**
     * Activar proveedor
     */
    public void activarProveedor(Long id) {
        Optional<Proveedor> proveedor = proveedorRepository.findById(id);
        if (proveedor.isPresent()) {
            Proveedor p = proveedor.get();
            p.setActivo(true);
            p.setFechaActualizacion(LocalDateTime.now());
            proveedorRepository.save(p);
        }
    }

    /**
     * Contar proveedores activos
     */
    public long contarProveedoresActivos() {
        return proveedorRepository.countByActivoTrue();
    }

    /**
     * Contar total de proveedores
     */
    public long contarTotalProveedores() {
        return proveedorRepository.count();
    }
}
