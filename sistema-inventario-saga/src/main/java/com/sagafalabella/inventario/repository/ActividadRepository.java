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

import com.sagafalabella.inventario.model.Actividad;
import com.sagafalabella.inventario.model.Actividad.NivelActividad;
import com.sagafalabella.inventario.model.Actividad.TipoActividad;

/**
 * Repositorio para la entidad Actividad
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    /**
     * Buscar actividades por usuario
     */
    @Query("SELECT a FROM Actividad a WHERE a.usuario.id = :usuarioId ORDER BY a.fechaActividad DESC")
    List<Actividad> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Buscar actividades por tipo
     */
    List<Actividad> findByTipoActividad(TipoActividad tipoActividad);

    /**
     * Buscar actividades por nivel
     */
    List<Actividad> findByNivel(NivelActividad nivel);

    /**
     * Buscar actividades por entidad
     */
    List<Actividad> findByEntidad(String entidad);

    /**
     * Buscar actividades en un rango de fechas
     */
    @Query("SELECT a FROM Actividad a WHERE a.fechaActividad BETWEEN :fechaInicio AND :fechaFin ORDER BY a.fechaActividad DESC")
    List<Actividad> findByFechaActividadBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                               @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Búsqueda con filtros múltiples
     */
    @Query("SELECT a FROM Actividad a WHERE " +
           "(:usuarioId IS NULL OR a.usuario.id = :usuarioId) AND " +
           "(:accion IS NULL OR a.accion LIKE %:accion%) AND " +
           "(:entidad IS NULL OR a.entidad = :entidad) AND " +
           "(:tipoActividad IS NULL OR a.tipoActividad = :tipoActividad) AND " +
           "(:nivel IS NULL OR a.nivel = :nivel) AND " +
           "(:resultado IS NULL OR a.resultado = :resultado) AND " +
           "(:fechaDesde IS NULL OR a.fechaActividad >= :fechaDesde) AND " +
           "(:fechaHasta IS NULL OR a.fechaActividad <= :fechaHasta) " +
           "ORDER BY a.fechaActividad DESC")
    Page<Actividad> findWithFilters(@Param("usuarioId") Long usuarioId,
                                   @Param("accion") String accion,
                                   @Param("entidad") String entidad,
                                   @Param("tipoActividad") TipoActividad tipoActividad,
                                   @Param("nivel") NivelActividad nivel,
                                   @Param("resultado") String resultado,
                                   @Param("fechaDesde") LocalDateTime fechaDesde,
                                   @Param("fechaHasta") LocalDateTime fechaHasta,
                                   Pageable pageable);

    /**
     * Obtener actividades recientes (últimas 24 horas)
     */
    @Query("SELECT a FROM Actividad a WHERE a.fechaActividad >= :fechaDesde ORDER BY a.fechaActividad DESC")
    List<Actividad> findActividadesRecientes(@Param("fechaDesde") LocalDateTime fechaDesde);

    /**
     * Contar actividades por tipo
     */
    @Query("SELECT a.tipoActividad, COUNT(a) FROM Actividad a GROUP BY a.tipoActividad")
    List<Object[]> countByTipoActividad();

    /**
     * Contar actividades por nivel
     */
    @Query("SELECT a.nivel, COUNT(a) FROM Actividad a GROUP BY a.nivel")
    List<Object[]> countByNivel();

    /**
     * Estadísticas de actividades por día
     */
    @Query("SELECT DATE(a.fechaActividad), COUNT(a) " +
           "FROM Actividad a " +
           "WHERE a.fechaActividad >= :fechaDesde " +
           "GROUP BY DATE(a.fechaActividad) " +
           "ORDER BY DATE(a.fechaActividad)")
    List<Object[]> getEstadisticasPorDia(@Param("fechaDesde") LocalDateTime fechaDesde);

    /**
     * Buscar actividades de error
     */
    @Query("SELECT a FROM Actividad a WHERE a.resultado = 'ERROR' OR a.nivel = :nivelError ORDER BY a.fechaActividad DESC")
    List<Actividad> findActividadesError(@Param("nivelError") NivelActividad nivelError);

    /**
     * Eliminar actividades antiguas
     */
    @Modifying
    @Query("DELETE FROM Actividad a WHERE a.fechaActividad < :fechaLimite")
    int deleteActividadesAntiguas(@Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Contar actividades por entidad
     */
    @Query("SELECT a.entidad, COUNT(a) FROM Actividad a WHERE a.entidad IS NOT NULL GROUP BY a.entidad")
    List<Object[]> countByEntidad();

    /**
     * Buscar actividades por IP
     */
    @Query("SELECT a FROM Actividad a WHERE a.direccionIp = :ip ORDER BY a.fechaActividad DESC")
    List<Actividad> findByDireccionIp(@Param("ip") String ip);

    /**
     * Obtener últimas actividades de un usuario
     */
    @Query("SELECT a FROM Actividad a WHERE a.usuario.id = :usuarioId ORDER BY a.fechaActividad DESC")
    Page<Actividad> findRecentByUsuario(@Param("usuarioId") Long usuarioId, Pageable pageable);
}
