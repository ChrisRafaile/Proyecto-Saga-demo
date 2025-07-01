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
 * Controlador para los dashboards específicos de cliente
 * Portal de cliente con funcionalidades limitadas
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Controller
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private ProductoService productoService;

    /**
     * Dashboard principal de cliente
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
          // Verificar que el usuario sea cliente
        if (usuario.getRol() != RolUsuario.CLIENTE) {
            return "redirect:/auth/access-denied";
        }
        
        // Datos para el dashboard del cliente
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
        
        // Información del cliente (valores por defecto por ahora)
        model.addAttribute("totalPedidos", 0); 
        model.addAttribute("pedidosPendientes", 0); 
        model.addAttribute("productosFavoritos", 0);
        model.addAttribute("totalGastado", 0); 
        
        // Productos disponibles (limitado)
        try {
            model.addAttribute("productosDisponibles", productoService.contarProductosActivos());
        } catch (Exception e) {
            model.addAttribute("productosDisponibles", 0);
        }
        
        return "client/dashboard";
    }
      /**
     * Catálogo de productos para cliente
     */
    @GetMapping("/productos")
    public String productos(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
        
        if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != RolUsuario.CLIENTE) {
            return "redirect:/auth/access-denied";
        }
        
        Usuario usuario = usuarioOpt.get();
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
        
        // Aquí se agregarían los productos disponibles
        // model.addAttribute("productos", productoService.obtenerProductosActivos());
        
        return "client/productos";
    }
      /**
     * Perfil del cliente
     */
    @GetMapping("/perfil")
    public String perfil(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
        
        if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() != RolUsuario.CLIENTE) {
            return "redirect:/auth/access-denied";
        }
        
        Usuario usuario = usuarioOpt.get();
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
        
        return "client/perfil";
    }
}
