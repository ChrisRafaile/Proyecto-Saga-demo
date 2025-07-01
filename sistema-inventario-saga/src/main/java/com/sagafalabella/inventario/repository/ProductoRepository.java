package com.sagafalabella.inventario.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sagafalabella.inventario.model.Producto;

/**
 * Repositorio para la entidad Producto
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca un producto por su código único
     */
    Optional<Producto> findByCodigoProducto(String codigoProducto);

    /**
     * Busca productos activos
     */
    List<Producto> findByActivoTrue();

    /**
     * Busca productos por categoría
     */
    List<Producto> findByCategoriaAndActivoTrue(String categoria);

    /**
     * Busca productos por nombre o descripción (búsqueda parcial)
     */
    @Query("SELECT p FROM Producto p WHERE " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))) AND " +
           "p.activo = true")
    Page<Producto> buscarPorTexto(@Param("texto") String texto, Pageable pageable);

    /**
     * Encuentra productos con stock bajo (menor o igual al stock mínimo)
     */
    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo AND p.activo = true")
    List<Producto> findProductosConStockBajo();    /**
     * Encuentra productos próximos a vencer
     */
    @Query("SELECT p FROM Producto p WHERE p.fechavencimiento <= :fecha AND p.activo = true")
    List<Producto> findProductosProximosAVencer(@Param("fecha") LocalDate fecha);

    /**
     * Encuentra productos sin stock
     */
    @Query("SELECT p FROM Producto p WHERE p.stockActual = 0 AND p.activo = true")
    List<Producto> findProductosSinStock();

    /**
     * Busca productos por proveedor
     */
    @Query("SELECT p FROM Producto p WHERE p.proveedor.id = :proveedorId AND p.activo = true")
    List<Producto> findByProveedorId(@Param("proveedorId") Long proveedorId);

    /**
     * Busca productos por marca
     */
    List<Producto> findByMarcaAndActivoTrue(String marca);

    /**
     * Busca productos por ubicación en almacén
     */
    List<Producto> findByUbicacionAlmacenAndActivoTrue(String ubicacionAlmacen);

    /**
     * Verifica si existe un producto con el código dado
     */
    boolean existsByCodigoProducto(String codigoProducto);

    /**
     * Cuenta productos activos
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.activo = true")
    Long contarProductosActivos();

    /**
     * Busca productos con stock disponible para una cantidad específica
     */
    @Query("SELECT p FROM Producto p WHERE p.stockActual >= :cantidad AND p.activo = true")
    List<Producto> findProductosConStockDisponible(@Param("cantidad") Integer cantidad);

    /**
     * Obtiene las categorías de productos distintas
     */
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.categoria IS NOT NULL AND p.activo = true")
    List<String> findCategorias();

    /**
     * Obtiene las marcas de productos distintas
     */
    @Query("SELECT DISTINCT p.marca FROM Producto p WHERE p.marca IS NOT NULL AND p.activo = true")
    List<String> findMarcas();
}
