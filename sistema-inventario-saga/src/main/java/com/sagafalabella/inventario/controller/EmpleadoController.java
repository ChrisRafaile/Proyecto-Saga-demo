package com.sagafalabella.inventario.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sagafalabella.inventario.model.Usuario;
import com.sagafalabella.inventario.model.Usuario.RolUsuario;
import com.sagafalabella.inventario.service.ProductoService;
import com.sagafalabella.inventario.service.UsuarioService;

/**
 * Controlador para los dashboards específicos de empleado
 * Portal de empleado con funcionalidades específicas según el tipo de empleado
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Controller
@RequestMapping("/empleado")
public class EmpleadoController {

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private ProductoService productoService;

    /**
     * Dashboard principal de empleado
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
        
        // Verificar que el usuario sea empleado
        if (!esEmpleado(usuario)) {
            return "redirect:/auth/access-denied";
        }
        
        // Datos para el dashboard del empleado
        try {
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
            model.addAttribute("empleadoRol", usuario.getRol().getDescripcion());
            model.addAttribute("totalProductos", productoService.contarProductosActivos());
            model.addAttribute("productosStockBajo", productoService.contarProductosStockBajo());
            model.addAttribute("tareasAsignadas", 0); // Pendiente: implementar
            
            // Información específica según el tipo de empleado
            if (usuario.getRol() == RolUsuario.EMPLEADO_ALMACEN) {
                model.addAttribute("tipoEmpleado", "Almacén");
                model.addAttribute("productosRecibidos", 0); // Pendiente: implementar
                model.addAttribute("movimientosHoy", 0); // Pendiente: implementar
            } else if (usuario.getRol() == RolUsuario.EMPLEADO_VENTAS) {
                model.addAttribute("tipoEmpleado", "Ventas");
                model.addAttribute("ventasHoy", 0); // Pendiente: implementar
                model.addAttribute("clientesAtendidos", 0); // Pendiente: implementar
            }
            
        } catch (Exception e) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
            model.addAttribute("empleadoRol", usuario.getRol().getDescripcion());
            model.addAttribute("totalProductos", 0);
            model.addAttribute("productosStockBajo", 0);
            model.addAttribute("tareasAsignadas", 0);
        }
        
        return "empleado/dashboard";
    }
    
    /**
     * Gestión de productos para empleados de almacén
     */
    @GetMapping("/productos")
    public String productos(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
        
        if (usuarioOpt.isEmpty() || !esEmpleado(usuarioOpt.get())) {
            return "redirect:/auth/access-denied";
        }
        
        Usuario usuario = usuarioOpt.get();
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
        
        // Aquí se agregarían los productos para gestión
        // model.addAttribute("productos", productoService.obtenerProductosParaEmpleado());
        
        return "empleado/productos";
    }
    
    /**
     * Reportes para empleados
     */
    @GetMapping("/reportes")
    public String reportes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
        
        if (usuarioOpt.isEmpty() || !esEmpleado(usuarioOpt.get())) {
            return "redirect:/auth/access-denied";
        }
        
        Usuario usuario = usuarioOpt.get();
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
        
        return "empleado/reportes";
    }
    
    /**
     * Perfil del empleado
     */
    @GetMapping("/perfil")
    public String perfil(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
        
        if (usuarioOpt.isEmpty() || !esEmpleado(usuarioOpt.get())) {
            return "redirect:/auth/access-denied";
        }
        
        Usuario usuario = usuarioOpt.get();
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
        
        return "empleado/perfil";
    }
    
    /**
     * Configuración para empleado
     */
    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
        
        if (usuarioOpt.isEmpty() || !esEmpleado(usuarioOpt.get())) {
            return "redirect:/auth/access-denied";
        }
        
        Usuario usuario = usuarioOpt.get();
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
        
        return "empleado/configuracion";
    }

    /**
     * Verifica si el usuario es empleado
     */
    private boolean esEmpleado(Usuario usuario) {
        return switch (usuario.getRol()) {
            case EMPLEADO_ALMACEN, EMPLEADO_VENTAS -> true;
            default -> false;
        };
    }
}
