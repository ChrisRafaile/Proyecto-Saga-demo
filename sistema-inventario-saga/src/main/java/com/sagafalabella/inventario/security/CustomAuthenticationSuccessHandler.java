package com.sagafalabella.inventario.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.sagafalabella.inventario.model.Usuario;
import com.sagafalabella.inventario.service.UsuarioService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Manejador personalizado del éxito de autenticación
 * Redirige a los usuarios a diferentes dashboards según su rol
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    @Lazy
    private UsuarioService usuarioService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        // Obtener el usuario autenticado
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
        
        if (usuarioOpt.isEmpty()) {
            response.sendRedirect("/auth/login?error=user_not_found");
            return;
        }
        
        Usuario usuario = usuarioOpt.get();
        String redirectUrl = determineTargetUrl(usuario);
        
        response.sendRedirect(redirectUrl);
    }
    
    /**
     * Determina la URL de destino basada en el rol del usuario
     */    private String determineTargetUrl(Usuario usuario) {
        return switch (usuario.getRol()) {
            case SUPER_ADMIN, ADMIN_INVENTARIO, ADMIN_VENTAS -> "/admin/dashboard";
            case EMPLEADO_ALMACEN, EMPLEADO_VENTAS -> "/empleado/dashboard";
            case CLIENTE -> "/client/dashboard";
            default -> "/dashboard"; // Fallback por si acaso
        };
    }
}
