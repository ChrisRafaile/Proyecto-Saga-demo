package com.sagafalabella.inventario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sagafalabella.inventario.model.Proveedor;

/**
 * Repositorio para gestión de proveedores
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    /**
     * Buscar proveedores activos
     */
    List<Proveedor> findByActivoTrue();

    /**
     * Buscar proveedor por RUC
     */
    Optional<Proveedor> findByRuc(String ruc);

    /**
     * Verificar si existe proveedor por RUC
     */
    boolean existsByRuc(String ruc);

    /**
     * Contar proveedores activos
     */
    long countByActivoTrue();

    /**
     * Buscar proveedores por nombre comercial
     */
    @Query("SELECT p FROM Proveedor p WHERE p.nombreComercial LIKE %:nombre% AND p.activo = true")
    List<Proveedor> findByNombreComercialContainingIgnoreCase(String nombre);

    /**
     * Buscar proveedores por razón social
     */
    @Query("SELECT p FROM Proveedor p WHERE p.razonSocial LIKE %:razon% AND p.activo = true")
    List<Proveedor> findByRazonSocialContainingIgnoreCase(String razon);

    /**
     * Buscar proveedores por calificación mínima
     */
    @Query("SELECT p FROM Proveedor p WHERE p.calificacion >= :calificacion AND p.activo = true")
    List<Proveedor> findByCalificacionGreaterThanEqual(Integer calificacion);
}
