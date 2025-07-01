package com.sagafalabella.inventario.repository;

import com.sagafalabella.inventario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Buscar usuario por username (activo)
     */
    Optional<Usuario> findByUsernameAndActivoTrue(String username);

    /**
     * Buscar usuario por email (activo)
     */
    Optional<Usuario> findByEmailAndActivoTrue(String email);

    /**
     * Verificar si existe un username
     */
    boolean existsByUsername(String username);

    /**
     * Verificar si existe un email
     */
    boolean existsByEmail(String email);

    /**
     * Listar usuarios activos
     */
    List<Usuario> findByActivoTrue();

    /**
     * Buscar por tipo de usuario y activo
     */
    List<Usuario> findByTipoUsuarioAndActivoTrue(Usuario.TipoUsuario tipoUsuario);

    /**
     * Buscar por rol y activo
     */
    List<Usuario> findByRolAndActivoTrue(Usuario.RolUsuario rol);

    /**
     * Contar usuarios por tipo y activos
     */
    long countByTipoUsuarioAndActivoTrue(Usuario.TipoUsuario tipoUsuario);

    /**
     * Contar usuarios activos
     */
    long countByActivoTrue();

    /**
     * Buscar usuarios por nombre o apellido (búsqueda)
     */
    @Query("SELECT u FROM Usuario u WHERE u.activo = true AND " +
           "(LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    List<Usuario> buscarPorNombreOApellido(@Param("busqueda") String busqueda);

    /**
     * Buscar administradores activos
     */
    @Query("SELECT u FROM Usuario u WHERE u.activo = true AND " +
           "(u.rol = 'SUPER_ADMIN' OR u.rol = 'ADMIN_INVENTARIO' OR u.rol = 'ADMIN_VENTAS')")
    List<Usuario> findAdministradoresActivos();

    /**
     * Buscar empleados activos
     */
    @Query("SELECT u FROM Usuario u WHERE u.activo = true AND " +
           "(u.rol = 'EMPLEADO_ALMACEN' OR u.rol = 'EMPLEADO_VENTAS')")
    List<Usuario> findEmpleadosActivos();
}
