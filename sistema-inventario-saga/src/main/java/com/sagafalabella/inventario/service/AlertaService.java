package com.sagafalabella.inventario.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sagafalabella.inventario.model.Alerta;
import com.sagafalabella.inventario.repository.AlertaRepository;

/**
 * Servicio para gestionar las alertas del sistema
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Service
public class AlertaService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertaService.class);
      @Autowired
    private AlertaRepository alertaRepository;
    
    /**
     * Crear una nueva alerta
     */
    public Alerta crearAlerta(Alerta alerta) {
        try {
            if (alerta.getFechaCreacion() == null) {
                alerta.setFechaCreacion(LocalDateTime.now());
            }
            if (alerta.getEstado() == null) {
                alerta.setEstado(Alerta.EstadoAlerta.NO_LEIDA);
            }
            if (alerta.getNotificacionEnviada() == null) {
                alerta.setNotificacionEnviada(false);
            }
            
            Alerta alertaGuardada = alertaRepository.save(alerta);
            logger.info("Alerta creada exitosamente: {}", alertaGuardada.getIdAlerta());
            return alertaGuardada;
        } catch (Exception e) {
            logger.error("Error al crear alerta: {}", e.getMessage());
            throw new RuntimeException("Error al crear la alerta: " + e.getMessage());
        }
    }
    
    /**
     * Crear alerta de stock bajo
     */
    public Alerta crearAlertaStockBajo(String nombreProducto, int stockActual, int stockMinimo) {
        Alerta alerta = new Alerta();
        alerta.setTitulo("Stock Bajo: " + nombreProducto);
        alerta.setDescripcion(String.format("El producto '%s' tiene solo %d unidades en stock (mínimo: %d)", 
                             nombreProducto, stockActual, stockMinimo));
        alerta.setTipoAlerta(Alerta.TipoAlerta.STOCK_BAJO);
        alerta.setNivelPrioridad(stockActual <= 5 ? Alerta.NivelPrioridad.CRITICA : Alerta.NivelPrioridad.ALTA);
        alerta.setEntidadRelacionada("Producto");
        return crearAlerta(alerta);
    }
    
    /**
     * Crear alerta de sistema
     */
    public Alerta crearAlertaSistema(String titulo, String descripcion, Alerta.NivelPrioridad prioridad) {
        Alerta alerta = new Alerta();
        alerta.setTitulo(titulo);
        alerta.setDescripcion(descripcion);
        alerta.setTipoAlerta(Alerta.TipoAlerta.SISTEMA);
        alerta.setNivelPrioridad(prioridad);
        alerta.setEntidadRelacionada("Sistema");
        return crearAlerta(alerta);
    }
    
    /**
     * Crear alerta de usuario
     */
    public Alerta crearAlertaUsuario(String titulo, String descripcion, Long usuarioId) {
        Alerta alerta = new Alerta();
        alerta.setTitulo(titulo);
        alerta.setDescripcion(descripcion);
        alerta.setTipoAlerta(Alerta.TipoAlerta.USUARIO);
        alerta.setNivelPrioridad(Alerta.NivelPrioridad.MEDIA);
        alerta.setEntidadRelacionada("Usuario");
        alerta.setIdEntidadRelacionada(usuarioId);
        return crearAlerta(alerta);
    }
    
    /**
     * Obtener todas las alertas con paginación
     */
    @Transactional(readOnly = true)
    public Page<Alerta> obtenerTodasConPaginacion(int page, int size, String sortBy, String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            return alertaRepository.findAll(pageable);
        } catch (Exception e) {
            logger.error("Error al obtener alertas con paginación: {}", e.getMessage());
            return Page.empty();
        }
    }
      /**
     * Buscar alertas con filtros
     */
    public Page<Alerta> buscarConFiltros(Alerta.TipoAlerta tipoAlerta, 
                                         Alerta.EstadoAlerta estado,
                                         Alerta.NivelPrioridad nivelPrioridad,
                                         LocalDateTime fechaInicio,
                                         LocalDateTime fechaFin,
                                         Pageable pageable) {
        try {
            logger.info("Buscando alertas con filtros - Tipo: {}, Estado: {}, Prioridad: {}, FechaInicio: {}, FechaFin: {}", 
                       tipoAlerta, estado, nivelPrioridad, fechaInicio, fechaFin);
            
            // Verificar si todos los filtros son nulos
            if (tipoAlerta == null && estado == null && nivelPrioridad == null && 
                fechaInicio == null && fechaFin == null) {
                
                logger.info("Todos los filtros son nulos, usando findAll básico");
                Page<Alerta> resultado = alertaRepository.findAll(pageable);
                logger.info("Resultado findAll - Total: {}, Contenido: {}", 
                           resultado.getTotalElements(), resultado.getContent().size());
                return resultado;
            }
            
            // Convertir enums a strings para evitar problemas con PostgreSQL
            // Si viene "todos", se trata como null para no filtrar
            String tipoStr = (tipoAlerta != null) ? tipoAlerta.name() : null;
            String estadoStr = (estado != null) ? estado.name() : null;
            String prioridadStr = (nivelPrioridad != null) ? nivelPrioridad.name() : null;
            
            logger.info("Usando consulta nativa con strings - Tipo: {}, Estado: {}, Prioridad: {}", 
                       tipoStr, estadoStr, prioridadStr);
            
            // Usar la consulta nativa con strings
            Page<Alerta> resultado = alertaRepository.findAlertasConFiltrosNativo(
                tipoStr, estadoStr, prioridadStr, fechaInicio, fechaFin, pageable);
            
            logger.info("Resultado con filtros nativos - Total: {}, Contenido: {}, Vacío: {}", 
                       resultado.getTotalElements(), resultado.getContent().size(), resultado.isEmpty());
            
            if (!resultado.getContent().isEmpty()) {
                logger.info("Primera alerta encontrada: ID={}, Título={}", 
                           resultado.getContent().get(0).getIdAlerta(), 
                           resultado.getContent().get(0).getTitulo());
            } else {
                logger.info("Consulta con filtros devolvió página vacía - esto es correcto si no hay alertas que cumplan los criterios");
            }
            
            return resultado;
        } catch (Exception e) {
            logger.error("Error al buscar alertas con filtros: {}", e.getMessage(), e);
            
            // Como última opción, usar findAll básico
            try {
                logger.warn("Usando findAll básico como última opción...");
                return alertaRepository.findAll(pageable);
            } catch (Exception ex) {
                logger.error("Error también en findAll básico: {}", ex.getMessage());
                return Page.empty();
            }
        }
    }
    
    /**
     * Obtener alerta por ID
     */
    @Transactional(readOnly = true)
    public Optional<Alerta> obtenerPorId(Long id) {
        try {
            return alertaRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error al obtener alerta por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Obtener alertas no leídas
     */
    @Transactional(readOnly = true)
    public List<Alerta> obtenerAlertasNoLeidas() {
        try {
            return alertaRepository.findAlertasNoLeidas();
        } catch (Exception e) {
            logger.error("Error al obtener alertas no leídas: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Obtener alertas críticas
     */
    @Transactional(readOnly = true)
    public List<Alerta> obtenerAlertasCriticas() {
        try {
            return alertaRepository.findAlertasCriticas();
        } catch (Exception e) {
            logger.error("Error al obtener alertas críticas: {}", e.getMessage());
            return List.of();
        }
    }
      /**
     * Obtener alertas recientes (últimas 24 horas) - versión simplificada
     */
    public List<Alerta> obtenerAlertasRecientes() {
        try {
            LocalDateTime fechaLimite = LocalDateTime.now().minusHours(24);
            List<Alerta> alertas = alertaRepository.findAlertasRecientes(fechaLimite);
            logger.info("Encontradas {} alertas recientes desde {}", alertas.size(), fechaLimite);
            return alertas;
        } catch (Exception e) {
            logger.error("Error al obtener alertas recientes: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Marcar alerta como leída
     */
    public void marcarComoLeida(Long alertaId) {
        try {
            Optional<Alerta> alertaOpt = alertaRepository.findById(alertaId);
            if (alertaOpt.isPresent()) {
                Alerta alerta = alertaOpt.get();
                alerta.marcarComoLeida();
                alertaRepository.save(alerta);
                logger.info("Alerta {} marcada como leída", alertaId);
            }
        } catch (Exception e) {
            logger.error("Error al marcar alerta {} como leída: {}", alertaId, e.getMessage());
            throw new RuntimeException("Error al marcar la alerta como leída");
        }
    }
    
    /**
     * Marcar múltiples alertas como leídas
     */
    public void marcarVariasComoLeidas(List<Long> alertaIds) {
        try {
            for (Long id : alertaIds) {
                marcarComoLeida(id);
            }
            logger.info("Marcadas {} alertas como leídas", alertaIds.size());
        } catch (Exception e) {
            logger.error("Error al marcar múltiples alertas como leídas: {}", e.getMessage());
            throw new RuntimeException("Error al marcar las alertas como leídas");
        }
    }
    
    /**
     * Marcar alerta como resuelta
     */
    public void marcarComoResuelta(Long alertaId, String accionesRealizadas) {
        try {
            Optional<Alerta> alertaOpt = alertaRepository.findById(alertaId);
            if (alertaOpt.isPresent()) {
                Alerta alerta = alertaOpt.get();
                alerta.marcarComoResuelta();
                if (accionesRealizadas != null && !accionesRealizadas.trim().isEmpty()) {
                    alerta.setAccionesRealizadas(accionesRealizadas);
                }
                alertaRepository.save(alerta);
                logger.info("Alerta {} marcada como resuelta", alertaId);
            }
        } catch (Exception e) {
            logger.error("Error al marcar alerta {} como resuelta: {}", alertaId, e.getMessage());
            throw new RuntimeException("Error al marcar la alerta como resuelta");
        }
    }
    
    /**
     * Cambiar estado de alerta
     */
    public void cambiarEstado(Long alertaId, Alerta.EstadoAlerta nuevoEstado) {
        try {
            Optional<Alerta> alertaOpt = alertaRepository.findById(alertaId);
            if (alertaOpt.isPresent()) {
                Alerta alerta = alertaOpt.get();
                alerta.setEstado(nuevoEstado);
                
                if (nuevoEstado == Alerta.EstadoAlerta.LEIDA && alerta.getFechaLeida() == null) {
                    alerta.setFechaLeida(LocalDateTime.now());
                }
                if (nuevoEstado == Alerta.EstadoAlerta.RESUELTA && alerta.getFechaResuelta() == null) {
                    alerta.setFechaResuelta(LocalDateTime.now());
                }
                
                alertaRepository.save(alerta);
                logger.info("Estado de alerta {} cambiado a {}", alertaId, nuevoEstado);
            }
        } catch (Exception e) {
            logger.error("Error al cambiar estado de alerta {}: {}", alertaId, e.getMessage());
            throw new RuntimeException("Error al cambiar el estado de la alerta");
        }
    }
    
    /**
     * Actualizar alerta
     */
    public Alerta actualizar(Alerta alerta) {
        try {
            return alertaRepository.save(alerta);
        } catch (Exception e) {
            logger.error("Error al actualizar alerta: {}", e.getMessage());
            throw new RuntimeException("Error al actualizar la alerta");
        }
    }
      /**
     * Eliminar alerta
     */
    public void eliminar(Long alertaId) {
        try {
            if (alertaRepository.existsById(alertaId)) {
                alertaRepository.deleteById(alertaId);
                logger.info("Alerta {} eliminada exitosamente", alertaId);
            } else {
                throw new RuntimeException("Alerta no encontrada");
            }
        } catch (org.springframework.dao.DataAccessException | IllegalArgumentException e) {
            logger.error("Error al eliminar alerta {}: {}", alertaId, e.getMessage());
            throw new RuntimeException("Error al eliminar la alerta");
        }
    }/**
     * Obtener estadísticas de alertas (versión simplificada sin transacción)
     */
    public AlertaEstadisticas obtenerEstadisticas() {
        try {
            AlertaEstadisticas stats = new AlertaEstadisticas();
            
            // Usar consultas directas sin JOIN para evitar problemas de lazy loading
            long totalAlertas = alertaRepository.count();
            long noLeidas = alertaRepository.countByEstado(Alerta.EstadoAlerta.NO_LEIDA);
            long criticas = alertaRepository.countAlertasCriticasNoResueltas();
            long stockBajo = alertaRepository.countAlertasStockBajo();
            
            stats.setTotalAlertas((int) totalAlertas);
            stats.setAlertasNoLeidas((int) noLeidas);
            stats.setAlertasCriticas((int) criticas);
            stats.setAlertasStockBajo((int) stockBajo);
            
            logger.info("Estadísticas calculadas - Total: {}, No leídas: {}, Críticas: {}, Stock bajo: {}", 
                       totalAlertas, noLeidas, criticas, stockBajo);
            
            return stats;
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de alertas: {}", e.getMessage(), e);
            // Devolver estadísticas por defecto en caso de error
            AlertaEstadisticas stats = new AlertaEstadisticas();
            stats.setTotalAlertas(0);
            stats.setAlertasNoLeidas(0);
            stats.setAlertasCriticas(0);
            stats.setAlertasStockBajo(0);
            return stats;
        }
    }
    
    /**
     * Limpiar alertas resueltas antiguas
     */
    public int limpiarAlertasAntiguas(int diasAntiguedad) {
        try {
            LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasAntiguedad);
            List<Alerta> alertasAntiguas = alertaRepository.findAll().stream()
                .filter(a -> a.getEstado() == Alerta.EstadoAlerta.RESUELTA)
                .filter(a -> a.getFechaCreacion().isBefore(fechaLimite))
                .toList();
            
            int eliminadas = alertasAntiguas.size();
            for (Alerta alerta : alertasAntiguas) {
                alertaRepository.delete(alerta);
            }
            
            logger.info("Eliminadas {} alertas antiguas (más de {} días)", eliminadas, diasAntiguedad);
            return eliminadas;
        } catch (Exception e) {
            logger.error("Error al limpiar alertas antiguas: {}", e.getMessage());
            throw new RuntimeException("Error al limpiar alertas antiguas");
        }
    }
    
    /**
     * Clase para estadísticas de alertas
     */
    public static class AlertaEstadisticas {
        private long totalAlertas;
        private long alertasNoLeidas;
        private long alertasCriticas;
        private long alertasStockBajo;
        
        // Getters y setters
        public long getTotalAlertas() { return totalAlertas; }
        public void setTotalAlertas(long totalAlertas) { this.totalAlertas = totalAlertas; }
        
        public long getAlertasNoLeidas() { return alertasNoLeidas; }
        public void setAlertasNoLeidas(long alertasNoLeidas) { this.alertasNoLeidas = alertasNoLeidas; }
        
        public long getAlertasCriticas() { return alertasCriticas; }
        public void setAlertasCriticas(long alertasCriticas) { this.alertasCriticas = alertasCriticas; }
        
        public long getAlertasStockBajo() { return alertasStockBajo; }
        public void setAlertasStockBajo(long alertasStockBajo) { this.alertasStockBajo = alertasStockBajo; }
    }
      /**
     * Crear alertas de ejemplo para testing
     */
    @Transactional
    public void crearAlertasEjemplo() {
        try {
            // Crear alertas de ejemplo directamente sin verificar
            logger.info("Iniciando creación de alertas de ejemplo...");
            
            // Crear alertas de ejemplo
            Alerta alerta1 = new Alerta();
            alerta1.setTitulo("Stock Crítico - Laptop HP Pavilion");
            alerta1.setDescripcion("El producto Laptop HP Pavilion tiene stock crítico (2 unidades restantes)");
            alerta1.setTipoAlerta(Alerta.TipoAlerta.STOCK_BAJO);
            alerta1.setNivelPrioridad(Alerta.NivelPrioridad.CRITICA);
            alerta1.setEstado(Alerta.EstadoAlerta.NO_LEIDA);
            alerta1.setEntidadRelacionada("PRODUCTO");
            alerta1.setIdEntidadRelacionada(1L);
            alerta1.setFechaCreacion(java.time.LocalDateTime.now());
            alertaRepository.save(alerta1);
            logger.info("Alerta 1 creada exitosamente");
            
            Alerta alerta2 = new Alerta();
            alerta2.setTitulo("Intento de Acceso No Autorizado");
            alerta2.setDescripcion("Se detectó un intento de acceso no autorizado desde IP: 192.168.1.100");
            alerta2.setTipoAlerta(Alerta.TipoAlerta.SEGURIDAD);
            alerta2.setNivelPrioridad(Alerta.NivelPrioridad.ALTA);
            alerta2.setEstado(Alerta.EstadoAlerta.NO_LEIDA);
            alerta2.setEntidadRelacionada("SISTEMA");
            alerta2.setFechaCreacion(java.time.LocalDateTime.now());
            alertaRepository.save(alerta2);
            logger.info("Alerta 2 creada exitosamente");
            
            Alerta alerta3 = new Alerta();
            alerta3.setTitulo("Backup Automático Fallido");
            alerta3.setDescripcion("El backup automático programado para las 2:00 AM ha fallado");
            alerta3.setTipoAlerta(Alerta.TipoAlerta.SISTEMA);
            alerta3.setNivelPrioridad(Alerta.NivelPrioridad.MEDIA);
            alerta3.setEstado(Alerta.EstadoAlerta.NO_LEIDA);
            alerta3.setEntidadRelacionada("SISTEMA");
            alerta3.setFechaCreacion(java.time.LocalDateTime.now());
            alertaRepository.save(alerta3);
            logger.info("Alerta 3 creada exitosamente");
            
            Alerta alerta4 = new Alerta();
            alerta4.setTitulo("Nuevo Usuario Registrado");
            alerta4.setDescripcion("Se ha registrado un nuevo usuario: María González");
            alerta4.setTipoAlerta(Alerta.TipoAlerta.USUARIO);
            alerta4.setNivelPrioridad(Alerta.NivelPrioridad.BAJA);
            alerta4.setEstado(Alerta.EstadoAlerta.NO_LEIDA);
            alerta4.setEntidadRelacionada("USUARIO");
            alerta4.setIdEntidadRelacionada(2L);
            alerta4.setFechaCreacion(java.time.LocalDateTime.now());
            alertaRepository.save(alerta4);
            logger.info("Alerta 4 creada exitosamente");
            
            Alerta alerta5 = new Alerta();
            alerta5.setTitulo("Mantenimiento Programado Completado");
            alerta5.setDescripcion("El mantenimiento programado del servidor se completó exitosamente");
            alerta5.setTipoAlerta(Alerta.TipoAlerta.SISTEMA);
            alerta5.setNivelPrioridad(Alerta.NivelPrioridad.BAJA);
            alerta5.setEstado(Alerta.EstadoAlerta.RESUELTA);
            alerta5.setEntidadRelacionada("SISTEMA");
            alerta5.setFechaCreacion(java.time.LocalDateTime.now().minusHours(2));
            alerta5.setFechaResuelta(java.time.LocalDateTime.now());
            alertaRepository.save(alerta5);
            logger.info("Alerta 5 creada exitosamente");
            
            logger.info("Alertas de ejemplo creadas exitosamente - Total: 5 alertas");
        } catch (Exception e) {
            logger.error("Error al crear alertas de ejemplo: {}", e.getMessage());
        }
    }
}
