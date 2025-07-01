package com.sagafalabella.inventario.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sagafalabella.inventario.model.Producto;
import com.sagafalabella.inventario.model.Usuario;
import com.sagafalabella.inventario.service.ProductoService;
import com.sagafalabella.inventario.service.UsuarioService;

/**
 * Controlador para la gestión de inventario
 * Maneja las páginas de productos, stock y control de inventario
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Controller
@RequestMapping("/inventario")
public class InventarioController {

    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private UsuarioService usuarioService;    /**
     * Página principal del inventario
     */
    @GetMapping("")
    public String inventario(Model model,
                           @RequestParam(defaultValue = "") String search,
                           @PageableDefault(size = 12) Pageable pageable) {
        
        // Obtener usuario actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            if (usuarioOpt.isPresent()) {
                model.addAttribute("usuario", usuarioOpt.get());
                model.addAttribute("nombreCompleto", usuarioOpt.get().getNombreCompleto());
            }
        }

        try {
            // Estadísticas del inventario
            model.addAttribute("totalProductos", productoService.contarProductosActivos());
            model.addAttribute("productosStockBajo", productoService.contarProductosStockBajo());
            
            // Lista de productos
            List<Producto> productos;
            if (search != null && !search.trim().isEmpty()) {
                productos = productoService.buscarPorNombre(search);
                model.addAttribute("searchQuery", search);
            } else {
                productos = productoService.listarProductosActivos();
            }
            
            model.addAttribute("productos", productos);
            
            // Productos con stock bajo para alertas
            List<Producto> productosStockBajo = productoService.listarProductosStockBajo();
            model.addAttribute("productosStockBajoLista", productosStockBajo);
            
        } catch (Exception e) {
            // Valores por defecto en caso de error
            model.addAttribute("totalProductos", 0);
            model.addAttribute("productosStockBajo", 0);
            model.addAttribute("productos", List.of());
            model.addAttribute("productosStockBajoLista", List.of());
        }

        return "inventario/dashboard";
    }

    /**
     * Página de productos específicos
     */
    @GetMapping("/productos")
    public String productos(Model model, 
                          @RequestParam(defaultValue = "") String search,
                          @PageableDefault(size = 16) Pageable pageable) {
        
        // Obtener usuario actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
            if (usuarioOpt.isPresent()) {
                model.addAttribute("usuario", usuarioOpt.get());
            }
        }

        try {
            List<Producto> productos;
            if (search != null && !search.trim().isEmpty()) {
                productos = productoService.buscarPorNombre(search);
                model.addAttribute("searchQuery", search);
            } else {
                productos = productoService.listarProductosActivos();
            }
            
            model.addAttribute("productos", productos);
            model.addAttribute("totalProductos", productos.size());
            
        } catch (Exception e) {
            model.addAttribute("productos", List.of());
            model.addAttribute("totalProductos", 0);
        }

        return "inventario/productos";
    }
}
