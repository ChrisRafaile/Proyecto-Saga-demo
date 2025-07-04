package com.sagafalabella.inventario.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sagafalabella.inventario.model.Cliente;
import com.sagafalabella.inventario.model.Producto;
import com.sagafalabella.inventario.model.Usuario;
import com.sagafalabella.inventario.service.ClienteService;
import com.sagafalabella.inventario.service.ProductoService;
import com.sagafalabella.inventario.service.UsuarioService;

/**
 * Controlador para la gestión de clientes y flujo de e-commerce
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Controller
@RequestMapping("/client")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;

    // Simulación de carrito de compras en memoria (en producción usar base de datos)
    private final Map<String, Map<Long, Integer>> carritoPorUsuario = new HashMap<>();

    /**
     * Dashboard principal del cliente
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/login";
            }

            String username = auth.getName();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                model.addAttribute("usuario", usuario);

                // Buscar cliente asociado al usuario
                Optional<Cliente> clienteOpt = clienteService.obtenerClientePorUsuario(usuario.getId());
                if (clienteOpt.isPresent()) {
                    model.addAttribute("cliente", clienteOpt.get());
                } else {
                    model.addAttribute("cliente", null);
                }

                // Estadísticas básicas
                long totalProductos = productoService.contarProductosActivos();
                model.addAttribute("totalProductos", totalProductos);

                // Productos destacados (últimos 6 productos activos)
                List<Producto> productosDestacados = productoService.listarProductosActivos()
                        .stream()
                        .limit(6)
                        .toList();
                model.addAttribute("productosDestacados", productosDestacados);

                return "client/dashboard";
            }

            return "redirect:/login";
            
        } catch (Exception e) {
            logger.error("Error en dashboard del cliente: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar el dashboard");
            return "client/dashboard";
        }
    }

    /**
     * Catálogo de productos con búsqueda y filtros
     */
    @GetMapping("/catalogo")
    public String catalogo(Model model,
                          @RequestParam(value = "search", required = false) String search,
                          @RequestParam(value = "categoria", required = false) String categoria,
                          @RequestParam(value = "marca", required = false) String marca,
                          @RequestParam(value = "ordenar", defaultValue = "nombre") String ordenar,
                          @RequestParam(value = "page", defaultValue = "0") int page) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/login";
            }

            String username = auth.getName();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                model.addAttribute("nombreCompleto", usuario.getNombre() + " " + usuario.getApellido());
                
                // Obtener carrito para mostrar contador
                Map<Long, Integer> carrito = carritoPorUsuario.getOrDefault(username, new HashMap<>());
                int totalItemsCarrito = carrito.values().stream().mapToInt(Integer::intValue).sum();
                model.addAttribute("session", Map.of("totalItemsCarrito", totalItemsCarrito));
            }

            List<Producto> productos = productoService.listarProductosActivos();
            
            // Filtrar por búsqueda
            if (search != null && !search.trim().isEmpty()) {
                productos = productos.stream()
                        .filter(p -> p.getNombre().toLowerCase().contains(search.toLowerCase()) ||
                                   (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(search.toLowerCase())))
                        .toList();
                model.addAttribute("search", search);
            }
            
            // Filtrar por categoría
            if (categoria != null && !categoria.trim().isEmpty()) {
                productos = productos.stream()
                        .filter(p -> p.getCategoria() != null && p.getCategoria().equals(categoria))
                        .toList();
                model.addAttribute("categoria", categoria);
            }
            
            // Filtrar por marca
            if (marca != null && !marca.trim().isEmpty()) {
                productos = productos.stream()
                        .filter(p -> p.getMarca() != null && p.getMarca().equals(marca))
                        .toList();
                model.addAttribute("marca", marca);
            }
            
            // Ordenar productos
            switch (ordenar) {
                case "precio":
                    productos = productos.stream()
                            .sorted((p1, p2) -> p1.getPrecio().compareTo(p2.getPrecio()))
                            .toList();
                    break;
                case "categoria":
                    productos = productos.stream()
                            .sorted((p1, p2) -> {
                                String cat1 = p1.getCategoria() != null ? p1.getCategoria() : "";
                                String cat2 = p2.getCategoria() != null ? p2.getCategoria() : "";
                                return cat1.compareTo(cat2);
                            })
                            .toList();
                    break;
                case "marca":
                    productos = productos.stream()
                            .sorted((p1, p2) -> {
                                String m1 = p1.getMarca() != null ? p1.getMarca() : "";
                                String m2 = p2.getMarca() != null ? p2.getMarca() : "";
                                return m1.compareTo(m2);
                            })
                            .toList();
                    break;
                default: // nombre
                    productos = productos.stream()
                            .sorted((p1, p2) -> p1.getNombre().compareTo(p2.getNombre()))
                            .toList();
                    break;
            }
            
            // Obtener listas para filtros
            List<Producto> todosLosProductos = productoService.listarProductosActivos();
            List<String> categorias = todosLosProductos.stream()
                    .map(Producto::getCategoria)
                    .filter(cat -> cat != null && !cat.trim().isEmpty())
                    .distinct()
                    .sorted()
                    .toList();
            List<String> marcas = todosLosProductos.stream()
                    .map(Producto::getMarca)
                    .filter(m -> m != null && !m.trim().isEmpty())
                    .distinct()
                    .sorted()
                    .toList();
            
            // Paginación simple (6 productos por página)
            int pageSize = 6;
            int totalProductos = productos.size();
            int totalPages = (int) Math.ceil((double) totalProductos / pageSize);
            int startIndex = page * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalProductos);
            
            List<Producto> productosPagina = productos.subList(startIndex, endIndex);

            model.addAttribute("productos", productosPagina);
            model.addAttribute("totalProductos", totalProductos);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("categorias", categorias);
            model.addAttribute("marcas", marcas);
            model.addAttribute("ordenar", ordenar);

            return "client/catalogo";
            
        } catch (Exception e) {
            logger.error("Error en catálogo: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar el catálogo");
            return "client/catalogo";
        }
    }

    /**
     * Detalle de producto
     */
    @GetMapping("/producto/{id}")
    public String detalleProducto(@PathVariable Long id, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/login";
            }

            Optional<Producto> productoOpt = productoService.buscarPorId(id);
            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();
                model.addAttribute("producto", producto);

                // Productos relacionados (misma categoría)
                List<Producto> productosRelacionados = productoService.listarProductosActivos()
                        .stream()
                        .filter(p -> p.getCategoria() != null && 
                                   p.getCategoria().equals(producto.getCategoria()) && 
                                   !p.getIdproducto().equals(id))
                        .limit(4)
                        .toList();
                model.addAttribute("productosRelacionados", productosRelacionados);

                return "client/producto-detalle";
            } else {
                model.addAttribute("error", "Producto no encontrado");
                return "redirect:/client/catalogo";
            }
            
        } catch (Exception e) {
            logger.error("Error en detalle de producto: {}", e.getMessage(), e);
            return "redirect:/client/catalogo";
        }
    }

    /**
     * Agregar producto al carrito
     */
    @PostMapping("/carrito/agregar")
    public String agregarAlCarrito(@RequestParam Long productoId,
                                  @RequestParam(defaultValue = "1") Integer cantidad,
                                  RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/login";
            }

            String username = auth.getName();
            Optional<Producto> productoOpt = productoService.buscarPorId(productoId);
            
            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();
                
                // Verificar stock disponible
                if (producto.getStockActual() < cantidad) {
                    redirectAttributes.addFlashAttribute("error", 
                        "Stock insuficiente. Disponible: " + producto.getStockActual());
                    return "redirect:/client/producto/" + productoId;
                }

                // Agregar al carrito en memoria
                carritoPorUsuario.computeIfAbsent(username, k -> new HashMap<>());
                Map<Long, Integer> carrito = carritoPorUsuario.get(username);
                carrito.put(productoId, carrito.getOrDefault(productoId, 0) + cantidad);

                redirectAttributes.addFlashAttribute("success", 
                    "Producto agregado al carrito");
                return "redirect:/client/producto/" + productoId;
            } else {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/client/catalogo";
            }
            
        } catch (Exception e) {
            logger.error("Error al agregar al carrito: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al agregar al carrito");
            return "redirect:/client/catalogo";
        }
    }

    /**
     * Agregar producto al carrito (AJAX)
     */
    @PostMapping("/carrito/agregar-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarAlCarritoAjax(@RequestParam Long productoId,
                                                                  @RequestParam(defaultValue = "1") Integer cantidad) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Debe iniciar sesión");
                return ResponseEntity.ok(response);
            }

            String username = auth.getName();
            Optional<Producto> productoOpt = productoService.buscarPorId(productoId);
            
            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();
                
                // Verificar stock disponible
                if (producto.getStockActual() < cantidad) {
                    response.put("success", false);
                    response.put("message", "Stock insuficiente. Disponible: " + producto.getStockActual());
                    return ResponseEntity.ok(response);
                }

                // Agregar al carrito en memoria
                carritoPorUsuario.computeIfAbsent(username, k -> new HashMap<>());
                Map<Long, Integer> carrito = carritoPorUsuario.get(username);
                carrito.put(productoId, carrito.getOrDefault(productoId, 0) + cantidad);

                // Calcular total de items en carrito
                int totalItems = carrito.values().stream().mapToInt(Integer::intValue).sum();

                response.put("success", true);
                response.put("message", "Producto agregado al carrito");
                response.put("totalItems", totalItems);
                response.put("productName", producto.getNombre());
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Producto no encontrado");
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            logger.error("Error al agregar al carrito (AJAX): {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al agregar al carrito");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Ver carrito de compras
     */
    @GetMapping("/carrito")
    public String verCarrito(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/login";
            }

            String username = auth.getName();
            Map<Long, Integer> carrito = carritoPorUsuario.getOrDefault(username, new HashMap<>());
            
            // Cargar productos del carrito
            Map<Producto, Integer> productosCarrito = new HashMap<>();
            double totalCarrito = 0.0;
            
            for (Map.Entry<Long, Integer> entry : carrito.entrySet()) {
                Optional<Producto> productoOpt = productoService.buscarPorId(entry.getKey());
                if (productoOpt.isPresent()) {
                    Producto producto = productoOpt.get();
                    Integer cantidad = entry.getValue();
                    productosCarrito.put(producto, cantidad);
                    totalCarrito += producto.getPrecio().doubleValue() * cantidad;
                }
            }

            model.addAttribute("productosCarrito", productosCarrito);
            model.addAttribute("totalCarrito", totalCarrito);
            model.addAttribute("cantidadItems", carrito.size());

            return "client/carrito";
            
        } catch (Exception e) {
            logger.error("Error al ver carrito: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar el carrito");
            return "client/carrito";
        }
    }

    /**
     * Perfil del cliente
     */
    @GetMapping("/perfil")
    public String perfil(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/login";
            }

            String username = auth.getName();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                model.addAttribute("usuario", usuario);

                // Buscar cliente asociado
                Optional<Cliente> clienteOpt = clienteService.obtenerClientePorUsuario(usuario.getId());
                if (clienteOpt.isPresent()) {
                    model.addAttribute("cliente", clienteOpt.get());
                } else {
                    model.addAttribute("cliente", null);
                }

                return "client/perfil";
            }

            return "redirect:/login";
            
        } catch (Exception e) {
            logger.error("Error en perfil del cliente: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar el perfil");
            return "client/perfil";
        }
    }

    /**
     * Página para editar perfil del cliente
     */
    @GetMapping("/perfil/editar")
    public String editarPerfil(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Obtener usuario autenticado
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            if (!usuarioOpt.isPresent()) {
                logger.error("Usuario no encontrado: {}", username);
                return "redirect:/login";
            }
            Usuario usuario = usuarioOpt.get();

            // Obtener cliente asociado
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorUsuario(usuario.getId());
            if (!clienteOpt.isPresent()) {
                logger.error("Cliente no encontrado para usuario: {}", username);
                return "redirect:/login";
            }
            Cliente cliente = clienteOpt.get();

            model.addAttribute("cliente", cliente);
            model.addAttribute("usuario", usuario);
            model.addAttribute("pageTitle", "Editar Perfil");
            
            logger.info("Mostrando edición de perfil para cliente: {}", cliente.getNombre());
            return "client/editar-perfil";
        } catch (Exception e) {
            logger.error("Error al cargar edición de perfil: ", e);
            model.addAttribute("error", "Error al cargar la edición de perfil");
            return "client/perfil";
        }
    }

    /**
     * Actualizar perfil del cliente
     */
    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam String nombre,
                                  @RequestParam String apellido,
                                  @RequestParam String email,
                                  @RequestParam(required = false) String telefono,
                                  @RequestParam(required = false) String dni,
                                  @RequestParam(required = false) String direccion,
                                  @RequestParam(required = false) String ciudad,
                                  @RequestParam(required = false) String codigoPostal,
                                  RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            if (!usuarioOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/client/perfil";
            }
            
            Usuario usuario = usuarioOpt.get();
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorUsuario(usuario.getId());
            
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                
                // Actualizar datos del cliente
                cliente.setNombre(nombre);
                cliente.setApellido(apellido);
                cliente.setEmail(email);
                cliente.setTelefono(telefono);
                cliente.setDni(dni);
                cliente.setDireccion(direccion);
                cliente.setCiudad(ciudad);
                cliente.setCodigoPostal(codigoPostal);
                
                // Guardar cambios
                clienteService.actualizarCliente(cliente);
                
                // También actualizar el usuario
                usuario.setNombre(nombre);
                usuario.setApellido(apellido);
                usuario.setEmail(email);
                usuarioService.guardarUsuario(usuario);
                
                redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
                logger.info("Perfil actualizado para cliente: {}", cliente.getNombre());
            } else {
                redirectAttributes.addFlashAttribute("error", "Cliente no encontrado");
            }
            
            return "redirect:/client/perfil";
            
        } catch (Exception e) {
            logger.error("Error al actualizar perfil: ", e);
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil");
            return "redirect:/client/perfil";
        }
    }

    /**
     * Página de notificaciones del cliente
     */
    @GetMapping("/notificaciones")
    public String notificaciones(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Obtener usuario autenticado
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            if (!usuarioOpt.isPresent()) {
                logger.error("Usuario no encontrado: {}", username);
                return "redirect:/login";
            }
            Usuario usuario = usuarioOpt.get();

            // Obtener cliente asociado
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorUsuario(usuario.getId());
            if (!clienteOpt.isPresent()) {
                logger.error("Cliente no encontrado para usuario: {}", username);
                return "redirect:/login";
            }
            Cliente cliente = clienteOpt.get();

            model.addAttribute("cliente", cliente);
            model.addAttribute("pageTitle", "Mis Notificaciones");
            
            // En una implementación real, aquí cargarías las notificaciones desde la base de datos
            // List<Notificacion> notificaciones = notificacionService.obtenerNotificacionesPorCliente(cliente.getId());
            // model.addAttribute("notificaciones", notificaciones);
            
            logger.info("Mostrando notificaciones para cliente: {}", cliente.getNombre());
            return "client/notificaciones";
        } catch (Exception e) {
            logger.error("Error al cargar notificaciones: ", e);
            model.addAttribute("error", "Error al cargar las notificaciones");
            return "client/dashboard";
        }
    }

    /**
     * Proceso de checkout (simulado)
     */
    @GetMapping("/checkout")
    public String checkout(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/login";
            }

            String username = auth.getName();
            Map<Long, Integer> carrito = carritoPorUsuario.getOrDefault(username, new HashMap<>());
            
            if (carrito.isEmpty()) {
                return "redirect:/client/carrito";
            }

            // Cargar productos del carrito para checkout
            Map<Producto, Integer> productosCarrito = new HashMap<>();
            double totalCarrito = 0.0;
            
            for (Map.Entry<Long, Integer> entry : carrito.entrySet()) {
                Optional<Producto> productoOpt = productoService.buscarPorId(entry.getKey());
                if (productoOpt.isPresent()) {
                    Producto producto = productoOpt.get();
                    Integer cantidad = entry.getValue();
                    productosCarrito.put(producto, cantidad);
                    totalCarrito += producto.getPrecio().doubleValue() * cantidad;
                }
            }

            model.addAttribute("productosCarrito", productosCarrito);
            model.addAttribute("totalCarrito", totalCarrito);

            return "client/checkout";
            
        } catch (Exception e) {
            logger.error("Error en checkout: {}", e.getMessage(), e);
            model.addAttribute("error", "Error en el proceso de checkout");
            return "client/carrito";
        }
    }

    /**
     * Confirmación de pedido (simulado)
     */
    @PostMapping("/confirmar-pedido")
    public String confirmarPedido(RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/login";
            }

            String username = auth.getName();
            Map<Long, Integer> carrito = carritoPorUsuario.getOrDefault(username, new HashMap<>());
            
            if (carrito.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El carrito está vacío");
                return "redirect:/client/carrito";
            }

            // Simular confirmación de pedido
            // En producción: crear pedido en base de datos, actualizar stock, etc.
            
            // Limpiar carrito después de confirmar
            carritoPorUsuario.put(username, new HashMap<>());
            
            redirectAttributes.addFlashAttribute("success", 
                "Pedido confirmado exitosamente. ¡Gracias por su compra!");
            
            return "redirect:/client/dashboard";
            
        } catch (Exception e) {
            logger.error("Error al confirmar pedido: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al confirmar el pedido");
            return "redirect:/client/checkout";
        }
    }

    /**
     * Página de pedidos del cliente
     */
    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Obtener usuario autenticado
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            if (!usuarioOpt.isPresent()) {
                logger.error("Usuario no encontrado: {}", username);
                return "redirect:/login";
            }
            Usuario usuario = usuarioOpt.get();

            // Obtener cliente asociado
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorUsuario(usuario.getId());
            if (!clienteOpt.isPresent()) {
                logger.error("Cliente no encontrado para usuario: {}", username);
                return "redirect:/login";
            }
            Cliente cliente = clienteOpt.get();

            model.addAttribute("cliente", cliente);
            model.addAttribute("pageTitle", "Mis Pedidos");
            
            logger.info("Mostrando pedidos para cliente: {}", cliente.getNombre());
            return "client/pedidos";
        } catch (Exception e) {
            logger.error("Error al cargar pedidos: ", e);
            model.addAttribute("error", "Error al cargar los pedidos");
            return "client/dashboard";
        }
    }

    /**
     * Página de favoritos del cliente
     */
    @GetMapping("/favoritos")
    public String favoritos(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Obtener usuario autenticado
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            if (!usuarioOpt.isPresent()) {
                logger.error("Usuario no encontrado: {}", username);
                return "redirect:/login";
            }
            Usuario usuario = usuarioOpt.get();

            // Obtener cliente asociado
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorUsuario(usuario.getId());
            if (!clienteOpt.isPresent()) {
                logger.error("Cliente no encontrado para usuario: {}", username);
                return "redirect:/login";
            }
            Cliente cliente = clienteOpt.get();

            model.addAttribute("cliente", cliente);
            model.addAttribute("pageTitle", "Mis Favoritos");
            
            logger.info("Mostrando favoritos para cliente: {}", cliente.getNombre());
            return "client/favoritos";
        } catch (Exception e) {
            logger.error("Error al cargar favoritos: ", e);
            model.addAttribute("error", "Error al cargar los favoritos");
            return "client/dashboard";
        }
    }

    /**
     * Página de direcciones del cliente
     */
    @GetMapping("/direcciones")
    public String direcciones(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Obtener usuario autenticado
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            if (!usuarioOpt.isPresent()) {
                logger.error("Usuario no encontrado: {}", username);
                return "redirect:/login";
            }
            Usuario usuario = usuarioOpt.get();

            // Obtener cliente asociado
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorUsuario(usuario.getId());
            if (!clienteOpt.isPresent()) {
                logger.error("Cliente no encontrado para usuario: {}", username);
                return "redirect:/login";
            }
            Cliente cliente = clienteOpt.get();

            model.addAttribute("cliente", cliente);
            model.addAttribute("pageTitle", "Mis Direcciones");
            
            logger.info("Mostrando direcciones para cliente: {}", cliente.getNombre());
            return "client/direcciones";
        } catch (Exception e) {
            logger.error("Error al cargar direcciones: ", e);
            model.addAttribute("error", "Error al cargar las direcciones");
            return "client/dashboard";
        }
    }

    /**
     * Página de soporte del cliente
     */
    @GetMapping("/soporte")
    public String soporte(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Obtener usuario autenticado
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            if (!usuarioOpt.isPresent()) {
                logger.error("Usuario no encontrado: {}", username);
                return "redirect:/login";
            }
            Usuario usuario = usuarioOpt.get();

            // Obtener cliente asociado
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorUsuario(usuario.getId());
            if (!clienteOpt.isPresent()) {
                logger.error("Cliente no encontrado para usuario: {}", username);
                return "redirect:/login";
            }
            Cliente cliente = clienteOpt.get();

            model.addAttribute("cliente", cliente);
            model.addAttribute("pageTitle", "Soporte al Cliente");
            
            logger.info("Mostrando soporte para cliente: {}", cliente.getNombre());
            return "client/soporte";
        } catch (Exception e) {
            logger.error("Error al cargar soporte: ", e);
            model.addAttribute("error", "Error al cargar el soporte");
            return "client/dashboard";
        }
    }

    /**
     * Página de configuración del cliente
     */
    @GetMapping("/configuracion")
    public String mostrarConfiguracion(Model model) {
        try {
            // Obtener usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            if (username == null || "anonymousUser".equals(username)) {
                logger.warn("Usuario no autenticado al acceder a configuración");
                return "redirect:/login";
            }

            // Obtener usuario autenticado
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            if (!usuarioOpt.isPresent()) {
                logger.error("Usuario no encontrado: {}", username);
                return "redirect:/login";
            }
            Usuario usuario = usuarioOpt.get();

            // Obtener cliente asociado
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorUsuario(usuario.getId());
            if (!clienteOpt.isPresent()) {
                logger.error("Cliente no encontrado para usuario: {}", username);
                return "redirect:/login";
            }
            Cliente cliente = clienteOpt.get();

            model.addAttribute("cliente", cliente);
            model.addAttribute("pageTitle", "Configuración");
            
            logger.info("Mostrando configuración para cliente: {}", cliente.getNombre());
            return "client/configuracion";
        } catch (Exception e) {
            logger.error("Error al cargar configuración: ", e);
            model.addAttribute("error", "Error al cargar la configuración");
            return "client/dashboard";
        }
    }

    /**
     * Página de compra exitosa
     */
    @GetMapping("/compra-exitosa")
    public String compraExitosa(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/login";
            }

            // Agregar información adicional si es necesario
            model.addAttribute("showConfetti", true);
            
            return "client/compra-exitosa";
            
        } catch (Exception e) {
            logger.error("Error en página de compra exitosa: {}", e.getMessage(), e);
            return "redirect:/client/dashboard";
        }
    }

    /**
     * Página de prueba para verificar favicon
     */
    @GetMapping("/test-favicon")
    public String testFavicon() {
        return "test-favicon";
    }

}
