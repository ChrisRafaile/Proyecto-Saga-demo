package com.sagafalabella.inventario.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sagafalabella.inventario.model.Actividad;
import com.sagafalabella.inventario.service.ActividadService;

/**
 * Controlador temporal para diagnóstico de actividades
 */
@RestController
@RequestMapping("/debug")
public class DebugActividadController {

    private static final Logger logger = LoggerFactory.getLogger(DebugActividadController.class);

    @Autowired
    private ActividadService actividadService;

    /**
     * Endpoint de debug para verificar actividades
     */
    @GetMapping("/actividades")
    public String debugActividades(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "15") int size,
                                   @RequestParam(defaultValue = "fechaActividad") String sortBy,
                                   @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.info("🔍 DEBUG: Iniciando diagnóstico de actividades");
        
        StringBuilder resultado = new StringBuilder();
        resultado.append("=== DIAGNÓSTICO DE ACTIVIDADES ===\n");
        
        try {
            // Llamar al servicio
            logger.info("🔍 DEBUG: Llamando al servicio de actividades");
            Page<Actividad> actividades = actividadService.obtenerTodosPaginado(page, size, sortBy, sortDir);
            
            resultado.append("✅ Servicio ejecutado exitosamente\n");
            resultado.append("📊 Total de elementos: ").append(actividades.getTotalElements()).append("\n");
            resultado.append("📄 Total de páginas: ").append(actividades.getTotalPages()).append("\n");
            resultado.append("📋 Elementos en esta página: ").append(actividades.getNumberOfElements()).append("\n");
            resultado.append("🔍 Página actual: ").append(actividades.getNumber()).append("\n");
            resultado.append("📐 Tamaño de página: ").append(actividades.getSize()).append("\n");
            
            if (actividades.hasContent()) {
                resultado.append("\n✅ ACTIVIDADES ENCONTRADAS:\n");
                int contador = 1;
                for (Actividad actividad : actividades.getContent()) {
                    resultado.append(contador++).append(". ")
                            .append("ID: ").append(actividad.getIdActividad())
                            .append(", Acción: ").append(actividad.getAccion())
                            .append(", Descripción: ").append(actividad.getDescripcion())
                            .append(", Fecha: ").append(actividad.getFechaActividad())
                            .append("\n");
                }
            } else {
                resultado.append("\n❌ NO SE ENCONTRARON ACTIVIDADES\n");
                resultado.append("⚠️ La página está vacía o no hay datos\n");
            }
            
        } catch (Exception e) {
            logger.error("❌ DEBUG: Error al obtener actividades", e);
            resultado.append("❌ ERROR: ").append(e.getMessage()).append("\n");
            resultado.append("💣 Stack trace: ").append(e.toString()).append("\n");
        }
        
        resultado.append("\n=== FIN DEL DIAGNÓSTICO ===");
        
        logger.info("🔍 DEBUG: Diagnóstico completado");
        return resultado.toString();
    }

    /**
     * Endpoint para verificar la conexión directa al repositorio
     */
    @GetMapping("/actividades/count")
    public String countActividades() {
        try {
            logger.info("🔍 DEBUG: Contando actividades directamente");
            
            // Este método debería existir en el repositorio
            long count = actividadService.contarActividades();
            
            return "Total de actividades en la base de datos: " + count;
            
        } catch (Exception e) {
            logger.error("❌ DEBUG: Error al contar actividades", e);
            return "Error al contar actividades: " + e.getMessage();
        }
    }
}
