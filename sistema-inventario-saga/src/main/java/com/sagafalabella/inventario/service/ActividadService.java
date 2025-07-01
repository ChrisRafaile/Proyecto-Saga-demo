package com.sagafalabella.inventario.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sagafalabella.inventario.model.Actividad;
import com.sagafalabella.inventario.model.Actividad.NivelActividad;
import com.sagafalabella.inventario.model.Actividad.TipoActividad;
import com.sagafalabella.inventario.model.Usuario;
import com.sagafalabella.inventario.repository.ActividadRepository;
import com.sagafalabella.inventario.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Servicio para gestión de actividades del sistema
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Service
@Transactional
public class ActividadService {

    private static final Logger logger = LoggerFactory.getLogger(ActividadService.class);

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Registrar una nueva actividad en el sistema
     */
    public Actividad registrarActividad(String accion, String descripcion, String entidad, Long idEntidad) {
        try {
            Actividad actividad = new Actividad();
            actividad.setAccion(accion);
            actividad.setDescripcion(descripcion);
            actividad.setEntidad(entidad);
            actividad.setIdEntidad(idEntidad);
            actividad.setFechaActividad(LocalDateTime.now());
            
            // Obtener usuario actual
            Usuario usuario = obtenerUsuarioActual();
            if (usuario != null) {
                actividad.setUsuario(usuario);
            }
            
            // Determinar tipo de actividad basado en la acción
            actividad.setTipoActividad(determinarTipoActividad(accion));
            
            // Determinar nivel basado en la acción
            actividad.setNivel(determinarNivelActividad(accion));
            
            // Obtener información de la petición HTTP
            establecerInformacionRequest(actividad);
            
            // Establecer resultado por defecto
            actividad.setResultado("SUCCESS");
            
            Actividad resultado = actividadRepository.save(actividad);
            
            logger.debug("Actividad registrada: {} - {}", accion, descripcion);
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("Error al registrar actividad: {} - {}", accion, descripcion, e);
            // No lanzar excepción para evitar que falle la operación principal
            return null;
        }
    }

    /**
     * Registrar actividad con más detalles
     */
    public Actividad registrarActividadCompleta(String accion, String descripcion, String entidad, 
                                               Long idEntidad, TipoActividad tipo, NivelActividad nivel,
                                               String valoresAnteriores, String valoresNuevos) {
        try {
            Actividad actividad = new Actividad();
            actividad.setAccion(accion);
            actividad.setDescripcion(descripcion);
            actividad.setEntidad(entidad);
            actividad.setIdEntidad(idEntidad);
            actividad.setTipoActividad(tipo);
            actividad.setNivel(nivel);
            actividad.setValoresAnteriores(valoresAnteriores);
            actividad.setValoresNuevos(valoresNuevos);
            actividad.setFechaActividad(LocalDateTime.now());
            actividad.setResultado("SUCCESS");
            
            // Obtener usuario actual
            Usuario usuario = obtenerUsuarioActual();
            if (usuario != null) {
                actividad.setUsuario(usuario);
            }
            
            // Obtener información de la petición HTTP
            establecerInformacionRequest(actividad);
            
            return actividadRepository.save(actividad);
            
        } catch (Exception e) {
            logger.error("Error al registrar actividad completa", e);
            return null;
        }
    }

    /**
     * Registrar actividad de error
     */
    public Actividad registrarError(String accion, String descripcion, String mensajeError, 
                                   String entidad, Long idEntidad) {
        try {
            Actividad actividad = new Actividad();
            actividad.setAccion(accion);
            actividad.setDescripcion(descripcion);
            actividad.setEntidad(entidad);
            actividad.setIdEntidad(idEntidad);
            actividad.setMensajeError(mensajeError);
            actividad.setFechaActividad(LocalDateTime.now());            actividad.setTipoActividad(TipoActividad.SISTEMA);
            actividad.setNivel(NivelActividad.ERROR);
            actividad.setResultado("ERROR");
            
            // Obtener usuario actual
            Usuario usuario = obtenerUsuarioActual();
            if (usuario != null) {
                actividad.setUsuario(usuario);
            }
            
            // Obtener información de la petición HTTP
            establecerInformacionRequest(actividad);
            
            return actividadRepository.save(actividad);
            
        } catch (Exception e) {
            logger.error("Error al registrar actividad de error", e);
            return null;
        }
    }    /**
     * Obtener todas las actividades con paginación
     */
    @Transactional(readOnly = true)
    public Page<Actividad> obtenerTodosPaginado(int page, int size, String sortBy, String sortDir) {
        try {
            logger.info("=== OBTENIENDO ACTIVIDADES PAGINADAS ===");
            logger.info("Parametros - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : 
                       Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            logger.info("Pageable creado: {}", pageable);
            
            Page<Actividad> resultado = actividadRepository.findAll(pageable);
            logger.info("Resultado obtenido - Total elementos: {}, Total paginas: {}, Elementos en esta pagina: {}", 
                       resultado.getTotalElements(), resultado.getTotalPages(), resultado.getNumberOfElements());
            
            if (resultado.getContent().isEmpty()) {
                logger.warn("⚠️ NO SE ENCONTRARON ACTIVIDADES en la base de datos");
            } else {
                logger.info("✅ Se encontraron {} actividades", resultado.getContent().size());
                resultado.getContent().forEach(actividad -> 
                    logger.info("Actividad: ID={}, Accion={}, Descripcion={}", 
                               actividad.getIdActividad(), actividad.getAccion(), actividad.getDescripcion())
                );
            }
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("❌ Error al obtener actividades paginadas", e);
            throw new RuntimeException("Error al obtener la lista de actividades", e);
        }
    }

    /**
     * Buscar actividades con filtros
     */
    @Transactional(readOnly = true)
    public Page<Actividad> buscarConFiltros(Long usuarioId, String accion, String entidad,
                                           TipoActividad tipoActividad, NivelActividad nivel,
                                           String resultado, LocalDateTime fechaDesde, 
                                           LocalDateTime fechaHasta, int page, int size, 
                                           String sortBy, String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : 
                       Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            return actividadRepository.findWithFilters(usuarioId, accion, entidad, 
                                                      tipoActividad, nivel, resultado,
                                                      fechaDesde, fechaHasta, pageable);
            
        } catch (Exception e) {
            logger.error("Error al buscar actividades con filtros", e);
            throw new RuntimeException("Error en la búsqueda de actividades", e);
        }
    }

    /**
     * Obtener actividad por ID
     */
    @Transactional(readOnly = true)
    public Optional<Actividad> obtenerPorId(Long id) {
        try {
            return actividadRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error al obtener actividad con ID: {}", id, e);
            throw new RuntimeException("Error al obtener la actividad", e);
        }
    }

    /**
     * Obtener actividades recientes (últimas 24 horas)
     */
    @Transactional(readOnly = true)
    public List<Actividad> obtenerActividadesRecientes() {
        try {
            LocalDateTime fechaDesde = LocalDateTime.now().minusHours(24);
            return actividadRepository.findActividadesRecientes(fechaDesde);
        } catch (Exception e) {
            logger.error("Error al obtener actividades recientes", e);
            throw new RuntimeException("Error al obtener actividades recientes", e);
        }
    }

    /**
     * Obtener estadísticas de actividades
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        try {
            List<Object[]> tiposCount = actividadRepository.countByTipoActividad();
            Map<TipoActividad, Long> estadisticasTipos = tiposCount.stream()
                .collect(Collectors.toMap(
                    arr -> (TipoActividad) arr[0],
                    arr -> (Long) arr[1]
                ));
            
            List<Object[]> nivelesCount = actividadRepository.countByNivel();
            Map<NivelActividad, Long> estadisticasNiveles = nivelesCount.stream()
                .collect(Collectors.toMap(
                    arr -> (NivelActividad) arr[0],
                    arr -> (Long) arr[1]
                ));
            
            List<Object[]> entidadesCount = actividadRepository.countByEntidad();
            
            long totalActividades = actividadRepository.count();
            
            List<Object[]> estadisticasDia = actividadRepository.getEstadisticasPorDia(
                LocalDateTime.now().minusDays(30));
            
            return Map.of(
                "totalActividades", totalActividades,
                "porTipo", estadisticasTipos,
                "porNivel", estadisticasNiveles,
                "porEntidad", entidadesCount,
                "porDia", estadisticasDia
            );
            
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de actividades", e);
            throw new RuntimeException("Error al obtener estadísticas", e);
        }
    }

    /**
     * Limpiar actividades antiguas
     */
    public int limpiarActividadesAntiguas(int diasAntiguedad) {
        try {
            LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasAntiguedad);
            int eliminadas = actividadRepository.deleteActividadesAntiguas(fechaLimite);
            
            // Registrar la limpieza
            registrarActividad("LIMPIAR_ACTIVIDADES", 
                "Se eliminaron " + eliminadas + " actividades anteriores a " + fechaLimite,
                "Sistema", null);
            
            logger.info("Eliminadas {} actividades anteriores a {}", eliminadas, fechaLimite);
            
            return eliminadas;
            
        } catch (Exception e) {
            logger.error("Error al limpiar actividades antiguas", e);
            throw new RuntimeException("Error al limpiar actividades antiguas", e);
        }
    }

    /**
     * Obtener actividades de error
     */
    @Transactional(readOnly = true)
    public List<Actividad> obtenerActividadesError() {
        try {
            return actividadRepository.findActividadesError(NivelActividad.ERROR);
        } catch (Exception e) {
            logger.error("Error al obtener actividades de error", e);
            throw new RuntimeException("Error al obtener actividades de error", e);
        }
    }

    /**
     * Eliminar actividad
     */
    public void eliminar(Long id) {
        try {
            Optional<Actividad> actividadOpt = actividadRepository.findById(id);
            if (actividadOpt.isEmpty()) {
                throw new RuntimeException("Actividad no encontrada con ID: " + id);
            }
            
            actividadRepository.deleteById(id);
            
            logger.info("Actividad eliminada con ID: {}", id);
            
        } catch (Exception e) {
            logger.error("Error al eliminar actividad con ID: {}", id, e);
            throw new RuntimeException("Error al eliminar la actividad", e);
        }
    }

    /**
     * Contar total de actividades (método de debug)
     */
    @Transactional(readOnly = true)
    public long contarActividades() {
        try {
            logger.info("🔍 DEBUG: Contando actividades en la base de datos");
            long count = actividadRepository.count();
            logger.info("🔍 DEBUG: Total de actividades encontradas: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("❌ DEBUG: Error al contar actividades", e);
            throw new RuntimeException("Error al contar actividades", e);
        }
    }

    // Métodos privados

    private Usuario obtenerUsuarioActual() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                return usuarioRepository.findByUsernameAndActivoTrue(auth.getName()).orElse(null);
            }
        } catch (Exception e) {
            logger.debug("No se pudo obtener el usuario actual", e);
        }
        return null;
    }    private TipoActividad determinarTipoActividad(String accion) {
        if (accion.contains("LOGIN") || accion.contains("LOGOUT")) {
            return TipoActividad.AUTENTICACION;
        } else if (accion.contains("CREAR")) {
            return TipoActividad.CREATE;
        } else if (accion.contains("ACTUALIZAR")) {
            return TipoActividad.UPDATE;
        } else if (accion.contains("ELIMINAR")) {
            return TipoActividad.DELETE;
        } else if (accion.contains("EXPORTAR")) {
            return TipoActividad.EXPORT;
        } else if (accion.contains("IMPORTAR")) {
            return TipoActividad.IMPORT;
        } else if (accion.contains("BACKUP")) {
            return TipoActividad.BACKUP;
        } else if (accion.contains("RESTORE")) {
            return TipoActividad.RESTORE;
        } else if (accion.contains("CONFIG")) {
            return TipoActividad.CONFIGURACION;
        } else {
            return TipoActividad.SISTEMA;
        }
    }

    private NivelActividad determinarNivelActividad(String accion) {
        if (accion.contains("ERROR") || accion.contains("FALLO")) {
            return NivelActividad.ERROR;
        } else if (accion.contains("ELIMINAR") || accion.contains("BACKUP") || 
                  accion.contains("RESTORE") || accion.contains("CONFIG")) {
            return NivelActividad.WARNING;
        } else if (accion.contains("LOGIN") || accion.contains("LOGOUT")) {
            return NivelActividad.INFO;
        } else {
            return NivelActividad.DEBUG;
        }
    }

    private void establecerInformacionRequest(Actividad actividad) {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            if (attr != null) {
                HttpServletRequest request = attr.getRequest();
                
                // Obtener IP del cliente
                String ip = obtenerIpCliente(request);
                actividad.setDireccionIp(ip);
                
                // Obtener User-Agent
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null && userAgent.length() > 500) {
                    userAgent = userAgent.substring(0, 500);
                }
                actividad.setUserAgent(userAgent);
            }
        } catch (Exception e) {
            // No hacer nada, la información del request es opcional
            logger.debug("No se pudo obtener información del request", e);
        }
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Si hay múltiples IPs, tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
