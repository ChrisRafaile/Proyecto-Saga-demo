package com.sagafalabella.inventario.repository;

import com.sagafalabella.inventario.model.Cliente;
import com.sagafalabella.inventario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Cliente
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Buscar cliente por email (activo)
     */
    Optional<Cliente> findByEmailAndActivoTrue(String email);

    /**
     * Verificar si existe un email
     */
    boolean existsByEmail(String email);

    /**
     * Listar clientes activos
     */
    List<Cliente> findByActivoTrue();

    /**
     * Buscar por tipo de cliente y activo
     */
    List<Cliente> findByTipoClienteAndActivoTrue(Cliente.TipoCliente tipoCliente);

    /**
     * Buscar cliente por usuario
     */
    Optional<Cliente> findByUsuario(Usuario usuario);

    /**
     * Contar clientes activos
     */
    long countByActivoTrue();

    /**
     * Buscar clientes por nombre o apellido (búsqueda)
     */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND " +
           "(LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    List<Cliente> buscarPorNombreOApellido(@Param("busqueda") String busqueda);

    /**
     * Buscar clientes por ciudad
     */
    List<Cliente> findByCiudadAndActivoTrue(String ciudad);

    /**
     * Buscar clientes VIP activos
     */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND c.tipoCliente = 'VIP'")
    List<Cliente> findClientesVIP();

    /**
     * Buscar clientes con usuarios asociados
     */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND c.usuario IS NOT NULL")
    List<Cliente> findClientesConUsuario();

    /**
     * Buscar clientes sin usuarios asociados
     */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND c.usuario IS NULL")
    List<Cliente> findClientesSinUsuario();
}
