package com.sagafalabella.inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.sagafalabella.inventario.security.CustomAuthenticationSuccessHandler;

/**
 * Configuración de seguridad para el Sistema de Inventario Saga Falabella
 * Define roles, permisos y autenticación
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configuración del encoder de contraseñas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración del administrador de autenticación
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configuración de la cadena de filtros de seguridad
     */    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) throws Exception {
        return http
            .authorizeHttpRequests(authz -> authz
                // Recursos públicos - PRIMERO Y EXPLÍCITO con patrón más amplio
                .requestMatchers("/", "/index*", "/home*").permitAll()
                .requestMatchers("/about*", "/contact*", "/help*", "/demo*", "/catalogo*").permitAll()
                .requestMatchers("/auth/**", "/register/**").permitAll()
                  // Recursos estáticos - Patrón más amplio
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/static/**").permitAll()
                .requestMatchers("/webjars/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/inventario/css/**", "/inventario/js/**", "/inventario/images/**", "/inventario/fonts/**").permitAll()
                .requestMatchers("/test/**", "/api/test/**").permitAll()
                
                // Rutas autenticadas - administración
                .requestMatchers("/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN_INVENTARIO", "ADMIN_VENTAS")
                .requestMatchers("/proveedores/**").hasAnyRole("SUPER_ADMIN", "ADMIN_INVENTARIO")
                .requestMatchers("/inventario/productos/**", "/inventario/almacen/**").hasAnyRole("SUPER_ADMIN", "ADMIN_INVENTARIO", "EMPLEADO_ALMACEN")
                .requestMatchers("/ventas/**", "/pedidos/**").hasAnyRole("SUPER_ADMIN", "ADMIN_VENTAS", "EMPLEADO_VENTAS")
                .requestMatchers("/register/empleado").hasAnyRole("SUPER_ADMIN", "ADMIN_INVENTARIO", "ADMIN_VENTAS")
                .requestMatchers("/reportes/**").hasAnyRole("SUPER_ADMIN", "ADMIN_INVENTARIO", "ADMIN_VENTAS")
                .requestMatchers("/cliente/**").hasRole("CLIENTE")
                .requestMatchers("/dashboard").authenticated()
                
                // Todas las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .successHandler(customAuthenticationSuccessHandler)
                .failureUrl("/auth/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/auth/access-denied")
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .build();
    }
}
