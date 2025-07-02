package com.sagafalabella.inventario.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración Web para recursos estáticos
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AppProperties appProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuración para imágenes de productos subidas
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + appProperties.getUpload().getDir() + "/");
        
        // Configuración para servir recursos estáticos (sin caché durante desarrollo)
        registry.addResourceHandler("/inventario/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(0); // Sin caché para desarrollo
        
        registry.addResourceHandler("/inventario/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(0); // Sin caché para desarrollo
        
        registry.addResourceHandler("/inventario/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(0); // Sin caché para desarrollo
        
        registry.addResourceHandler("/inventario/fonts/**")
                .addResourceLocations("classpath:/static/fonts/")
                .setCachePeriod(0); // Sin caché para desarrollo
        
        // Fallback para recursos estáticos sin el prefijo /inventario
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
          registry.addResourceHandler("/fonts/**")
                .addResourceLocations("classpath:/static/fonts/");
    }
}
