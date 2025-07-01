package com.sagafalabella.inventario.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sagafalabella.inventario.model.Pedido;
import com.sagafalabella.inventario.model.Pedido.EstadoPedido;
import com.sagafalabella.inventario.model.Pedido.TipoEntrega;

/**
 * Repositorio para la entidad Pedido
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Buscar pedido por número de pedido
     */
    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    /**
     * Buscar pedidos por estado
     */
    List<Pedido> findByEstado(EstadoPedido estado);

    /**
     * Buscar pedidos por cliente
     */
    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId")
    List<Pedido> findByClienteId(@Param("clienteId") Long clienteId);

    /**
     * Buscar pedidos por tipo de entrega
     */
    List<Pedido> findByTipoEntrega(TipoEntrega tipoEntrega);

    /**
     * Buscar pedidos en un rango de fechas
     */
    @Query("SELECT p FROM Pedido p WHERE p.fechaPedido BETWEEN :fechaInicio AND :fechaFin")
    List<Pedido> findByFechaPedidoBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                         @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Buscar pedidos pendientes de entrega
     */
    @Query("SELECT p FROM Pedido p WHERE p.estado IN :estados")
    List<Pedido> findByEstadoIn(@Param("estados") List<EstadoPedido> estados);

    /**
     * Búsqueda con filtros múltiples
     */
    @Query("SELECT p FROM Pedido p WHERE " +
           "(:numeroPedido IS NULL OR p.numeroPedido LIKE %:numeroPedido%) AND " +
           "(:estado IS NULL OR p.estado = :estado) AND " +
           "(:tipoEntrega IS NULL OR p.tipoEntrega = :tipoEntrega) AND " +
           "(:clienteId IS NULL OR p.cliente.id = :clienteId) AND " +
           "(:fechaDesde IS NULL OR p.fechaPedido >= :fechaDesde) AND " +
           "(:fechaHasta IS NULL OR p.fechaPedido <= :fechaHasta)")
    Page<Pedido> findWithFilters(@Param("numeroPedido") String numeroPedido,
                                @Param("estado") EstadoPedido estado,
                                @Param("tipoEntrega") TipoEntrega tipoEntrega,
                                @Param("clienteId") Long clienteId,
                                @Param("fechaDesde") LocalDateTime fechaDesde,
                                @Param("fechaHasta") LocalDateTime fechaHasta,
                                Pageable pageable);

    /**
     * Contar pedidos por estado
     */
    @Query("SELECT p.estado, COUNT(p) FROM Pedido p GROUP BY p.estado")
    List<Object[]> countByEstado();

    /**
     * Estadísticas de pedidos por mes
     */
    @Query("SELECT YEAR(p.fechaPedido), MONTH(p.fechaPedido), COUNT(p), SUM(p.total) " +
           "FROM Pedido p " +
           "WHERE p.fechaPedido >= :fechaDesde " +
           "GROUP BY YEAR(p.fechaPedido), MONTH(p.fechaPedido) " +
           "ORDER BY YEAR(p.fechaPedido), MONTH(p.fechaPedido)")
    List<Object[]> getEstadisticasPorMes(@Param("fechaDesde") LocalDateTime fechaDesde);

    /**
     * Verificar si existe un pedido con el número dado
     */
    boolean existsByNumeroPedido(String numeroPedido);

    /**
     * Buscar pedidos por cliente con paginación
     */
    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId")
    Page<Pedido> findByClienteIdWithPagination(@Param("clienteId") Long clienteId, Pageable pageable);
}
