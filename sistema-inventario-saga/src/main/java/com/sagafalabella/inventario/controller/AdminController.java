package com.sagafalabella.inventario.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itextpdf.text.DocumentException;
import com.sagafalabella.inventario.model.Actividad;
import com.sagafalabella.inventario.model.Alerta;
import com.sagafalabella.inventario.model.Pedido;
import com.sagafalabella.inventario.model.Producto;
import com.sagafalabella.inventario.model.Usuario;
import com.sagafalabella.inventario.service.ActividadService;
import com.sagafalabella.inventario.service.AlertaService;
import com.sagafalabella.inventario.service.CargaMasivaService;
import com.sagafalabella.inventario.service.ExportService;
import com.sagafalabella.inventario.service.ImagenService;
import com.sagafalabella.inventario.service.PedidoService;
import com.sagafalabella.inventario.service.ProductoService;
import com.sagafalabella.inventario.service.ProveedorService;
import com.sagafalabella.inventario.service.RespaldoService;
import com.sagafalabella.inventario.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * Controlador para los dashboards específicos de administrador
 * Portal de administración con funcionalidades completas
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private CargaMasivaService cargaMasivaService;
    
    @Autowired
    private ImagenService imagenService;    
    @Autowired
    private ProveedorService proveedorService;
    
    @Autowired
    private ExportService exportService;
    
    @Autowired
    private RespaldoService respaldoService;
    
    @Autowired
    private PedidoService pedidoService;
      @Autowired
    private ActividadService actividadService;
      @Autowired
    private AlertaService alertaService;
    
    @Autowired
    private com.sagafalabella.inventario.repository.AlertaRepository alertaRepository;
    
    /**
     * Dashboard principal de administración
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/auth/login";
        }
        
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
        
        if (usuarioOpt.isEmpty()) {
            return "redirect:/auth/login?error=user_not_found";
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Verificar que el usuario tenga permisos de administrador
        if (!esAdministrador(usuario)) {
            return "redirect:/auth/access-denied";
        }
        
        // Datos para el dashboard del administrador
        try {
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
            model.addAttribute("totalUsuarios", usuarioService.contarUsuariosActivos());
            model.addAttribute("totalProductos", productoService.contarProductosActivos());
            model.addAttribute("pedidosPendientes", 0); // Pendiente: PedidoService
            model.addAttribute("proveedoresActivos", 0); // Pendiente: ProveedorService
            model.addAttribute("alertasCount", 3);
            model.addAttribute("usuarioActual", usuario.getNombreCompleto());
        } catch (Exception e) {
            // Valores por defecto en caso de error
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("totalProductos", 0);
            model.addAttribute("pedidosPendientes", 0);
            model.addAttribute("proveedoresActivos", 0);
            model.addAttribute("alertasCount", 0);
            model.addAttribute("usuarioActual", "Usuario");
        }        return "admin/dashboard";
    }
    
    /**
     * Portal principal de administración (redirige a dashboard)
     * @deprecated Usar /admin/dashboard en su lugar
     */
    @Deprecated
    @GetMapping("/portal")
    public String portal() {
        return "redirect:/admin/dashboard";
    }
    
    // ==================== GESTIÓN DE USUARIOS ====================
    
    /**
     * Lista todos los usuarios del sistema
     */
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            model.addAttribute("usuarios", usuarioService.obtenerTodosLosUsuarios());
            model.addAttribute("totalUsuarios", usuarioService.contarUsuariosActivos());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar usuarios: " + e.getMessage());
            model.addAttribute("usuarios", java.util.Collections.emptyList());
        }
        
        return "admin/usuarios";
    }
    
    /**
     * Formulario para crear nuevo usuario
     */
    @GetMapping("/usuarios/nuevo")
    public String formularioNuevoUsuario(Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Usuario.RolUsuario.values());
        return "admin/usuario-form";
    }
    
    /**
     * Guardar nuevo usuario
     */
    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            usuarioService.guardarUsuario(usuario);
            return "redirect:/admin/usuarios?success=Usuario creado exitosamente";
        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar usuario: " + e.getMessage());
            model.addAttribute("usuario", usuario);
            model.addAttribute("roles", Usuario.RolUsuario.values());
            return "admin/usuario-form";
        }
    }
    
    /**
     * Editar usuario existente
     */
    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            Optional<Usuario> usuario = usuarioService.buscarPorId(id);
            if (usuario.isPresent()) {
                model.addAttribute("usuario", usuario.get());
                model.addAttribute("roles", Usuario.RolUsuario.values());
                return "admin/usuario-form";
            } else {
                return "redirect:/admin/usuarios?error=Usuario no encontrado";
            }
        } catch (Exception e) {
            return "redirect:/admin/usuarios?error=Error al cargar usuario";
        }
    }
    
    /**
     * Eliminar usuario
     */
    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            return "redirect:/admin/usuarios?success=Usuario eliminado exitosamente";
        } catch (Exception e) {
            return "redirect:/admin/usuarios?error=Error al eliminar usuario";
        }
    }
    
    /**
     * Activar usuario
     */
    @PostMapping("/usuarios/activar/{id}")
    public String activarUsuario(@PathVariable Long id) {
        try {
            usuarioService.activarUsuario(id);
            return "redirect:/admin/usuarios?success=Usuario activado exitosamente";
        } catch (Exception e) {
            return "redirect:/admin/usuarios?error=Error al activar usuario";
        }
    }
    
    // ==================== GESTIÓN DE PRODUCTOS ====================
    
    /**
     * Lista todos los productos
     */
    @GetMapping("/productos")
    public String listarProductos(Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            model.addAttribute("productos", productoService.obtenerTodosLosProductos());
            model.addAttribute("totalProductos", productoService.contarProductosActivos());
        } catch (org.springframework.dao.DataAccessException | IllegalArgumentException e) {
            model.addAttribute("error", "Error al cargar productos: " + e.getMessage());
            model.addAttribute("productos", java.util.Collections.emptyList());
        }
        
        return "admin/productos";
    }
    
    /**
     * Formulario para nuevo producto
     */
    @GetMapping("/productos/nuevo")
    public String formularioNuevoProducto(Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", cargaMasivaService.obtenerCategorias());
        return "admin/producto-form";
    }
      /**
     * Guardar producto con imagen
     */
    @PostMapping("/productos/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, 
                                @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
                                Model model, RedirectAttributes redirectAttributes) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        try {
            // Validaciones y valores seguros
            if (producto.getActivo() == null) producto.setActivo(true);
            if (producto.getStockActual() == null) producto.setStockActual(0);
            if (producto.getStockMinimo() == null) producto.setStockMinimo(1);
            if (producto.getPrecio() == null || producto.getPrecio().doubleValue() <= 0) producto.setPrecio(new java.math.BigDecimal("1.00"));
            if (producto.getNombre() == null || producto.getNombre().isBlank()) producto.setNombre("Producto Test");
            if (producto.getCodigoProducto() == null || producto.getCodigoProducto().isBlank()) producto.setCodigoProducto("TEST-" + System.currentTimeMillis());
            if (producto.getCategoria() == null || producto.getCategoria().isBlank()) producto.setCategoria("SinCategoria");
            if (producto.getMarca() == null) producto.setMarca("");
            if (producto.getUbicacionAlmacen() == null) producto.setUbicacionAlmacen("");
            // Forzar proveedor nulo si no se asigna desde el formulario
            if (producto.getProveedor() == null) {
                producto.setProveedor(null); // O asignar proveedor por defecto si lo deseas
            }
            // Procesar imagen si se proporciona
            if (imagenFile != null && !imagenFile.isEmpty()) {
                try {
                    String nombreArchivo = imagenService.guardarImagen(imagenFile);
                    producto.setImagenNombre(nombreArchivo);
                    producto.setImagenUrl(imagenService.getImagenUrl(nombreArchivo));
                } catch (IOException | IllegalArgumentException imgError) {
                    System.err.println("Error al procesar imagen: " + imgError.getMessage());
                }
            }
            // Guardar producto
            productoService.guardar(producto);
            redirectAttributes.addFlashAttribute("success", "Producto guardado exitosamente");
            return "redirect:/admin/productos";        } catch (Exception e) {
            logger.error("Error al guardar producto: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al guardar producto: " + e.getMessage());
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", cargaMasivaService.obtenerCategorias());
            return "admin/producto-form";
        }
    }
    
    /**
     * Editar producto
     */
    @GetMapping("/productos/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            Optional<Producto> producto = productoService.buscarPorId(id);
            if (producto.isPresent()) {
                model.addAttribute("producto", producto.get());
                model.addAttribute("categorias", cargaMasivaService.obtenerCategorias());
                return "admin/producto-form";
            } else {
                return "redirect:/admin/productos?error=Producto no encontrado";
            }
        } catch (Exception e) {
            return "redirect:/admin/productos?error=Error al cargar producto";
        }
    }
    
    /**
     * Eliminar producto
     */
    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        try {
            // Obtener producto para eliminar su imagen
            Optional<Producto> productoOpt = productoService.buscarPorId(id);
            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();
                if (producto.getImagenNombre() != null) {
                    imagenService.eliminarImagen(producto.getImagenNombre());
                }
            }
            
            productoService.eliminarProducto(id);
            return "redirect:/admin/productos?success=Producto eliminado exitosamente";
        } catch (Exception e) {
            return "redirect:/admin/productos?error=Error al eliminar producto";
        }
    }
    
    /**
     * Desactivar producto
     */
    @PostMapping("/productos/desactivar/{id}")
    public String desactivarProducto(@PathVariable Long id) {
        try {
            productoService.cambiarEstadoProducto(id, false);
            return "redirect:/admin/productos?success=Producto desactivado exitosamente";
        } catch (Exception e) {
            return "redirect:/admin/productos?error=Error al desactivar producto";
        }
    }
    
    /**
     * Activar producto
     */
    @PostMapping("/productos/activar/{id}")
    public String activarProducto(@PathVariable Long id) {
        try {
            productoService.cambiarEstadoProducto(id, true);
            return "redirect:/admin/productos?success=Producto activado exitosamente";        } catch (Exception e) {
            return "redirect:/admin/productos?error=Error al activar producto";
        }
    }
    
    // ==================== CARGA MASIVA DE PRODUCTOS ====================
    
    /**
     * Página de carga masiva de productos
     */
    @GetMapping("/carga-masiva")
    public String cargaMasiva(Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        model.addAttribute("categorias", cargaMasivaService.obtenerCategorias());
        return "admin/carga-masiva";
    }
    
    /**
     * Página de carga masiva (ruta alternativa para compatibilidad)
     */
    @GetMapping("/productos/carga-masiva")
    public String cargaMasivaAlternativa(Model model) {
        return cargaMasiva(model); // Redirige al método principal
    }
    
    /**
     * Carga automática de productos de ejemplo
     */
    @PostMapping("/productos/generar-ejemplos")
    public String generarProductosEjemplo(RedirectAttributes redirectAttributes) {
        try {
            int productosCreados = cargaMasivaService.cargarProductosAutomaticos();
            redirectAttributes.addFlashAttribute("success", 
                "Se crearon " + productosCreados + " productos de ejemplo exitosamente");        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al crear productos de ejemplo: " + e.getMessage());
        }
        
        return "redirect:/admin/productos";
    }
    
    /**
     * Subir imagen de producto independientemente
     */
    @PostMapping("/productos/{id}/imagen")
    @SuppressWarnings("UseSpecificCatch")
    public String subirImagenProducto(@PathVariable Long id, 
                                    @RequestParam("imagen") MultipartFile archivo,
                                    RedirectAttributes redirectAttributes) {
        try {
            Optional<Producto> productoOpt = productoService.buscarPorId(id);
            if (productoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/admin/productos";
            }
            
            Producto producto = productoOpt.get();
            
            // Eliminar imagen anterior si existe
            if (producto.getImagenNombre() != null) {
                imagenService.eliminarImagen(producto.getImagenNombre());
            }
            
            // Guardar nueva imagen
            String nombreArchivo = imagenService.guardarImagen(archivo);
            producto.setImagenNombre(nombreArchivo);
            producto.setImagenUrl(imagenService.getImagenUrl(nombreArchivo));
              productoService.guardar(producto);
            
            redirectAttributes.addFlashAttribute("success", "Imagen actualizada correctamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al subir imagen: " + e.getMessage());
        }
        
        return "redirect:/admin/productos/editar/" + id;
    }
    
    // ==================== REPORTES ====================
    
    /**
     * Dashboard de reportes
     */
    @GetMapping("/reportes")
    public String reportes(Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            model.addAttribute("stockBajo", productoService.contarProductosStockBajo());
            model.addAttribute("totalProductos", productoService.contarProductosActivos());
            model.addAttribute("totalUsuarios", usuarioService.contarUsuariosActivos());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar reportes");
        }
        
        return "admin/reportes";
    }
    
    // ==================== CONFIGURACIÓN ====================
    
    /**
     * Página de configuración del sistema
     */
    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        return "admin/configuracion";
    }
    
    // ==================== PROVEEDORES ====================
      /**
     * Gestión de proveedores
     */
    @GetMapping("/proveedores")
    public String proveedores(Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            model.addAttribute("proveedores", proveedorService.obtenerTodosLosProveedores());
            model.addAttribute("totalProveedores", proveedorService.contarTotalProveedores());
            model.addAttribute("proveedoresActivos", proveedorService.contarProveedoresActivos());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar proveedores: " + e.getMessage());
            model.addAttribute("proveedores", java.util.Collections.emptyList());
        }
        
        return "admin/proveedores";
    }
    
    /**
     * Formulario para crear nuevo proveedor
     */
    @GetMapping("/proveedores/nuevo")
    public String nuevoProveedor(Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        model.addAttribute("proveedor", new com.sagafalabella.inventario.model.Proveedor());
        return "admin/proveedores-nuevo";
    }
    
    /**
     * Procesar creación de nuevo proveedor
     */
    @PostMapping("/proveedores/nuevo")
    public String crearProveedor(@ModelAttribute com.sagafalabella.inventario.model.Proveedor proveedor, 
                                RedirectAttributes redirectAttributes) {
        try {
            proveedorService.guardarProveedor(proveedor);
            redirectAttributes.addFlashAttribute("success", "Proveedor creado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear proveedor: " + e.getMessage());
        }
        
        return "redirect:/admin/proveedores";
    }
    
    /**
     * Formulario para editar proveedor
     */
    @GetMapping("/proveedores/editar/{id}")
    public String editarProveedor(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            com.sagafalabella.inventario.model.Proveedor proveedor = proveedorService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
            model.addAttribute("proveedor", proveedor);
            return "admin/proveedores-editar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar proveedor: " + e.getMessage());
            return "redirect:/admin/proveedores";
        }
    }
    
    /**
     * Procesar actualización de proveedor
     */
    @PostMapping("/proveedores/editar/{id}")
    public String actualizarProveedor(@PathVariable Long id, 
                                     @ModelAttribute com.sagafalabella.inventario.model.Proveedor proveedor,
                                     RedirectAttributes redirectAttributes) {
        try {
            proveedor.setId(id);
            proveedorService.actualizarProveedor(proveedor);
            redirectAttributes.addFlashAttribute("success", "Proveedor actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar proveedor: " + e.getMessage());
        }
        
        return "redirect:/admin/proveedores";
    }
    
    /**
     * Eliminar proveedor
     */
    @PostMapping("/proveedores/eliminar/{id}")
    public String eliminarProveedor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            proveedorService.eliminarProveedor(id);
            redirectAttributes.addFlashAttribute("success", "Proveedor eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar proveedor: " + e.getMessage());
        }
        
        return "redirect:/admin/proveedores";
    }
      // ==================== PEDIDOS ====================
    
    /**
     * Lista todos los pedidos con paginación y filtros
     */
    @GetMapping("/pedidos")
    public String pedidos(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(defaultValue = "fechaPedido") String sortBy,
                         @RequestParam(defaultValue = "desc") String sortDir,
                         @RequestParam(required = false) String numeroPedido,
                         @RequestParam(required = false) String estado,
                         @RequestParam(required = false) String tipoEntrega,
                         @RequestParam(required = false) String fechaDesde,
                         @RequestParam(required = false) String fechaHasta,
                         Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            // Convertir parámetros para filtros
            Pedido.EstadoPedido estadoPedido = null;
            if (estado != null && !estado.isEmpty()) {
                try {
                    estadoPedido = Pedido.EstadoPedido.valueOf(estado);
                } catch (IllegalArgumentException e) {
                    logger.warn("Estado de pedido inválido: {}", estado);
                }
            }
            
            Pedido.TipoEntrega tipoEntregaPedido = null;
            if (tipoEntrega != null && !tipoEntrega.isEmpty()) {
                try {
                    tipoEntregaPedido = Pedido.TipoEntrega.valueOf(tipoEntrega);
                } catch (IllegalArgumentException e) {
                    logger.warn("Tipo de entrega inválido: {}", tipoEntrega);
                }
            }
            
            LocalDateTime fechaDesdeParam = null;
            LocalDateTime fechaHastaParam = null;
            if (fechaDesde != null && !fechaDesde.isEmpty()) {
                fechaDesdeParam = LocalDateTime.parse(fechaDesde + "T00:00:00");
            }
            if (fechaHasta != null && !fechaHasta.isEmpty()) {
                fechaHastaParam = LocalDateTime.parse(fechaHasta + "T23:59:59");
            }
            
            Page<Pedido> pedidos;
            if (numeroPedido != null || estadoPedido != null || tipoEntregaPedido != null ||
                fechaDesdeParam != null || fechaHastaParam != null) {
                // Búsqueda con filtros
                pedidos = pedidoService.buscarConFiltros(numeroPedido, estadoPedido, 
                                                        tipoEntregaPedido, null,
                                                        fechaDesdeParam, fechaHastaParam,
                                                        page, size, sortBy, sortDir);
            } else {
                // Lista todos los pedidos
                pedidos = pedidoService.obtenerTodosPaginado(page, size, sortBy, sortDir);
            }
            
            model.addAttribute("pedidos", pedidos);
            model.addAttribute("estados", Pedido.EstadoPedido.values());
            model.addAttribute("tiposEntrega", Pedido.TipoEntrega.values());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", pedidos.getTotalPages());
            model.addAttribute("totalElements", pedidos.getTotalElements());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
            // Mantener filtros en la vista
            model.addAttribute("numeroPedido", numeroPedido);
            model.addAttribute("estado", estado);
            model.addAttribute("tipoEntrega", tipoEntrega);
            model.addAttribute("fechaDesde", fechaDesde);
            model.addAttribute("fechaHasta", fechaHasta);
            
        } catch (Exception e) {
            logger.error("Error al cargar pedidos", e);
            model.addAttribute("error", "Error al cargar pedidos: " + e.getMessage());
            model.addAttribute("pedidos", Page.empty());
        }
        
        return "admin/pedidos";
    }
    
    /**
     * Formulario para nuevo pedido
     */
    @GetMapping("/pedidos/nuevo")
    public String nuevoPedido(Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        model.addAttribute("pedido", new Pedido());
        model.addAttribute("estados", Pedido.EstadoPedido.values());
        model.addAttribute("tiposEntrega", Pedido.TipoEntrega.values());
        return "admin/pedidos-form";
    }
    
    /**
     * Guardar pedido (nuevo o editar)
     */
    @PostMapping("/pedidos/guardar")
    public String guardarPedido(@ModelAttribute Pedido pedido, 
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            pedidoService.guardar(pedido);
            redirectAttributes.addFlashAttribute("success", 
                "Pedido " + (pedido.getId() == null ? "creado" : "actualizado") + " exitosamente");
            return "redirect:/admin/pedidos";
        } catch (Exception e) {
            logger.error("Error al guardar pedido", e);
            model.addAttribute("error", "Error al guardar pedido: " + e.getMessage());
            model.addAttribute("pedido", pedido);
            model.addAttribute("estados", Pedido.EstadoPedido.values());
            model.addAttribute("tiposEntrega", Pedido.TipoEntrega.values());
            return "admin/pedidos-form";
        }
    }
    
    /**
     * Editar pedido
     */
    @GetMapping("/pedidos/editar/{id}")
    public String editarPedido(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            Optional<Pedido> pedidoOpt = pedidoService.obtenerPorId(id);
            if (pedidoOpt.isPresent()) {
                model.addAttribute("pedido", pedidoOpt.get());
                model.addAttribute("estados", Pedido.EstadoPedido.values());
                model.addAttribute("tiposEntrega", Pedido.TipoEntrega.values());
                return "admin/pedidos-form";
            } else {
                redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
                return "redirect:/admin/pedidos";
            }
        } catch (Exception e) {
            logger.error("Error al cargar pedido para editar", e);
            redirectAttributes.addFlashAttribute("error", "Error al cargar pedido: " + e.getMessage());
            return "redirect:/admin/pedidos";
        }
    }
    
    /**
     * Eliminar pedido
     */
    @PostMapping("/pedidos/eliminar/{id}")
    public String eliminarPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Pedido eliminado exitosamente");
        } catch (Exception e) {
            logger.error("Error al eliminar pedido", e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar pedido: " + e.getMessage());
        }
        return "redirect:/admin/pedidos";
    }
    
    /**
     * Cambiar estado del pedido
     */
    @PostMapping("/pedidos/cambiar-estado/{id}")
    public String cambiarEstadoPedido(@PathVariable Long id, 
                                     @RequestParam("nuevoEstado") String nuevoEstado,
                                     RedirectAttributes redirectAttributes) {
        try {
            Pedido.EstadoPedido estado = Pedido.EstadoPedido.valueOf(nuevoEstado);
            pedidoService.cambiarEstado(id, estado);
            redirectAttributes.addFlashAttribute("success", "Estado del pedido actualizado exitosamente");
        } catch (Exception e) {
            logger.error("Error al cambiar estado del pedido", e);
            redirectAttributes.addFlashAttribute("error", "Error al cambiar estado: " + e.getMessage());
        }
        return "redirect:/admin/pedidos";
    }
    
    /**
     * Exportar pedidos a Excel
     */
    @GetMapping("/pedidos/exportar/excel")
    public ResponseEntity<byte[]> exportarPedidosExcel() {
        try {
            byte[] excelData = exportService.exportarPedidosExcel();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "pedidos.xlsx");
            headers.setContentLength(excelData.length);
              return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
                    
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Error al exportar pedidos a Excel", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Exportar pedidos a PDF
     */
    @GetMapping("/pedidos/exportar/pdf")
    public ResponseEntity<byte[]> exportarPedidosPDF() {
        try {
            byte[] pdfData = exportService.exportarPedidosPDF();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "pedidos.pdf");
            headers.setContentLength(pdfData.length);
              return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);
                      } catch (IOException | IllegalArgumentException | DocumentException e) {
            logger.error("Error al exportar pedidos a PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }
      // ==================== ALERTAS ====================
    
    /**
     * Ver todas las alertas del sistema
     */
    @GetMapping("/alertas")
    public String alertas(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "15") int size,
                         @RequestParam(defaultValue = "fechaCreacion") String sortBy,
                         @RequestParam(defaultValue = "desc") String sortDir,
                         @RequestParam(required = false) String tipoAlerta,
                         @RequestParam(required = false) String estado,
                         @RequestParam(required = false) String nivelPrioridad,
                         @RequestParam(required = false) String fechaDesde,
                         @RequestParam(required = false) String fechaHasta,
                         Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            // Log de parámetros recibidos con más detalle
            logger.info("=== FILTROS RECIBIDOS (DETALLADO) ===");
            logger.info("tipoAlerta: '{}' (length: {})", tipoAlerta, tipoAlerta != null ? tipoAlerta.length() : "null");
            logger.info("estado: '{}' (length: {})", estado, estado != null ? estado.length() : "null");
            logger.info("nivelPrioridad: '{}' (length: {})", nivelPrioridad, nivelPrioridad != null ? nivelPrioridad.length() : "null");
            logger.info("fechaDesde: '{}'", fechaDesde);
            logger.info("fechaHasta: '{}'", fechaHasta);
            logger.info("Valores enum disponibles:");
            logger.info("- TipoAlerta: {}", java.util.Arrays.toString(com.sagafalabella.inventario.model.Alerta.TipoAlerta.values()));
            logger.info("- EstadoAlerta: {}", java.util.Arrays.toString(com.sagafalabella.inventario.model.Alerta.EstadoAlerta.values()));
            logger.info("- NivelPrioridad: {}", java.util.Arrays.toString(com.sagafalabella.inventario.model.Alerta.NivelPrioridad.values()));
            
            // Convertir parámetros de filtro con mejor manejo
            com.sagafalabella.inventario.model.Alerta.TipoAlerta tipoEnum = null;
            if (tipoAlerta != null && !tipoAlerta.trim().isEmpty() && !tipoAlerta.equals("todos")) {
                try {
                    // Usar exactamente el valor que viene del formulario (sin convertir a uppercase)
                    tipoEnum = com.sagafalabella.inventario.model.Alerta.TipoAlerta.valueOf(tipoAlerta);
                    logger.info("Tipo enum convertido exitosamente: {}", tipoEnum);
                } catch (IllegalArgumentException e) {
                    logger.warn("Tipo de alerta inválido: '{}', valores válidos: {}", tipoAlerta, 
                               java.util.Arrays.toString(com.sagafalabella.inventario.model.Alerta.TipoAlerta.values()));
                    tipoEnum = null; // Asegurar que quede null si es inválido
                }
            } else {
                logger.info("Tipo de alerta no especificado, vacío o 'todos', usando todos");
            }
            
            com.sagafalabella.inventario.model.Alerta.EstadoAlerta estadoEnum = null;
            if (estado != null && !estado.trim().isEmpty() && !estado.equals("todos")) {
                try {
                    // Usar exactamente el valor que viene del formulario
                    estadoEnum = com.sagafalabella.inventario.model.Alerta.EstadoAlerta.valueOf(estado);
                    logger.info("Estado enum convertido exitosamente: {}", estadoEnum);
                } catch (IllegalArgumentException e) {
                    logger.warn("Estado de alerta inválido: '{}', valores válidos: {}", estado, 
                               java.util.Arrays.toString(com.sagafalabella.inventario.model.Alerta.EstadoAlerta.values()));
                    estadoEnum = null; // Asegurar que quede null si es inválido
                }
            } else {
                logger.info("Estado no especificado, vacío o 'todos', usando todos");
            }
            
            com.sagafalabella.inventario.model.Alerta.NivelPrioridad nivelEnum = null;
            if (nivelPrioridad != null && !nivelPrioridad.trim().isEmpty() && !nivelPrioridad.equals("todos")) {
                try {
                    // Usar exactamente el valor que viene del formulario
                    nivelEnum = com.sagafalabella.inventario.model.Alerta.NivelPrioridad.valueOf(nivelPrioridad);
                    logger.info("Prioridad enum convertida exitosamente: {}", nivelEnum);
                } catch (IllegalArgumentException e) {
                    logger.warn("Nivel de prioridad inválido: '{}', valores válidos: {}", nivelPrioridad, 
                               java.util.Arrays.toString(com.sagafalabella.inventario.model.Alerta.NivelPrioridad.values()));
                    nivelEnum = null; // Asegurar que quede null si es inválido
                }
            } else {
                logger.info("Nivel de prioridad no especificado, vacío o 'todos', usando todos");
            }
            
            // Convertir fechas
            java.time.LocalDateTime fechaInicio = null;
            java.time.LocalDateTime fechaFin = null;
            
            if (fechaDesde != null && !fechaDesde.isEmpty()) {
                try {
                    fechaInicio = java.time.LocalDate.parse(fechaDesde).atStartOfDay();
                } catch (Exception e) {
                    logger.warn("Fecha desde inválida: {}", fechaDesde);
                }
            }
            
            if (fechaHasta != null && !fechaHasta.isEmpty()) {
                try {
                    fechaFin = java.time.LocalDate.parse(fechaHasta).atTime(23, 59, 59);
                } catch (Exception e) {
                    logger.warn("Fecha hasta inválida: {}", fechaHasta);
                }
            }
            
            // Obtener alertas con filtros
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size, 
                    sortDir.equalsIgnoreCase("desc") ? 
                    org.springframework.data.domain.Sort.by(sortBy).descending() : 
                    org.springframework.data.domain.Sort.by(sortBy).ascending());
            
            org.springframework.data.domain.Page<com.sagafalabella.inventario.model.Alerta> alertasPage = 
                alertaService.buscarConFiltros(tipoEnum, estadoEnum, nivelEnum, fechaInicio, fechaFin, pageable);
            
            // Log para diagnóstico
            logger.info("=== DEBUG ALERTAS ===");
            logger.info("Filtros aplicados - Tipo: {}, Estado: {}, Prioridad: {}", tipoEnum, estadoEnum, nivelEnum);
            logger.info("Página solicitada: {}, Tamaño: {}", page, size);
            logger.info("Alertas encontradas: {} total, {} en esta página", 
                       alertasPage.getTotalElements(), alertasPage.getContent().size());
            logger.info("Contenido vacío: {}", alertasPage.isEmpty());
            
            // Obtener estadísticas
            com.sagafalabella.inventario.service.AlertaService.AlertaEstadisticas estadisticas = 
                alertaService.obtenerEstadisticas();
            
            // Obtener alertas recientes para la lista
            java.util.List<com.sagafalabella.inventario.model.Alerta> alertasRecientes = 
                alertaService.obtenerAlertasRecientes();
            
            // Agregar datos al modelo
            model.addAttribute("alertas", alertasPage);
            model.addAttribute("alertasRecientes", alertasRecientes);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("tiposAlerta", com.sagafalabella.inventario.model.Alerta.TipoAlerta.values());
            model.addAttribute("estadosAlerta", com.sagafalabella.inventario.model.Alerta.EstadoAlerta.values());
            model.addAttribute("nivelesPrioridad", com.sagafalabella.inventario.model.Alerta.NivelPrioridad.values());
            
            // Parámetros de filtro para mantener en el formulario
            model.addAttribute("tipoAlertaSelected", tipoAlerta);
            model.addAttribute("estadoSelected", estado);
            model.addAttribute("nivelPrioridadSelected", nivelPrioridad);
            model.addAttribute("fechaDesde", fechaDesde);
            model.addAttribute("fechaHasta", fechaHasta);
            
            // Parámetros de paginación
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
        } catch (Exception e) {
            logger.error("Error al cargar alertas", e);
            model.addAttribute("error", "Error al cargar las alertas: " + e.getMessage());
            model.addAttribute("alertas", org.springframework.data.domain.Page.empty());
            model.addAttribute("estadisticas", new com.sagafalabella.inventario.service.AlertaService.AlertaEstadisticas());
        }
        
        return "admin/alertas";
    }
    
    /**
     * Formulario para nueva alerta
     */
    @GetMapping("/alertas/nueva")
    public String nuevaAlerta(Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        model.addAttribute("alerta", new com.sagafalabella.inventario.model.Alerta());
        return "admin/alerta-form";
    }
    
    /**
     * Guardar nueva alerta
     */
    @PostMapping("/alertas/nueva")
    public String guardarNuevaAlerta(@Valid @ModelAttribute com.sagafalabella.inventario.model.Alerta alerta,
                                    org.springframework.validation.BindingResult result,
                                    Model model,
                                    org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            if (result.hasErrors()) {
                model.addAttribute("alerta", alerta);
                return "admin/alerta-form";
            }
            
            alertaService.crearAlerta(alerta);
            redirectAttributes.addFlashAttribute("success", "Alerta creada exitosamente");
            return "redirect:/admin/alertas";
        } catch (Exception e) {
            logger.error("Error al guardar alerta", e);
            model.addAttribute("error", "Error al crear la alerta: " + e.getMessage());
            model.addAttribute("alerta", alerta);
            return "admin/alerta-form";
        }
    }
    
    /**
     * Formulario para editar alerta
     */
    @GetMapping("/alertas/editar/{id}")
    public String editarAlerta(@PathVariable Long id, Model model, 
                              org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            java.util.Optional<com.sagafalabella.inventario.model.Alerta> alertaOpt = alertaService.obtenerPorId(id);
            if (alertaOpt.isPresent()) {
                model.addAttribute("alerta", alertaOpt.get());
                return "admin/alerta-form";
            } else {
                redirectAttributes.addFlashAttribute("error", "Alerta no encontrada");
                return "redirect:/admin/alertas";
            }
        } catch (Exception e) {
            logger.error("Error al cargar alerta para editar", e);
            redirectAttributes.addFlashAttribute("error", "Error al cargar la alerta: " + e.getMessage());
            return "redirect:/admin/alertas";
        }
    }
    
    /**
     * Actualizar alerta editada
     */
    @PostMapping("/alertas/editar/{id}")
    public String actualizarAlerta(@PathVariable Long id,
                                  @Valid @ModelAttribute com.sagafalabella.inventario.model.Alerta alerta,
                                  org.springframework.validation.BindingResult result,
                                  Model model,
                                  org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            if (result.hasErrors()) {
                alerta.setIdAlerta(id);
                model.addAttribute("alerta", alerta);
                return "admin/alerta-form";
            }
            
            alerta.setIdAlerta(id);
            alertaService.actualizar(alerta);
            redirectAttributes.addFlashAttribute("success", "Alerta actualizada exitosamente");
            return "redirect:/admin/alertas";
        } catch (Exception e) {
            logger.error("Error al actualizar alerta", e);
            model.addAttribute("error", "Error al actualizar la alerta: " + e.getMessage());
            alerta.setIdAlerta(id);
            model.addAttribute("alerta", alerta);
            return "admin/alerta-form";
        }
    }
    
    /**
     * Eliminar alerta
     */
    @PostMapping("/alertas/eliminar/{id}")
    public String eliminarAlerta(@PathVariable Long id,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            alertaService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Alerta eliminada exitosamente");
        } catch (Exception e) {
            logger.error("Error al eliminar alerta", e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la alerta: " + e.getMessage());
        }
        return "redirect:/admin/alertas";
    }
    
    /**
     * Limpiar alertas antiguas
     */
    @PostMapping("/alertas/limpiar")
    public String limpiarAlertasAntiguas(@RequestParam(defaultValue = "30") int diasAntiguedad,
                                        org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            int eliminadas = alertaService.limpiarAlertasAntiguas(diasAntiguedad);
            redirectAttributes.addFlashAttribute("success", 
                "Se eliminaron " + eliminadas + " alertas resueltas anteriores a " + diasAntiguedad + " días");
        } catch (Exception e) {
            logger.error("Error al limpiar alertas antiguas", e);
            redirectAttributes.addFlashAttribute("error", "Error al limpiar alertas: " + e.getMessage());
        }
        return "redirect:/admin/alertas";
    }
    
    /**
     * API para obtener estadísticas de alertas (AJAX)
     */
    @GetMapping("/alertas/estadisticas")
    @ResponseBody
    public com.sagafalabella.inventario.service.AlertaService.AlertaEstadisticas obtenerEstadisticasAlertas() {
        try {
            return alertaService.obtenerEstadisticas();
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de alertas", e);
            return new com.sagafalabella.inventario.service.AlertaService.AlertaEstadisticas();
        }
    }
    
    /**
     * Marcar múltiples alertas como leídas
     */
    @PostMapping("/alertas/marcar-leidas")
    public String marcarAlertasComoLeidas(@RequestParam("alertaIds") List<Long> alertaIds,
                                         RedirectAttributes redirectAttributes) {
        if (!verificarAccesoAdmin(null)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            if (alertaIds == null || alertaIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se seleccionaron alertas para marcar como leídas");
                return "redirect:/admin/alertas";
            }
            
            alertaService.marcarVariasComoLeidas(alertaIds);
            redirectAttributes.addFlashAttribute("success", 
                "Se marcaron " + alertaIds.size() + " alerta(s) como leídas exitosamente");
            
            logger.info("Marcadas {} alertas como leídas por usuario {}", 
                       alertaIds.size(), SecurityContextHolder.getContext().getAuthentication().getName());
                       
        } catch (Exception e) {
            logger.error("Error al marcar alertas como leídas", e);
            redirectAttributes.addFlashAttribute("error", "Error al marcar las alertas como leídas: " + e.getMessage());
        }
        
        return "redirect:/admin/alertas";
    }
    
    /**
     * Marcar una alerta individual como leída
     */
    @PostMapping("/alertas/marcar-leida/{id}")
    public String marcarAlertaComoLeida(@PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
        if (!verificarAccesoAdmin(null)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            alertaService.marcarComoLeida(id);
            redirectAttributes.addFlashAttribute("success", "Alerta marcada como leída exitosamente");
            
            logger.info("Alerta {} marcada como leída por usuario {}", 
                       id, SecurityContextHolder.getContext().getAuthentication().getName());
                       
        } catch (Exception e) {
            logger.error("Error al marcar alerta {} como leída", id, e);
            redirectAttributes.addFlashAttribute("error", "Error al marcar la alerta como leída: " + e.getMessage());
        }
        
        return "redirect:/admin/alertas";
    }
    
    /**
     * Endpoint de diagnóstico para verificar alertas cargadas
     */
    @GetMapping("/alertas/debug")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugAlertas() {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            // Obtener todas las alertas sin filtros
            Pageable pageable = PageRequest.of(0, 10, Sort.by("fechaCreacion").descending());
            Page<Alerta> alertasPage = alertaService.buscarConFiltros(null, null, null, null, null, pageable);
            
            debug.put("totalAlertas", alertasPage.getTotalElements());
            debug.put("alertasEnPagina", alertasPage.getContent().size());
            debug.put("numeroPagina", alertasPage.getNumber());
            debug.put("totalPaginas", alertasPage.getTotalPages());
            
            // Obtener estadísticas
            var estadisticas = alertaService.obtenerEstadisticas();
            debug.put("estadisticas", Map.of(
                "alertasCriticas", estadisticas.getAlertasCriticas(),
                "alertasNoLeidas", estadisticas.getAlertasNoLeidas(),
                "alertasStockBajo", estadisticas.getAlertasStockBajo(),
                "totalAlertas", estadisticas.getTotalAlertas()
            ));
            
            // Información de las primeras alertas
            debug.put("primerasAlertas", alertasPage.getContent().stream()
                .limit(3)
                .map(alerta -> Map.of(
                    "id", alerta.getIdAlerta(),
                    "titulo", alerta.getTitulo(),
                    "estado", alerta.getEstado().name(),
                    "tipo", alerta.getTipoAlerta().name(),
                    "prioridad", alerta.getNivelPrioridad().name()
                ))
                .toList());
            
            debug.put("status", "success");
            
        } catch (Exception e) {
            logger.error("Error en debug de alertas", e);
            debug.put("status", "error");
            debug.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(debug);
    }
    
    /**
     * Endpoint super simple para verificar alertas directamente del repositorio
     */
    @GetMapping("/alertas/debug-simple")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugSimple() {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            // Prueba directa con el repositorio
            long totalCount = alertaRepository.count();
            debug.put("countDirecto", totalCount);
            
            // Intentar findAll sin paginación
            List<Alerta> todasAlertas = alertaRepository.findAll();
            debug.put("findAllCount", todasAlertas.size());
            
            if (!todasAlertas.isEmpty()) {
                Alerta primera = todasAlertas.get(0);
                debug.put("primeraAlerta", Map.of(
                    "id", primera.getIdAlerta(),
                    "titulo", primera.getTitulo(),
                    "fecha", primera.getFechaCreacion().toString()
                ));
            }
            
            // Prueba con findAll paginado
            Pageable pageableSimple = PageRequest.of(0, 5);
            Page<Alerta> pageSimple = alertaRepository.findAll(pageableSimple);
            debug.put("findAllPaginado", Map.of(
                "total", pageSimple.getTotalElements(),
                "contenido", pageSimple.getContent().size()
            ));
            
            debug.put("status", "success");
            
        } catch (Exception e) {
            logger.error("Error en debug simple", e);
            debug.put("status", "error");
            debug.put("error", e.getMessage());
            debug.put("stackTrace", java.util.Arrays.toString(e.getStackTrace()));
        }
        
        return ResponseEntity.ok(debug);
    }
    
    // ==================== ACTIVIDAD ====================
    
    /**
     * Lista todas las actividades del sistema con paginación y filtros
     */
    @GetMapping("/actividad")
    public String actividad(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "15") int size,
                           @RequestParam(defaultValue = "fechaActividad") String sortBy,
                           @RequestParam(defaultValue = "desc") String sortDir,
                           @RequestParam(required = false) String accion,
                           @RequestParam(required = false) String entidad,
                           @RequestParam(required = false) String tipoActividad,
                           @RequestParam(required = false) String nivel,
                           @RequestParam(required = false) String resultado,
                           @RequestParam(required = false) String fechaDesde,
                           @RequestParam(required = false) String fechaHasta,
                           @RequestParam(required = false) Long usuarioId,
                           Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
          try {
            logger.info("Accediendo a página de actividad - Usuario: {}, Filtros: tipoActividad={}, nivel={}", 
                       SecurityContextHolder.getContext().getAuthentication().getName(), tipoActividad, nivel);
            
            // Convertir parámetros para filtros
            Actividad.TipoActividad tipoActividadEnum = null;
            if (tipoActividad != null && !tipoActividad.isEmpty()) {
                try {
                    tipoActividadEnum = Actividad.TipoActividad.valueOf(tipoActividad);
                } catch (IllegalArgumentException e) {
                    logger.warn("Tipo de actividad inválido: {}", tipoActividad);
                }
            }
            
            Actividad.NivelActividad nivelEnum = null;
            if (nivel != null && !nivel.isEmpty()) {
                try {
                    nivelEnum = Actividad.NivelActividad.valueOf(nivel);
                } catch (IllegalArgumentException e) {
                    logger.warn("Nivel de actividad inválido: {}", nivel);
                }
            }
            
            LocalDateTime fechaDesdeParam = null;
            LocalDateTime fechaHastaParam = null;
            if (fechaDesde != null && !fechaDesde.isEmpty()) {
                fechaDesdeParam = LocalDateTime.parse(fechaDesde + "T00:00:00");
            }
            if (fechaHasta != null && !fechaHasta.isEmpty()) {
                fechaHastaParam = LocalDateTime.parse(fechaHasta + "T23:59:59");
            }
            
            Page<Actividad> actividades;
            if (accion != null || entidad != null || tipoActividadEnum != null || 
                nivelEnum != null || resultado != null || fechaDesdeParam != null || 
                fechaHastaParam != null || usuarioId != null) {
                // Búsqueda con filtros
                logger.info("Aplicando filtros para búsqueda de actividades");
                actividades = actividadService.buscarConFiltros(usuarioId, accion, entidad,
                                                               tipoActividadEnum, nivelEnum, 
                                                               resultado, fechaDesdeParam, 
                                                               fechaHastaParam, page, size, 
                                                               sortBy, sortDir);
            } else {
                // Lista todas las actividades
                logger.info("Obteniendo todas las actividades sin filtros");
                actividades = actividadService.obtenerTodosPaginado(page, size, sortBy, sortDir);
            }
            
            logger.info("Actividades obtenidas: {} elementos, {} páginas totales", 
                       actividades.getTotalElements(), actividades.getTotalPages());
              // Generar algunas actividades de ejemplo si no hay datos
            if (actividades.getTotalElements() == 0) {
                logger.info("No hay actividades, generando actividades de ejemplo");
                try {
                    // Registrar esta misma acción como actividad
                    actividadService.registrarActividad("CONSULTA_ACTIVIDADES", 
                                                       "Consulta de actividades del sistema", 
                                                       "Actividad", null);
                    
                    // Volver a obtener las actividades
                    actividades = actividadService.obtenerTodosPaginado(page, size, sortBy, sortDir);
                    logger.info("Actividades después de generar ejemplo: {}", actividades.getTotalElements());
                } catch (Exception e) {
                    logger.error("Error generando actividad de ejemplo", e);
                }
            }
            
            model.addAttribute("actividades", actividades);
            model.addAttribute("tiposActividad", Actividad.TipoActividad.values());
            model.addAttribute("nivelesActividad", Actividad.NivelActividad.values());
            model.addAttribute("resultados", new String[]{"SUCCESS", "ERROR", "WARNING"});
            model.addAttribute("usuarios", usuarioService.obtenerTodosLosUsuarios());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", actividades.getTotalPages());
            model.addAttribute("totalElements", actividades.getTotalElements());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
            // Mantener filtros en la vista
            model.addAttribute("accion", accion);
            model.addAttribute("entidad", entidad);
            model.addAttribute("tipoActividad", tipoActividad);
            model.addAttribute("nivel", nivel);
            model.addAttribute("resultado", resultado);
            model.addAttribute("fechaDesde", fechaDesde);
            model.addAttribute("fechaHasta", fechaHasta);
            model.addAttribute("usuarioId", usuarioId);
            
        } catch (Exception e) {
            logger.error("Error al cargar actividades", e);
            model.addAttribute("error", "Error al cargar actividades: " + e.getMessage());
            model.addAttribute("actividades", Page.empty());
        }
        
        return "admin/actividad";
    }
    
    /**
     * Ver detalle de una actividad
     */
    @GetMapping("/actividad/detalle/{id}")
    public String detalleActividad(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            Optional<Actividad> actividadOpt = actividadService.obtenerPorId(id);
            if (actividadOpt.isPresent()) {
                model.addAttribute("actividad", actividadOpt.get());
                return "admin/actividad-detalle";
            } else {
                redirectAttributes.addFlashAttribute("error", "Actividad no encontrada");
                return "redirect:/admin/actividad";
            }
        } catch (Exception e) {
            logger.error("Error al cargar detalle de actividad", e);
            redirectAttributes.addFlashAttribute("error", "Error al cargar actividad: " + e.getMessage());
            return "redirect:/admin/actividad";
        }
    }
    
    /**
     * Eliminar actividad
     */
    @PostMapping("/actividad/eliminar/{id}")
    public String eliminarActividad(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            actividadService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Actividad eliminada exitosamente");
        } catch (Exception e) {
            logger.error("Error al eliminar actividad", e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar actividad: " + e.getMessage());
        }
        return "redirect:/admin/actividad";
    }
    
    /**
     * Limpiar actividades antiguas
     */
    @PostMapping("/actividad/limpiar")
    public String limpiarActividades(@RequestParam(defaultValue = "30") int diasAntiguedad,
                                    RedirectAttributes redirectAttributes) {
        try {
            int eliminadas = actividadService.limpiarActividadesAntiguas(diasAntiguedad);
            redirectAttributes.addFlashAttribute("success", 
                "Se eliminaron " + eliminadas + " actividades anteriores a " + diasAntiguedad + " días");
        } catch (Exception e) {
            logger.error("Error al limpiar actividades", e);
            redirectAttributes.addFlashAttribute("error", "Error al limpiar actividades: " + e.getMessage());
        }
        return "redirect:/admin/actividad";
    }
    
    /**
     * Exportar log de actividades a Excel
     */
    @GetMapping("/actividad/exportar/excel")
    public ResponseEntity<byte[]> exportarActividadesExcel(@RequestParam(required = false) String accion,
                                                          @RequestParam(required = false) String entidad,
                                                          @RequestParam(required = false) String tipoActividad,
                                                          @RequestParam(required = false) String nivel,
                                                          @RequestParam(required = false) String fechaDesde,
                                                          @RequestParam(required = false) String fechaHasta) {
        try {
            byte[] excelData = exportService.exportarActividadesExcel();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "log_actividades.xlsx");
            headers.setContentLength(excelData.length);
              return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
                    
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Error al exportar log de actividades", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Exportar log de actividades a PDF
     */
    @GetMapping("/actividad/exportar/pdf")
    public ResponseEntity<byte[]> exportarActividadesPDF() {
        try {
            byte[] pdfData = exportService.exportarActividadesPDF();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "log_actividades.pdf");
            headers.setContentLength(pdfData.length);
              return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);
                      } catch (IOException | IllegalArgumentException | DocumentException e) {
            logger.error("Error al exportar log de actividades a PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener estadísticas de actividades (API REST)
     */
    @GetMapping("/actividad/estadisticas")
    @ResponseBody
    public Map<String, Object> obtenerEstadisticasActividades() {
        try {
            return actividadService.obtenerEstadisticas();
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de actividades", e);
            return Map.of("error", "Error al obtener estadísticas: " + e.getMessage());
        }
    }
    
    // ==================== RESPALDOS ====================
    
    /**
     * Gestión de respaldos del sistema
     */
    @GetMapping("/respaldos")
    public String respaldos(Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        model.addAttribute("respaldos", java.util.Collections.emptyList());
        model.addAttribute("mensaje", "Sistema de respaldos en desarrollo");
        return "admin/respaldos";
    }
    
    // ==================== PERFIL DE USUARIO ====================
    
    /**
     * Ver perfil del administrador
     */
    @GetMapping("/perfil")
    public String verPerfil(Model model) {
        try {
            // Verificación de autenticación más robusta
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                logger.warn("Usuario no autenticado intentando acceder al perfil");
                return "redirect:/auth/login";
            }
            
            String username = auth.getName();
            if (username == null || username.trim().isEmpty()) {
                logger.warn("Nombre de usuario vacío en autenticación");
                return "redirect:/auth/login";
            }
            
            logger.info("Cargando perfil para usuario: {}", username);
            
            // Búsqueda del usuario con manejo de excepciones mejorado
            Optional<Usuario> usuarioOpt;
            try {
                usuarioOpt = usuarioService.buscarPorUsername(username);
            } catch (Exception e) {
                logger.error("Error al buscar usuario en base de datos: {}", username, e);
                model.addAttribute("error", "Error al acceder a los datos del usuario");
                return "redirect:/admin/dashboard?error=db_error";
            }
            
            if (usuarioOpt.isEmpty()) {
                logger.warn("Usuario no encontrado en base de datos: {}", username);
                return "redirect:/auth/login?error=user_not_found";
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar permisos de administrador
            if (!esAdministrador(usuario)) {
                logger.warn("Usuario sin permisos de administrador: {}", username);
                return "redirect:/auth/access-denied";
            }
            
            // Forzar la carga completa de datos del usuario
            try {
                // Asegurar que todos los datos estén disponibles
                String nombreCompleto = usuario.getNombreCompleto();
                if (nombreCompleto == null || nombreCompleto.trim().isEmpty() || nombreCompleto.trim().equals(" ")) {
                    // Si nombre o apellido están vacíos, asignar valores por defecto
                    if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
                        usuario.setNombre("Administrador");
                    }
                    if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
                        usuario.setApellido("Principal");
                    }
                    nombreCompleto = usuario.getNombreCompleto();
                }
                
                if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
                    usuario.setEmail("admin@sagafalabella.com");
                }
                if (usuario.getTelefono() == null || usuario.getTelefono().trim().isEmpty()) {
                    usuario.setTelefono("No especificado");
                }
                
                // Agregar datos del usuario al modelo con valores garantizados
                model.addAttribute("usuario", usuario);
                model.addAttribute("nombreCompleto", nombreCompleto);
                model.addAttribute("username", usuario.getUsername());
                model.addAttribute("email", usuario.getEmail());
                model.addAttribute("telefono", usuario.getTelefono());
                model.addAttribute("nombre", usuario.getNombre());
                model.addAttribute("apellido", usuario.getApellido());
                model.addAttribute("rol", usuario.getRol() != null ? usuario.getRol().getDescripcion() : "ADMINISTRADOR");
                model.addAttribute("tipoUsuario", usuario.getTipoUsuario() != null ? usuario.getTipoUsuario().getDescripcion() : "Personal Interno");
                
                logger.info("Datos del usuario agregados al modelo: nombre={}, email={}", 
                    nombreCompleto, usuario.getEmail());
                
            } catch (Exception e) {
                logger.error("Error al procesar datos del usuario", e);
                // Agregar datos por defecto en caso de error
                model.addAttribute("usuario", usuario);
                model.addAttribute("nombreCompleto", "Administrador Principal");
                model.addAttribute("username", username);
                model.addAttribute("email", "admin@sagafalabella.com");
                model.addAttribute("telefono", "No especificado");
                model.addAttribute("nombre", "Administrador");
                model.addAttribute("apellido", "Principal");
                model.addAttribute("rol", "ADMINISTRADOR");
                model.addAttribute("tipoUsuario", "Personal Interno");
            }
            
            // Estadísticas del perfil con valores por defecto seguros
            try {
                java.time.LocalDateTime fechaActual = java.time.LocalDateTime.now();
                model.addAttribute("fechaUltimoAcceso", fechaActual.minusHours(2));
                model.addAttribute("fechaRegistro", fechaActual.minusDays(30));
                model.addAttribute("totalSesiones", 47);
                model.addAttribute("accionesRealizadas", 156);
            } catch (Exception e) {
                logger.warn("Error al cargar estadísticas del perfil", e);
                // Valores por defecto en caso de error
                java.time.LocalDateTime fechaActual = java.time.LocalDateTime.now();
                model.addAttribute("fechaUltimoAcceso", fechaActual);
                model.addAttribute("fechaRegistro", fechaActual);
                model.addAttribute("totalSesiones", 0);
                model.addAttribute("accionesRealizadas", 0);
            }
            
            // Asegurar que la vista no esté en modo edición
            model.addAttribute("editandoPerfil", false);
            model.addAttribute("vistaActual", "perfil");
            
            logger.info("Perfil cargado exitosamente para usuario: {} con datos completos", username);
            return "admin/perfil";
            
        } catch (Exception e) {
            logger.error("Error inesperado al cargar perfil", e);
            model.addAttribute("error", "Error al cargar el perfil. Por favor, inténtelo de nuevo.");
            return "redirect:/admin/dashboard";
        }
    }
    
    /**
     * Formulario para editar perfil del administrador
     */
    @GetMapping("/perfil/editar")
    public String editarPerfil(Model model) {
        try {
            if (!verificarAccesoAdmin(model)) {
                return "redirect:/auth/access-denied";
            }
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                model.addAttribute("usuario", usuario);
                model.addAttribute("editandoPerfil", true);
                model.addAttribute("vistaActual", "editar");
                model.addAttribute("nombreCompleto", usuario.getNombreCompleto() != null ? 
                    usuario.getNombreCompleto() : "Administrador Principal");
                
                logger.info("Formulario de edición de perfil cargado para usuario: {}", username);
            } else {
                logger.warn("Usuario no encontrado al intentar editar perfil: {}", username);
                return "redirect:/auth/login?error=user_not_found";
            }
            
            return "admin/perfil-form";
            
        } catch (Exception e) {
            logger.error("Error al cargar formulario de edición de perfil", e);
            return "redirect:/admin/perfil?error=load_error";
        }
    }
    
    /**
     * Guardar cambios del perfil
     */
    @PostMapping("/perfil/guardar")
    public String guardarPerfil(@ModelAttribute Usuario usuarioForm, 
                               @RequestParam(value = "passwordActual", required = false) String passwordActual,
                               @RequestParam(value = "passwordNuevo", required = false) String passwordNuevo,
                               @RequestParam(value = "confirmarPassword", required = false) String confirmarPassword,
                               Model model, RedirectAttributes redirectAttributes) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            
            if (usuarioOpt.isEmpty()) {
                return "redirect:/auth/login?error=user_not_found";
            }
            
            Usuario usuarioActual = usuarioOpt.get();
            
            // Actualizar información básica
            usuarioActual.setNombre(usuarioForm.getNombre());
            usuarioActual.setApellido(usuarioForm.getApellido());
            usuarioActual.setEmail(usuarioForm.getEmail());
            usuarioActual.setTelefono(usuarioForm.getTelefono());
              // Cambiar contraseña si se proporcionó
            if (passwordNuevo != null && !passwordNuevo.trim().isEmpty()) {
                if (passwordActual == null || passwordActual.trim().isEmpty()) {
                    model.addAttribute("error", "Debe proporcionar la contraseña actual para cambiarla");
                    model.addAttribute("usuario", usuarioForm);
                    return "admin/perfil-form";
                }
                
                if (!usuarioService.verificarPasswordActual(usuarioActual, passwordActual)) {
                    model.addAttribute("error", "La contraseña actual es incorrecta");
                    model.addAttribute("usuario", usuarioForm);
                    return "admin/perfil-form";
                }
                
                if (!passwordNuevo.equals(confirmarPassword)) {
                    model.addAttribute("error", "Las contraseñas nuevas no coinciden");
                    model.addAttribute("usuario", usuarioForm);
                    return "admin/perfil-form";
                }
                
                if (passwordNuevo.length() < 6) {
                    model.addAttribute("error", "La nueva contraseña debe tener al menos 6 caracteres");
                    model.addAttribute("usuario", usuarioForm);
                    return "admin/perfil-form";
                }
                
                usuarioService.cambiarPassword(usuarioActual, passwordNuevo);
            }
            
            usuarioService.guardarUsuario(usuarioActual);
            
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
            return "redirect:/admin/perfil";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar perfil: " + e.getMessage());
            model.addAttribute("usuario", usuarioForm);
            return "admin/perfil-form";
        }
    }
    
    // ==================== DEBUG TEMPORAL ====================
    
    /**
     * Endpoint temporal para debug - ver todos los productos en DB
     */
    @GetMapping("/productos/debug")
    public String debugProductos(Model model) {
        if (!verificarAccesoAdmin(model)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            List<Producto> todosLosProductos = productoService.obtenerTodosLosProductos();
            model.addAttribute("totalProductos", todosLosProductos.size());
            model.addAttribute("productos", todosLosProductos);
            
            // Información de debug
            model.addAttribute("debug", true);
            model.addAttribute("mensaje", "Total de productos en base de datos: " + todosLosProductos.size());
            
            System.out.println("=== DEBUG PRODUCTOS ===");
            System.out.println("Total productos en BD: " + todosLosProductos.size());
            for (Producto p : todosLosProductos) {
                System.out.println("- " + p.getCodigoProducto() + ": " + p.getNombre());
            }
            System.out.println("=====================");
            
        } catch (Exception e) {
            model.addAttribute("error", "Error en debug: " + e.getMessage());
            model.addAttribute("productos", java.util.Collections.emptyList());
        }
        
        return "admin/productos";
    }
    
    /**
     * Endpoint temporal para limpiar productos de ejemplo
     */    @PostMapping("/productos/limpiar-ejemplos")
    public String limpiarProductosEjemplo(RedirectAttributes redirectAttributes) {
        try {
            int eliminados = 0;
            
            // Códigos de productos de ejemplo
            String[] codigosEjemplo = {
                "SG-001", "IP-001", "HW-001", "HP-LAP-001", "LN-LAP-001", "AC-001",
                "RM-001", "RM-002", "RM-003", "RF-001", "RF-002", "RF-003",
                "HG-001", "HG-002", "HG-003", "HG-004", "DP-001", "DP-002", "DP-003",
                "BL-001", "BL-002", "BL-003"
            };
            
            for (String codigo : codigosEjemplo) {
                Optional<Producto> producto = productoService.buscarPorCodigo(codigo);
                if (producto.isPresent()) {
                    productoService.eliminarProducto(producto.get().getIdproducto());
                    eliminados++;
                    System.out.println("Eliminado producto ejemplo: " + codigo);
                }
            }
            
            redirectAttributes.addFlashAttribute("success", 
                "Se eliminaron " + eliminados + " productos de ejemplo");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al limpiar productos: " + e.getMessage());
        }
        
        return "redirect:/admin/productos/debug";
    }
    
    /**
     * Endpoint temporal para limpiar TODOS los productos
     * USAR CON PRECAUCIÓN - Solo para testing/desarrollo
     */
    @PostMapping("/productos/limpiar-todos")
    public String limpiarTodosLosProductos(RedirectAttributes redirectAttributes) {
        try {
            int eliminados = productoService.eliminarTodosLosProductos();
            
            redirectAttributes.addFlashAttribute("success", 
                "Se eliminaron " + eliminados + " productos exitosamente. Base de datos limpia.");
                
            System.out.println("LIMPIEZA COMPLETA: Se eliminaron " + eliminados + " productos");
            
        } catch (Exception e) {
            System.err.println("Error en limpieza completa: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", 
                "Error al limpiar todos los productos: " + e.getMessage());
        }
        
        return "redirect:/admin/productos";
    }    /**
     * Endpoint temporal GET para limpiar TODOS los productos (para testing)
     * USAR CON PRECAUCIÓN - Solo para testing/desarrollo
     */
    @GetMapping("/test/limpiar-todos")
    @ResponseBody
    public String limpiarTodosLosProductosTest() {
        try {
            int eliminados = productoService.eliminarTodosLosProductos();
            
            String mensaje = "LIMPIEZA COMPLETA EXITOSA: Se eliminaron " + eliminados + " productos";
            System.out.println(mensaje);
            
            return mensaje;
              } catch (Exception e) {
            String error = "Error en limpieza completa: " + e.getMessage();
            logger.error(error, e);
            return error;
        }
    }
    
    // ==================== FIN DEBUG TEMPORAL ====================
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Verifica acceso de administrador y agrega datos básicos al modelo
     */
    private boolean verificarAccesoAdmin(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return false;
        }
        
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
        
        if (usuarioOpt.isEmpty() || !esAdministrador(usuarioOpt.get())) {
            return false;
        }
        
        Usuario usuario = usuarioOpt.get();
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
        
        return true;
    }
    
    /**
     * Verifica si el usuario es administrador
     */
    private boolean esAdministrador(Usuario usuario) {
        return switch (usuario.getRol()) {
            case SUPER_ADMIN, ADMIN_INVENTARIO, ADMIN_VENTAS -> true;
            default -> false;
        };
    }

    /**
     * Carga masiva de productos desde archivo CSV
     */    @PostMapping("/productos/cargar-csv")
    public String cargarProductosCSV(@RequestParam("archivo") MultipartFile archivo,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (archivo.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Por favor seleccione un archivo CSV");
                return "redirect:/admin/carga-masiva";
            }
            
            String nombreArchivo = archivo.getOriginalFilename();
            if (nombreArchivo == null) {
                redirectAttributes.addFlashAttribute("error", "No se pudo obtener el nombre del archivo");
                return "redirect:/admin/carga-masiva";
            }
            
            if (!nombreArchivo.toLowerCase().endsWith(".csv")) {
                redirectAttributes.addFlashAttribute("error", "El archivo debe ser un CSV");
                return "redirect:/admin/carga-masiva";
            }
            
            int productosCreados = cargaMasivaService.cargarProductosDesdeCSV(archivo);
            redirectAttributes.addFlashAttribute("success", 
                "Se cargaron " + productosCreados + " productos exitosamente desde el archivo CSV");
                
        } catch (Exception e) {
            // Log del error para debugging
            System.err.println("Error al cargar CSV: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", 
                "Error al cargar el archivo CSV: " + e.getMessage());
        }
          return "redirect:/admin/carga-masiva";
    }
    
    // ==========================================
    // ENDPOINTS DE EXPORTACIÓN DE REPORTES
    // ==========================================
    
    /**
     * Exportar reporte de productos a PDF
     */
    @GetMapping("/reportes/exportar/pdf")
    public ResponseEntity<byte[]> exportarReportePDF() {
        try {
            byte[] pdfData = exportService.exportarProductosPDF();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte_productos.pdf");
            headers.setContentLength(pdfData.length);
              return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);
                    
        } catch (IOException | DocumentException e) {
            logger.error("Error al generar reporte PDF: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Exportar reporte de productos a Excel
     */
    @GetMapping("/reportes/exportar/excel")
    public ResponseEntity<byte[]> exportarReporteExcel() {
        try {
            byte[] excelData = exportService.exportarProductosExcel();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "reporte_productos.xlsx");
            headers.setContentLength(excelData.length);
              return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
                    
        } catch (IOException e) {
            logger.error("Error al generar reporte Excel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ==========================================
    // ENDPOINTS DE GESTIÓN DE RESPALDOS
    // ==========================================
    
    /**
     * Crear respaldo del sistema
     */
    @PostMapping("/respaldos/crear")
    public String crearRespaldo(RedirectAttributes redirectAttributes) {
        try {
            String nombreArchivo = respaldoService.crearRespaldo();
            redirectAttributes.addFlashAttribute("success", 
                "Respaldo creado exitosamente: " + nombreArchivo);        } catch (IOException | SQLException e) {
            logger.error("Error al crear respaldo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", 
                "Error al crear respaldo: " + e.getMessage());
        }
        return "redirect:/admin/respaldos";
    }
    
    /**
     * Restaurar respaldo desde archivo
     */
    @PostMapping("/respaldos/restaurar")
    public String restaurarRespaldo(@RequestParam("archivo") MultipartFile archivo,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (archivo.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Por favor seleccione un archivo de respaldo");
                return "redirect:/admin/respaldos";
            }
            
            respaldoService.restaurarRespaldo(archivo);
            redirectAttributes.addFlashAttribute("success", "Respaldo restaurado exitosamente");
              } catch (IOException | SQLException e) {
            logger.error("Error al restaurar respaldo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", 
                "Error al restaurar respaldo: " + e.getMessage());
        }
        return "redirect:/admin/respaldos";
    }
    
    /**
     * Descargar archivo de respaldo
     */
    @GetMapping("/respaldos/descargar/{nombreArchivo}")
    public ResponseEntity<Resource> descargarRespaldo(@PathVariable String nombreArchivo) {
        try {
            Resource archivo = respaldoService.descargarRespaldo(nombreArchivo);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(archivo);
                      } catch (IOException e) {
            logger.error("Error al descargar respaldo: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Verificar integridad del sistema
     */
    @PostMapping("/respaldos/verificar")
    @ResponseBody
    public RespaldoService.VerificacionResult verificarIntegridad() {
        try {
            return respaldoService.verificarIntegridad();        } catch (SQLException | RuntimeException e) {
            logger.error("Error durante verificación de integridad: {}", e.getMessage(), e);
            RespaldoService.VerificacionResult result = new RespaldoService.VerificacionResult();
            result.setConexionBD(false);
            result.getErrores().add("Error durante verificación: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * Ejecutar limpieza del sistema
     */
    @PostMapping("/respaldos/limpiar")
    @ResponseBody
    public RespaldoService.LimpiezaResult ejecutarLimpieza() {
        try {
            return respaldoService.ejecutarLimpieza();        } catch (IOException | SQLException | RuntimeException e) {
            logger.error("Error durante limpieza: {}", e.getMessage(), e);
            RespaldoService.LimpiezaResult result = new RespaldoService.LimpiezaResult();
            result.getErrores().add("Error durante limpieza: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * Guardar configuración de respaldos
     */
    @PostMapping("/respaldos/configuracion")
    public String guardarConfiguracion(@RequestParam("respaldoAutomatico") boolean respaldoAutomatico,
                                     @RequestParam("frecuencia") String frecuencia,
                                     @RequestParam("hora") String hora,
                                     @RequestParam("diasRetencion") int diasRetencion,
                                     RedirectAttributes redirectAttributes) {
        try {
            RespaldoService.ConfiguracionRespaldo config = new RespaldoService.ConfiguracionRespaldo();
            config.setRespaldoAutomatico(respaldoAutomatico);
            config.setFrecuencia(frecuencia);
            config.setHora(hora);
            config.setDiasRetencion(diasRetencion);
            
            respaldoService.guardarConfiguracion(config);
            redirectAttributes.addFlashAttribute("success", "Configuración guardada exitosamente");
              } catch (IOException | RuntimeException e) {
            logger.error("Error al guardar configuración: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", 
                "Error al guardar configuración: " + e.getMessage());
        }
        return "redirect:/admin/respaldos";
    }
    
    /**
     * Endpoint temporal para generar datos de actividad de prueba
     */
    @GetMapping("/actividad/generar-datos-prueba")
    public String generarDatosPrueba(RedirectAttributes redirectAttributes) {
        if (!verificarAccesoAdmin(null)) {
            return "redirect:/auth/access-denied";
        }
        
        try {
            logger.info("Generando datos de actividad de prueba");
            
            // Generar diferentes tipos de actividades

            actividadService.registrarActividad("LOGIN", "Inicio de sesión exitoso", "Usuario", 1L);
            actividadService.registrarActividad("CONSULTA_PRODUCTOS", "Consulta de lista de productos", "Producto", null);
            actividadService.registrarActividad("CREACION_PRODUCTO", "Creación de nuevo producto", "Producto", 123L);
            actividadService.registrarActividad("ACTUALIZACION_USUARIO", "Actualización de datos de usuario", "Usuario", 2L);
            actividadService.registrarActividad("EXPORTACION_DATOS", "Exportación de datos a Excel", "Reporte", null);
            actividadService.registrarActividad("CONFIGURACION_SISTEMA", "Modificación de configuración", "Sistema", null);
            actividadService.registrarActividad("ERROR_CONEXION", "Error de conexión a base de datos", "Sistema", null);
            actividadService.registrarActividad("RESPALDO_BD", "Respaldo automático de base de datos", "Sistema", null);
            
            redirectAttributes.addFlashAttribute("mensaje", "Datos de actividad de prueba generados exitosamente. Se crearon 8 actividades de ejemplo.");
            logger.info("Datos de actividad de prueba generados exitosamente");
            
        } catch (Exception e) {
            logger.error("Error generando datos de prueba", e);
            redirectAttributes.addFlashAttribute("error", "Error al generar datos de prueba: " + e.getMessage());
        }
        
        return "redirect:/admin/actividad";
    }
    
    /**
     * Crear alertas de ejemplo para pruebas
     */
    @PostMapping("/alertas/crear-ejemplos")
    public String crearAlertasEjemplo(org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            alertaService.crearAlertasEjemplo();
            redirectAttributes.addFlashAttribute("success", "Alertas de ejemplo creadas exitosamente");
        } catch (Exception e) {
            logger.error("Error al crear alertas de ejemplo", e);
            redirectAttributes.addFlashAttribute("error", "Error al crear alertas de ejemplo: " + e.getMessage());
        }
        return "redirect:/admin/alertas";
    }
    
    /**
     * Crear alertas de ejemplo para pruebas (GET endpoint sin CSRF)
     */
    @GetMapping("/alertas/crear-ejemplos-test")
    public String crearAlertasEjemploTest(org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            alertaService.crearAlertasEjemplo();
            redirectAttributes.addFlashAttribute("success", "Alertas de ejemplo creadas exitosamente via GET");
        } catch (Exception e) {
            logger.error("Error al crear alertas de ejemplo via GET", e);
            redirectAttributes.addFlashAttribute("error", "Error al crear alertas de ejemplo: " + e.getMessage());
        }
        return "redirect:/admin/alertas";
    }
    
    /**
     * Crear alertas de ejemplo DIRECTO sin servicio (para debug)
     */
    @GetMapping("/alertas/crear-directo")
    public String crearAlertasDirecto(org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            logger.info("Creando alertas directamente con repository...");
            
            // Crear alerta 1 directamente
            com.sagafalabella.inventario.model.Alerta alerta1 = new com.sagafalabella.inventario.model.Alerta();
            alerta1.setTitulo("DIRECTO - Stock Crítico Laptop");
            alerta1.setDescripcion("Alerta creada directamente desde controlador");
            alerta1.setTipoAlerta(com.sagafalabella.inventario.model.Alerta.TipoAlerta.STOCK_BAJO);
            alerta1.setNivelPrioridad(com.sagafalabella.inventario.model.Alerta.NivelPrioridad.CRITICA);
            alerta1.setEstado(com.sagafalabella.inventario.model.Alerta.EstadoAlerta.NO_LEIDA);
            alerta1.setEntidadRelacionada("PRODUCTO");
            alerta1.setIdEntidadRelacionada(1L);
            alerta1.setFechaCreacion(java.time.LocalDateTime.now());
            
            // Guardar directamente con repository
            com.sagafalabella.inventario.model.Alerta alertaGuardada = alertaRepository.save(alerta1);
            logger.info("Alerta guardada con ID: {}", alertaGuardada.getIdAlerta());
            
            redirectAttributes.addFlashAttribute("success", "Alerta creada directamente - ID: " + alertaGuardada.getIdAlerta());
        } catch (Exception e) {
            logger.error("Error al crear alerta directamente", e);
           
            redirectAttributes.addFlashAttribute("error", "Error directo: " + e.getMessage());
        }
        return "redirect:/admin/alertas";
    }
    
    /**
     * Verificar conexión a base de datos y tabla alertas
     */
    @GetMapping("/alertas/verificar-bd")
    @ResponseBody
    public String verificarBaseDatos() {
        try {
            // Contar alertas existentes
            long totalAlertas = alertaRepository.count();
            
            // Intentar obtener todas las alertas
            java.util.List<com.sagafalabella.inventario.model.Alerta> alertas = alertaRepository.findAll();
            
            StringBuilder resultado = new StringBuilder();
            resultado.append("=== VERIFICACIÓN BASE DE DATOS ===\n");
            resultado.append("Total alertas en BD: ").append(totalAlertas).append("\n");
            resultado.append("Alertas encontradas: ").append(alertas.size()).append("\n\n");
            
            if (!alertas.isEmpty()) {
                resultado.append("ALERTAS EXISTENTES:\n");
                for (com.sagafalabella.inventario.model.Alerta alerta : alertas) {
                    resultado.append("- ID: ").append(alerta.getIdAlerta())
                            .append(", Título: ").append(alerta.getTitulo())
                            .append(", Estado: ").append(alerta.getEstado())
                            .append(", Tipo: ").append(alerta.getTipoAlerta())
                            .append(", Prioridad: ").append(alerta.getNivelPrioridad())
                            .append("\n");
                }
            } else {
                resultado.append("No hay alertas en la base de datos.\n");
            }
            
            return resultado.toString();
        } catch (Exception e) {
            logger.error("Error verificando base de datos", e);
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Endpoint de prueba para verificar alertas sin autenticación
     */
    @GetMapping("/test/alertas-simple")
    @ResponseBody
    public String testAlertasSimple() {
        try {
            // Obtener todas las alertas directamente
            java.util.List<com.sagafalabella.inventario.model.Alerta> alertas = alertaRepository.findAll();
            
            StringBuilder resultado = new StringBuilder();
            resultado.append("=== TEST ALERTAS SIMPLE ===\n");
            resultado.append("Total alertas en BD: ").append(alertas.size()).append("\n\n");
            
            if (!alertas.isEmpty()) {
                resultado.append("PRIMERAS 5 ALERTAS:\n");
                for (int i = 0; i < Math.min(5, alertas.size()); i++) {
                    com.sagafalabella.inventario.model.Alerta alerta = alertas.get(i);
                    resultado.append("- ID: ").append(alerta.getIdAlerta())
                           .append(" | Título: ").append(alerta.getTitulo())
                           .append(" | Estado: ").append(alerta.getEstado())
                           .append(" | Tipo: ").append(alerta.getTipoAlerta())
                           .append("\n");
                }
            } else {
                resultado.append("No hay alertas en la base de datos.\n");
            }
            
            return resultado.toString();
        } catch (Exception e) {
            logger.error("Error en test de alertas simple", e);
            return "ERROR: " + e.getMessage();
        }
    }    /**
     * Endpoint para probar transacciones de alertas de manera aislada
     */
    @GetMapping("/alertas/test-transaccion")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testTransaccionAlertas() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Probar obtener todas las alertas directamente del repositorio
            List<Alerta> alertas = alertaRepository.findAll();
            response.put("totalAlertas", alertas.size());
            
            // Probar estadísticas básicas
            long totalRegistros = alertaRepository.count();
            response.put("totalRegistrosBD", totalRegistros);
            
            // Probar consulta simple de alertas no leídas
            List<Alerta> noLeidas = alertaRepository.findByEstadoOrderByFechaCreacionDesc(Alerta.EstadoAlerta.NO_LEIDA);
            response.put("alertasNoLeidas", noLeidas.size());
            
            // Probar algunas consultas de conteo
            long criticas = alertaRepository.countAlertasCriticasNoResueltas();
            response.put("alertasCriticas", criticas);
            
            response.put("status", "success");
            response.put("message", "Transacciones funcionando correctamente");
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error en transacciones: " + e.getMessage());
            response.put("stackTrace", Arrays.toString(e.getStackTrace()));
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint de debug específico para probar filtros
     */
    @GetMapping("/alertas/debug-filtros")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugFiltros(
            @RequestParam(required = false) String tipoAlerta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String nivelPrioridad) {
        
        Map<String, Object> debug = new HashMap<>();
        
        try {
            // Log de parámetros recibidos
            debug.put("parametrosRecibidos", Map.of(
                "tipoAlerta", tipoAlerta != null ? tipoAlerta : "null",
                "estado", estado != null ? estado : "null", 
                "nivelPrioridad", nivelPrioridad != null ? nivelPrioridad : "null"
            ));
            
            // Convertir parámetros
            Alerta.TipoAlerta tipoEnum = null;
            if (tipoAlerta != null && !tipoAlerta.isEmpty() && !tipoAlerta.equals("todos")) {
                try {
                    tipoEnum = Alerta.TipoAlerta.valueOf(tipoAlerta.toUpperCase());
                } catch (Exception e) {
                    debug.put("errorTipo", e.getMessage());
                }
            }
            
            Alerta.EstadoAlerta estadoEnum = null;
            if (estado != null && !estado.isEmpty() && !estado.equals("todos")) {
                try {
                    estadoEnum = Alerta.EstadoAlerta.valueOf(estado.toUpperCase());
                } catch (Exception e) {
                    debug.put("errorEstado", e.getMessage());
                }
            }
            
            Alerta.NivelPrioridad nivelEnum = null;
            if (nivelPrioridad != null && !nivelPrioridad.isEmpty() && !nivelPrioridad.equals("todos")) {
                try {
                    nivelEnum = Alerta.NivelPrioridad.valueOf(nivelPrioridad.toUpperCase());
                } catch (Exception e) {
                    debug.put("errorPrioridad", e.getMessage());
                }
            }
            
            debug.put("enumsConvertidos", Map.of(
                "tipoEnum", tipoEnum != null ? tipoEnum.name() : "null",
                "estadoEnum", estadoEnum != null ? estadoEnum.name() : "null",
                "nivelEnum", nivelEnum != null ? nivelEnum.name() : "null"
            ));
            
            // Probar consulta con filtros
            Pageable pageable = PageRequest.of(0, 10);
            Page<Alerta> resultado = alertaService.buscarConFiltros(tipoEnum, estadoEnum, nivelEnum, null, null, pageable);
            
            debug.put("resultadoConsulta", Map.of(
                "totalElementos", resultado.getTotalElements(),
                "elementosEnPagina", resultado.getContent().size(),
                "paginaVacia", resultado.isEmpty()
            ));
            
            // También probar consulta directa al repositorio
            long totalDirecto = alertaRepository.count();
            debug.put("totalDirectoRepositorio", totalDirecto);
            
            // Listar primeras alertas para verificar datos
            List<Alerta> primerasAlertas = alertaRepository.findAll().stream().limit(3).collect(Collectors.toList());
            debug.put("primerasAlertasDisponibles", primerasAlertas.stream()
                .map(a -> Map.of(
                    "id", a.getIdAlerta(),
                    "titulo", a.getTitulo(),
                    "tipo", a.getTipoAlerta().name(),
                    "estado", a.getEstado().name(),
                    "prioridad", a.getNivelPrioridad().name()
                ))
                .collect(Collectors.toList()));
                
            debug.put("status", "success");
            
        } catch (Exception e) {
            debug.put("status", "error");
            debug.put("error", e.getMessage());
            debug.put("stackTrace", Arrays.toString(e.getStackTrace()));
        }
        
        return ResponseEntity.ok(debug);
    }
}
