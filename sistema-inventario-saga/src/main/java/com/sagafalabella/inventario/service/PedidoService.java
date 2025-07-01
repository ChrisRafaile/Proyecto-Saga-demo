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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sagafalabella.inventario.model.Pedido;
import com.sagafalabella.inventario.model.Pedido.EstadoPedido;
import com.sagafalabella.inventario.model.Pedido.TipoEntrega;
import com.sagafalabella.inventario.repository.PedidoRepository;

/**
 * Servicio para gestión de pedidos
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Service
@Transactional
public class PedidoService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ActividadService actividadService;

    /**
     * Obtener todos los pedidos con paginación
     */
    @Transactional(readOnly = true)
    public Page<Pedido> obtenerTodosPaginado(int page, int size, String sortBy, String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : 
                       Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Pedido> resultado = pedidoRepository.findAll(pageable);
            
            logger.info("Obtenidos {} pedidos (página {} de {})", 
                       resultado.getNumberOfElements(), page + 1, resultado.getTotalPages());
            
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener pedidos paginados", e);
            throw new RuntimeException("Error al obtener la lista de pedidos", e);
        }
    }

    /**
     * Buscar pedidos con filtros
     */
    @Transactional(readOnly = true)
    public Page<Pedido> buscarConFiltros(String numeroPedido, EstadoPedido estado, 
                                        TipoEntrega tipoEntrega, Long clienteId,
                                        LocalDateTime fechaDesde, LocalDateTime fechaHasta,
                                        int page, int size, String sortBy, String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : 
                       Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Pedido> resultado = pedidoRepository.findWithFilters(
                numeroPedido, estado, tipoEntrega, clienteId, 
                fechaDesde, fechaHasta, pageable);
            
            logger.info("Búsqueda con filtros encontró {} pedidos", resultado.getTotalElements());
            
            return resultado;
        } catch (Exception e) {
            logger.error("Error al buscar pedidos con filtros", e);
            throw new RuntimeException("Error en la búsqueda de pedidos", e);
        }
    }

    /**
     * Obtener pedido por ID
     */
    @Transactional(readOnly = true)
    public Optional<Pedido> obtenerPorId(Long id) {
        try {
            return pedidoRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error al obtener pedido con ID: {}", id, e);
            throw new RuntimeException("Error al obtener el pedido", e);
        }
    }

    /**
     * Buscar pedido por número
     */
    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorNumeroPedido(String numeroPedido) {
        try {
            return pedidoRepository.findByNumeroPedido(numeroPedido);
        } catch (Exception e) {
            logger.error("Error al buscar pedido por número: {}", numeroPedido, e);
            throw new RuntimeException("Error al buscar el pedido", e);
        }
    }

    /**
     * Guardar pedido
     */
    public Pedido guardar(Pedido pedido) {
        try {
            boolean esNuevo = pedido.getId() == null;
            
            // Generar número de pedido si es nuevo
            if (esNuevo && (pedido.getNumeroPedido() == null || pedido.getNumeroPedido().isEmpty())) {
                pedido.setNumeroPedido(generarNumeroPedido());
            }
            
            // Validar número de pedido único
            if (esNuevo && pedidoRepository.existsByNumeroPedido(pedido.getNumeroPedido())) {
                throw new RuntimeException("Ya existe un pedido con el número: " + pedido.getNumeroPedido());
            }
            
            Pedido resultado = pedidoRepository.save(pedido);
            
            // Registrar actividad
            String accion = esNuevo ? "CREAR_PEDIDO" : "ACTUALIZAR_PEDIDO";
            actividadService.registrarActividad(accion, 
                "Pedido " + (esNuevo ? "creado" : "actualizado") + ": " + resultado.getNumeroPedido(),
                "Pedido", resultado.getId());
            
            logger.info("Pedido {} guardado: {}", esNuevo ? "creado" : "actualizado", resultado.getNumeroPedido());
            
            return resultado;
        } catch (Exception e) {
            logger.error("Error al guardar pedido", e);
            throw new RuntimeException("Error al guardar el pedido: " + e.getMessage(), e);
        }
    }

    /**
     * Eliminar pedido
     */
    public void eliminar(Long id) {
        try {
            Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
            if (pedidoOpt.isEmpty()) {
                throw new RuntimeException("Pedido no encontrado con ID: " + id);
            }
            
            Pedido pedido = pedidoOpt.get();
            
            // Validar que se puede eliminar
            if (pedido.getEstado() == EstadoPedido.EN_TRANSITO || 
                pedido.getEstado() == EstadoPedido.ENTREGADO) {
                throw new RuntimeException("No se puede eliminar un pedido en tránsito o entregado");
            }
            
            pedidoRepository.deleteById(id);
            
            // Registrar actividad
            actividadService.registrarActividad("ELIMINAR_PEDIDO", 
                "Pedido eliminado: " + pedido.getNumeroPedido(),
                "Pedido", id);
            
            logger.info("Pedido eliminado: {}", pedido.getNumeroPedido());
            
        } catch (Exception e) {
            logger.error("Error al eliminar pedido con ID: {}", id, e);
            throw new RuntimeException("Error al eliminar el pedido: " + e.getMessage(), e);
        }
    }

    /**
     * Cambiar estado del pedido
     */
    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        try {
            Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
            if (pedidoOpt.isEmpty()) {
                throw new RuntimeException("Pedido no encontrado con ID: " + id);
            }
            
            Pedido pedido = pedidoOpt.get();
            EstadoPedido estadoAnterior = pedido.getEstado();
            
            // Validar transición de estado
            validarTransicionEstado(estadoAnterior, nuevoEstado);
            
            pedido.setEstado(nuevoEstado);
            
            // Actualizar fecha de entrega si se marca como entregado
            if (nuevoEstado == EstadoPedido.ENTREGADO && pedido.getFechaEntregaReal() == null) {
                pedido.setFechaEntregaReal(LocalDateTime.now());
            }
            
            Pedido resultado = pedidoRepository.save(pedido);
            
            // Registrar actividad
            actividadService.registrarActividad("CAMBIAR_ESTADO_PEDIDO", 
                String.format("Estado cambiado de %s a %s para pedido: %s", 
                             estadoAnterior, nuevoEstado, pedido.getNumeroPedido()),
                "Pedido", id);
            
            logger.info("Estado del pedido {} cambiado de {} a {}", 
                       pedido.getNumeroPedido(), estadoAnterior, nuevoEstado);
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("Error al cambiar estado del pedido con ID: {}", id, e);
            throw new RuntimeException("Error al cambiar el estado del pedido: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener estadísticas de pedidos
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        try {
            List<Object[]> estadosCounts = pedidoRepository.countByEstado();
            Map<EstadoPedido, Long> estadisticasEstados = estadosCounts.stream()
                .collect(Collectors.toMap(
                    arr -> (EstadoPedido) arr[0],
                    arr -> (Long) arr[1]
                ));
            
            long totalPedidos = pedidoRepository.count();
            
            List<Object[]> estadisticasMes = pedidoRepository.getEstadisticasPorMes(
                LocalDateTime.now().minusMonths(12));
            
            return Map.of(
                "totalPedidos", totalPedidos,
                "porEstado", estadisticasEstados,
                "porMes", estadisticasMes
            );
            
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de pedidos", e);
            throw new RuntimeException("Error al obtener estadísticas", e);
        }
    }

    /**
     * Obtener pedidos por cliente
     */
    @Transactional(readOnly = true)
    public Page<Pedido> obtenerPorCliente(Long clienteId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPedido").descending());
            return pedidoRepository.findByClienteIdWithPagination(clienteId, pageable);
        } catch (Exception e) {
            logger.error("Error al obtener pedidos del cliente: {}", clienteId, e);
            throw new RuntimeException("Error al obtener pedidos del cliente", e);
        }
    }

    /**
     * Buscar pedidos por estado
     */
    @Transactional(readOnly = true)
    public List<Pedido> buscarPorEstado(EstadoPedido estado) {
        try {
            return pedidoRepository.findByEstado(estado);
        } catch (Exception e) {
            logger.error("Error al buscar pedidos por estado: {}", estado, e);
            throw new RuntimeException("Error al buscar pedidos por estado", e);
        }
    }

    /**
     * Generar número de pedido único
     */
    private String generarNumeroPedido() {
        String fecha = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        long count = pedidoRepository.count() + 1;
        return String.format("PED-%s-%06d", fecha, count);
    }

    /**
     * Validar transición de estado
     */
    private void validarTransicionEstado(EstadoPedido estadoActual, EstadoPedido nuevoEstado) {
        if (estadoActual == EstadoPedido.CANCELADO || estadoActual == EstadoPedido.ENTREGADO) {
            throw new RuntimeException("No se puede cambiar el estado de un pedido cancelado o entregado");
        }
        
        if (nuevoEstado == EstadoPedido.PENDIENTE && estadoActual != EstadoPedido.PENDIENTE) {
            throw new RuntimeException("No se puede regresar a estado PENDIENTE");
        }
    }
}
