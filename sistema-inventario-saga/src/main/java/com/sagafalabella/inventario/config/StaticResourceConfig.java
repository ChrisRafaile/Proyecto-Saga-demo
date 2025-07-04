package com.sagafalabella.inventario.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración para el manejo optimizado de recursos estáticos
 * Incluye configuración específica para favicon
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuración para archivos estáticos generales
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600); // Cache por 1 hora
        
        // Configuración específica para CSS, JS e imágenes
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(3600);
        
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(3600);
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(3600);
        
        // Configuración específica para favicon con cache corto para debugging
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico")
                .setCachePeriod(0); // Sin cache para debugging
        
        registry.addResourceHandler("/favicon.svg")
                .addResourceLocations("classpath:/static/favicon.svg")
                .setCachePeriod(0); // Sin cache para debugging
        
        registry.addResourceHandler("/favicon.png")
                .addResourceLocations("classpath:/static/favicon.png")
                .setCachePeriod(0); // Sin cache para debugging
        
        registry.addResourceHandler("/favicon-16x16.png")
                .addResourceLocations("classpath:/static/favicon-16x16.png")
                .setCachePeriod(0); // Sin cache para debugging
        
        registry.addResourceHandler("/apple-touch-icon.png")
                .addResourceLocations("classpath:/static/apple-touch-icon.png")
                .setCachePeriod(0); // Sin cache para debugging
    }
}
