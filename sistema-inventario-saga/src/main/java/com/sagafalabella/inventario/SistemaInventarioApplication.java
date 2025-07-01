package com.sagafalabella.inventario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import com.sagafalabella.inventario.config.AppProperties;
import com.sagafalabella.inventario.service.ImagenService;

/**
 * Aplicación principal del Sistema de Gestión de Inventario para Saga Falabella
 * 
 * @author Christopher Lincoln Rafaile Naupay
 * @version 1.0.0
 * @since 2025
 */
@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(AppProperties.class)
public class SistemaInventarioApplication implements CommandLineRunner {

    @Autowired
    private ImagenService imagenService;

    public static void main(String[] args) {
        SpringApplication.run(SistemaInventarioApplication.class, args);
        System.out.println("=================================================");
        System.out.println("Sistema de Gestión de Inventario - Saga Falabella");
        System.out.println("Aplicación iniciada correctamente en puerto 8080");
        System.out.println("URL: http://localhost:8080/inventario");
        System.out.println("=================================================");
    }

    @Override
    public void run(String... args) throws Exception {
        // Inicializar directorio de uploads
        imagenService.inicializarDirectorio();
        System.out.println("Directorio de imágenes inicializado correctamente");
    }
}
