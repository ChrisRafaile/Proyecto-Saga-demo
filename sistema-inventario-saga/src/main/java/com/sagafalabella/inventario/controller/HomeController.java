package com.sagafalabella.inventario.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para páginas públicas y de inicio
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Controller
public class HomeController {    /**
     * Página principal del sistema
     * Redirige a dashboard si está autenticado, sino muestra página de bienvenida
     */
    @GetMapping({"/", "/index", "/home"})
    public String index() {
        // Para usuarios no autenticados, siempre mostrar la página de inicio
        // Solo redireccionar si el usuario está explícitamente autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && 
            !auth.getPrincipal().equals("anonymousUser") && 
            !"anonymous".equals(auth.getName())) {
            return "redirect:/dashboard";
        }
        
        // Si no está autenticado o es anónimo, mostrar página de inicio
        return "index";
    }

    /**
     * Página "Acerca de"
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("titulo", "Acerca de");
        return "about";
    }

    /**
     * Página de contacto
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("titulo", "Contacto");
        return "contact";
    }

    /**
     * Página de ayuda
     */
    @GetMapping("/help")
    public String help(Model model) {
        model.addAttribute("titulo", "Ayuda");
        return "help";
    }

    /**
     * Página de demostración del sistema
     */
    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("titulo", "Demo del Sistema");
        return "demo";
    }

    /**
     * Catálogo de productos público (redirige a productos si está autenticado)
     */
    @GetMapping("/catalogo")
    public String catalogo(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Si el usuario está autenticado, redirigir a la página de productos
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/productos";
        }
        
        // Si no está autenticado, mostrar catálogo público
        model.addAttribute("titulo", "Catálogo de Productos");
        return "catalogo-publico";
    }

    /**
     * Página de características del sistema
     */
    @GetMapping("/features")
    public String features(Model model) {
        model.addAttribute("titulo", "Características del Sistema");
        return "features";
    }

    /**
     * Página de precios y planes
     */
    @GetMapping("/pricing")
    public String pricing(Model model) {
        model.addAttribute("titulo", "Planes y Precios");
        return "pricing";
    }
}
