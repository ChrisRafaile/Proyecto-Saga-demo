package com.sagafalabella.inventario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Controlador para manejo de autenticación (Login/Logout)
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    /**
     * Página de login
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        
        // Si el usuario ya está autenticado, redirigir al dashboard
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        
        if (logout != null) {
            model.addAttribute("message", "Has cerrado sesión exitosamente");
        }
        
        return "auth/login";
    }

    /**
     * Página de selección de tipo de registro
     */
    @GetMapping("/register")
    public String registerChoice() {
        return "auth/register-choice";
    }

    /**
     * Página de acceso denegado
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }
}
