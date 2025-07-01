package com.sagafalabella.inventario.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sagafalabella.inventario.model.Producto;
import com.sagafalabella.inventario.repository.ProductoRepository;

/**
 * Servicio para gestión de productos
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Service
@Transactional
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);

    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Buscar producto por ID
     */
    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    /**
     * Listar todos los productos activos
     */
    public List<Producto> listarProductosActivos() {
        return productoRepository.findByActivoTrue();
    }    /**
     * Contar productos activos
     */
    public long contarProductosActivos() {
        return productoRepository.contarProductosActivos();
    }

    /**
     * Contar productos con stock bajo
     */
    public long contarProductosStockBajo() {
        return productoRepository.findProductosConStockBajo().size();
    }

    /**
     * Buscar productos por nombre
     */
    public List<Producto> buscarPorNombre(String nombre) {
        // Usamos el método de búsqueda por texto que ya existe
        return productoRepository.buscarPorTexto(nombre, 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    /**
     * Guardar producto
     */
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }    /**
     * Eliminar producto (lógico)
     */
    public void eliminar(Long id) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isPresent()) {
            Producto p = producto.get();
            p.setActivo(false);
            productoRepository.save(p);
        }
    }

    /**
     * Listar productos con stock bajo
     */
    public List<Producto> listarProductosStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }
    
    // ==================== MÉTODOS CRUD PARA ADMINISTRACIÓN ====================
    
    /**
     * Obtener todos los productos del sistema
     */
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }
    
    /**
     * Guardar o actualizar producto (para administración)
     */
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }
    
    /**
     * Eliminar producto (soft delete para administración)
     */
    public void eliminarProducto(Long id) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isPresent()) {
            Producto p = producto.get();
            p.setActivo(false);
            productoRepository.save(p);
        }
    }
      /**
     * Activar/Desactivar producto
     */
    public void cambiarEstadoProducto(Long id, boolean activo) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isPresent()) {
            Producto p = producto.get();
            p.setActivo(activo);
            productoRepository.save(p);
        }
    }
    
    /**
     * Verificar si existe un producto por código
     */
    public boolean existePorCodigo(String codigo) {
        return productoRepository.findByCodigoProducto(codigo).isPresent();
    }
    
    /**
     * Buscar producto por código
     */
    public Optional<Producto> buscarPorCodigo(String codigo) {
        return productoRepository.findByCodigoProducto(codigo);
    }
    
    /**
     * Guardar múltiples productos (para carga masiva)
     */
    public List<Producto> guardarTodos(List<Producto> productos) {
        return productoRepository.saveAll(productos);
    }
    
    /**
     * Verifica si existe un producto con el código dado
     */
    public boolean existeCodigoProducto(String codigoProducto) {
        return productoRepository.existsByCodigoProducto(codigoProducto);
    }
      /**
     * Eliminar TODOS los productos (para limpieza completa)
     * USAR CON PRECAUCIÓN - Solo para testing/desarrollo
     */
    @Transactional
    public int eliminarTodosLosProductos() {
        try {
            // Obtener todos los productos
            List<Producto> todosLosProductos = productoRepository.findAll();
            int eliminados = todosLosProductos.size();
            
            System.out.println("🔍 Productos encontrados antes de eliminar: " + eliminados);
            
            // Eliminar todos los productos
            productoRepository.deleteAll();
            
            // Flush para asegurar que se ejecuten las operaciones
            productoRepository.flush();
            
            // Verificar que se eliminaron
            long productosRestantes = productoRepository.count();
            System.out.println("🔍 Productos restantes después de eliminar: " + productosRestantes);
            
            return eliminados;        } catch (Exception e) {
            logger.error("Error al eliminar productos: {}", e.getMessage(), e);
            throw e;
        }
    }
}
