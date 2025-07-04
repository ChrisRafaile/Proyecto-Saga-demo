package com.sagafalabella.inventario.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor para agregar meta información común a todas las páginas
 */
@Configuration
public class FaviconInterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new FaviconInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/css/**", "/js/**", "/images/**",
                                   "/favicon.ico", "/favicon.svg", "/favicon-16.svg");
    }

    private static class FaviconInterceptor implements HandlerInterceptor {
        
        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler, ModelAndView modelAndView) throws Exception {
            
            if (modelAndView != null && modelAndView.getViewName() != null) {
                // Agregar información común que puede ser útil
                modelAndView.addObject("faviconVersion", "v2");
                modelAndView.addObject("appVersion", "1.0.0");
                
                // Agregar headers para prevenir cache en páginas dinámicas
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
            }
        }
    }
}
