package com.sagafalabella.inventario.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controlador dedicado para manejar el logout del sistema
 * Proporciona múltiples métodos de logout para garantizar compatibilidad
 */
@Controller
@RequestMapping("/logout")
public class LogoutController {

    /**
     * Página intermedia de logout con experiencia profesional
     * Permite al usuario confirmar el logout antes de proceder
     */
    @GetMapping("/page")
    public String showLogoutPage(Model model, Authentication authentication) {
        // Verificar si el usuario está autenticado
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/auth/login?expired=true";
        }
        
        // Obtener información del usuario para mostrar en la página
        String username = authentication.getName();
        model.addAttribute("username", username);
        model.addAttribute("isAuthenticated", true);
        
        return "auth/logout";
    }
    
    /**
     * Procesamiento directo del logout (POST)
     * Este método invalida la sesión y redirige al login
     */
    @PostMapping("/process")
    public String processLogout(HttpServletRequest request, HttpServletResponse response, 
                               Authentication authentication, RedirectAttributes redirectAttributes) {
        
        // Verificar si hay una sesión activa
        if (authentication != null) {
            // Realizar logout usando Spring Security
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        
        // Limpiar contexto de seguridad
        SecurityContextHolder.clearContext();
        
        // Invalidar sesión explícitamente
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }
        
        // Agregar mensaje de éxito
        redirectAttributes.addFlashAttribute("logoutSuccess", true);
        redirectAttributes.addFlashAttribute("message", "Sesión cerrada exitosamente");
        
        return "redirect:/auth/login?logout=true";
    }
    
    /**
     * Logout directo (GET) - para compatibilidad con Spring Security
     * Redirige al POST handler para consistencia
     */
    @GetMapping("/direct")
    public String directLogout(HttpServletRequest request, HttpServletResponse response, 
                              Authentication authentication) {
        
        // Si hay autenticación, proceder con logout directo
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            SecurityContextHolder.clearContext();
            
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }
        }
        
        return "redirect:/auth/login?logout=true";
    }
    
    /**
     * Logout de emergencia - para casos donde otros métodos fallen
     * Fuerza la limpieza de sesión sin verificaciones adicionales
     */
    @GetMapping("/force")
    public String forceLogout(HttpServletRequest request, HttpServletResponse response) {
        
        try {
            // Obtener autenticación actual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }
            
            // Limpiar contexto de seguridad
            SecurityContextHolder.clearContext();
            
            // Invalidar sesión si existe
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }
            
            // Limpiar cookies relacionadas con la sesión
            response.addHeader("Set-Cookie", "JSESSIONID=; Path=/; HttpOnly; Max-Age=0");
            
        } catch (Exception e) {
            // Log error pero continuar con redirect
            System.err.println("Error durante logout forzado: " + e.getMessage());
        }
        
        return "redirect:/auth/login?logout=true&forced=true";
    }
    
    /**
     * Endpoint para AJAX logout requests
     * Útil para aplicaciones SPA o requests asíncronos
     */
    @PostMapping("/ajax")
    public String ajaxLogout(HttpServletRequest request, HttpServletResponse response, 
                            Authentication authentication) {
        
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            SecurityContextHolder.clearContext();
            
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }
        }
        
        // Para requests AJAX, devolver JSON o código de estado
        response.setStatus(HttpServletResponse.SC_OK);
        return "redirect:/auth/login?logout=true";
    }
}
