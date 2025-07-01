package com.sagafalabella.inventario.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sagafalabella.inventario.model.Usuario;
import com.sagafalabella.inventario.service.ProductoService;
import com.sagafalabella.inventario.service.UsuarioService;

/**
 * Controlador para el dashboard principal
 * Muestra diferentes interfaces según el rol del usuario
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Controller
public class DashboardController {    @Autowired
    private UsuarioService usuarioService;    @Autowired
    private ProductoService productoService;

    /**
     * Dashboard principal - muestra diferentes vistas según el rol
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/auth/login";
        }
          // Obtener nombre del usuario autenticado
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
        
        if (usuarioOpt.isEmpty()) {
            return "redirect:/auth/login?error=user_not_found";
        }
        
        Usuario usuario = usuarioOpt.get();
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
        
        // Redirigir según el rol del usuario
        return switch (usuario.getRol()) {
            case SUPER_ADMIN, ADMIN_INVENTARIO, ADMIN_VENTAS -> dashboardAdmin(model, usuario);
            case EMPLEADO_ALMACEN, EMPLEADO_VENTAS -> dashboardEmpleado(model, usuario);
            case CLIENTE -> dashboardCliente(model, usuario);
            default -> "redirect:/auth/access-denied";
        };
    }    /**
     * Dashboard para Administradores
     */
    private String dashboardAdmin(Model model, Usuario usuario) {
        // Estadísticas generales del sistema
        try {
            model.addAttribute("totalUsuarios", usuarioService.contarUsuariosActivos());
            model.addAttribute("totalProductos", productoService.contarProductosActivos());
            model.addAttribute("pedidosPendientes", 0); // Pendiente: PedidoService
            model.addAttribute("proveedoresActivos", 0); // Pendiente: ProveedorService
            
            // Información adicional para el dashboard
            model.addAttribute("alertasCount", 3);
            model.addAttribute("usuarioActual", usuario.getNombreCompleto());
        } catch (Exception e) {
            // Valores por defecto en caso de error
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("totalProductos", 0);
            model.addAttribute("pedidosPendientes", 0);
            model.addAttribute("proveedoresActivos", 0);
            model.addAttribute("alertasCount", 0);
            model.addAttribute("usuarioActual", "Usuario");
        }
        
        return "admin/dashboard";
    }    /**
     * Dashboard para Empleados
     */
    private String dashboardEmpleado(Model model, Usuario usuario) {
        // Estadísticas básicas para empleados
        try {
            model.addAttribute("totalProductos", productoService.contarProductosActivos());
            model.addAttribute("productosStockBajo", productoService.contarProductosStockBajo());
            model.addAttribute("tareasAsignadas", 0); // Pendiente: implementar
            
            // Información específica del empleado
            model.addAttribute("empleadoNombre", usuario.getNombreCompleto());
            model.addAttribute("empleadoRol", usuario.getRol().toString());
        } catch (Exception e) {
            model.addAttribute("totalProductos", 0);
            model.addAttribute("productosStockBajo", 0);
            model.addAttribute("tareasAsignadas", 0);
            model.addAttribute("empleadoNombre", "Empleado");
            model.addAttribute("empleadoRol", "");
        }
        
        return "empleado/dashboard";
    }/**
     * Dashboard para Cliente
     */
    private String dashboardCliente(Model model, Usuario usuario) {
        // Información del usuario actual
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
        
        // Información del cliente (valores por defecto por ahora)
        model.addAttribute("totalPedidos", 0); 
        model.addAttribute("pedidosPendientes", 0); 
        model.addAttribute("productosFavoritos", 0);
        model.addAttribute("totalGastado", 0); 
          return "client/dashboard";
    }
}
