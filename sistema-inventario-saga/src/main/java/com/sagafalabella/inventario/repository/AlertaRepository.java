package com.sagafalabella.inventario.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sagafalabella.inventario.model.Alerta;
import com.sagafalabella.inventario.model.Usuario;

/**
 * Repositorio para gestionar las alertas del sistema
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    
    /**
     * Buscar alertas por estado
     */
    List<Alerta> findByEstadoOrderByFechaCreacionDesc(Alerta.EstadoAlerta estado);
    
    /**
     * Buscar alertas por tipo
     */
    List<Alerta> findByTipoAlertaOrderByFechaCreacionDesc(Alerta.TipoAlerta tipoAlerta);
      /**
     * Buscar alertas por nivel de prioridad
     */
    List<Alerta> findByNivelPrioridadOrderByFechaCreacionDesc(Alerta.NivelPrioridad nivelPrioridad);
    
    /**
     * Buscar alertas no leídas
     */
    @Query("SELECT a FROM Alerta a WHERE a.estado = 'NO_LEIDA' ORDER BY a.fechaCreacion DESC")
    List<Alerta> findAlertasNoLeidas();
    
    /**
     * Buscar alertas críticas
     */
    @Query("SELECT a FROM Alerta a WHERE a.nivelPrioridad = 'CRITICA' AND a.estado != 'RESUELTA' ORDER BY a.fechaCreacion DESC")
    List<Alerta> findAlertasCriticas();
      /**
     * Contar alertas por estado
     */
    long countByEstado(Alerta.EstadoAlerta estado);
    
    /**
     * Contar alertas críticas no resueltas
     */
    @Query("SELECT COUNT(a) FROM Alerta a WHERE a.nivelPrioridad = 'CRITICA' AND a.estado != 'RESUELTA'")
    long countAlertasCriticasNoResueltas();
    
    /**
     * Contar alertas de stock bajo
     */
    @Query("SELECT COUNT(a) FROM Alerta a WHERE a.tipoAlerta = 'STOCK_BAJO' AND a.estado != 'RESUELTA'")
    long countAlertasStockBajo();
    
    /**
     * Buscar alertas por usuario asignado
     */
    List<Alerta> findByUsuarioAsignadoOrderByFechaCreacionDesc(Usuario usuarioAsignado);
    
    /**
     * Buscar alertas por rango de fechas
     */
    @Query("SELECT a FROM Alerta a WHERE a.fechaCreacion BETWEEN :fechaInicio AND :fechaFin ORDER BY a.fechaCreacion DESC")
    List<Alerta> findByFechaCreacionBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                           @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Buscar alertas con filtros múltiples - versión corregida para PostgreSQL
     */
    @Query("SELECT a FROM Alerta a WHERE " +
           "(:tipoAlerta IS NULL OR a.tipoAlerta = :tipoAlerta) AND " +
           "(:estado IS NULL OR a.estado = :estado) AND " +
           "(:nivelPrioridad IS NULL OR a.nivelPrioridad = :nivelPrioridad) AND " +
           "(:fechaInicio IS NULL OR a.fechaCreacion >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR a.fechaCreacion <= :fechaFin) " +
           "ORDER BY a.fechaCreacion DESC")
    Page<Alerta> findAlertasConFiltros(@Param("tipoAlerta") Alerta.TipoAlerta tipoAlerta,
                                      @Param("estado") Alerta.EstadoAlerta estado,
                                      @Param("nivelPrioridad") Alerta.NivelPrioridad nivelPrioridad,
                                      @Param("fechaInicio") LocalDateTime fechaInicio,
                                      @Param("fechaFin") LocalDateTime fechaFin,
                                      Pageable pageable);

    /**
     * Método alternativo usando SQL nativo para evitar problemas con enums nulos
     */
    @Query(value = "SELECT * FROM alertas a WHERE " +
           "(:tipoAlerta IS NULL OR a.tipo_alerta = CAST(:tipoAlerta AS VARCHAR)) AND " +
           "(:estado IS NULL OR a.estado = CAST(:estado AS VARCHAR)) AND " +
           "(:nivelPrioridad IS NULL OR a.nivel_prioridad = CAST(:nivelPrioridad AS VARCHAR)) AND " +
           "(:fechaInicio IS NULL OR a.fecha_creacion >= CAST(:fechaInicio AS TIMESTAMP)) AND " +
           "(:fechaFin IS NULL OR a.fecha_creacion <= CAST(:fechaFin AS TIMESTAMP)) " +
           "ORDER BY a.fecha_creacion DESC",
           countQuery = "SELECT COUNT(*) FROM alertas a WHERE " +
           "(:tipoAlerta IS NULL OR a.tipo_alerta = CAST(:tipoAlerta AS VARCHAR)) AND " +
           "(:estado IS NULL OR a.estado = CAST(:estado AS VARCHAR)) AND " +
           "(:nivelPrioridad IS NULL OR a.nivel_prioridad = CAST(:nivelPrioridad AS VARCHAR)) AND " +
           "(:fechaInicio IS NULL OR a.fecha_creacion >= CAST(:fechaInicio AS TIMESTAMP)) AND " +
           "(:fechaFin IS NULL OR a.fecha_creacion <= CAST(:fechaFin AS TIMESTAMP))",
           nativeQuery = true)
    Page<Alerta> findAlertasConFiltrosNativo(@Param("tipoAlerta") String tipoAlerta,
                                            @Param("estado") String estado,
                                            @Param("nivelPrioridad") String nivelPrioridad,
                                            @Param("fechaInicio") LocalDateTime fechaInicio,
                                            @Param("fechaFin") LocalDateTime fechaFin,
                                            Pageable pageable);
    
    /**
     * Buscar todas las alertas sin filtros - método de respaldo
     */
    @Query("SELECT a FROM Alerta a ORDER BY a.fechaCreacion DESC")
    Page<Alerta> findTodasLasAlertas(Pageable pageable);
    
    /**
     * Buscar alertas recientes (últimas 24 horas)
     */
    @Query("SELECT a FROM Alerta a WHERE a.fechaCreacion >= :fechaLimite ORDER BY a.fechaCreacion DESC")
    List<Alerta> findAlertasRecientes(@Param("fechaLimite") LocalDateTime fechaLimite);
      /**
     * Buscar alertas por entidad relacionada
     */
    @Query("SELECT a FROM Alerta a WHERE a.entidadRelacionada = :entidad AND a.idEntidadRelacionada = :idEntidad ORDER BY a.fechaCreacion DESC")
    List<Alerta> findByEntidadRelacionada(@Param("entidad") String entidad, @Param("idEntidad") Long idEntidad);
    
    /**
     * Marcar múltiples alertas como leídas
     */
    @Modifying
    @Transactional
    @Query("UPDATE Alerta a SET a.estado = 'LEIDA', a.fechaLeida = :fechaLeida WHERE a.idAlerta IN :ids AND a.estado = 'NO_LEIDA'")
    void marcarComoLeidasPorIds(@Param("ids") List<Long> ids, @Param("fechaLeida") LocalDateTime fechaLeida);
    
    /**
     * Eliminar alertas antiguas (más de X días)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Alerta a WHERE a.fechaCreacion < :fechaLimite AND a.estado = 'RESUELTA'")
    void eliminarAlertasAntiguas(@Param("fechaLimite") LocalDateTime fechaLimite);
    
    /**
     * Verificar si existe una alerta con el título especificado
     */
    boolean existsByTitulo(String titulo);
}
