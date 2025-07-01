package com.sagafalabella.inventario.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.sagafalabella.inventario.config.AppProperties;

/**
 * Servicio para gestión de imágenes de productos
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Service
public class ImagenService {

    @Autowired
    private AppProperties appProperties;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};

    /**
     * Guarda una imagen de producto
     */
    public String guardarImagen(MultipartFile archivo) throws IOException {
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Validar tamaño
        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo es demasiado grande. Máximo 5MB");
        }

        // Validar extensión
        String extension = getFileExtension(archivo.getOriginalFilename());
        if (!isValidExtension(extension)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Solo se permiten: jpg, jpeg, png, gif, webp");
        }

        // Crear directorio si no existe
        Path uploadPath = Paths.get(appProperties.getUpload().getDir());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generar nombre único
        String nombreArchivo = UUID.randomUUID().toString() + "." + extension;
        Path targetPath = uploadPath.resolve(nombreArchivo);

        // Guardar archivo
        Files.copy(archivo.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return nombreArchivo;
    }

    /**
     * Elimina una imagen
     */
    public void eliminarImagen(String nombreArchivo) {
        if (StringUtils.hasText(nombreArchivo)) {
            try {
                Path path = Paths.get(appProperties.getUpload().getDir()).resolve(nombreArchivo);
                Files.deleteIfExists(path);
            } catch (IOException e) {
                // Log error but don't throw exception
                System.err.println("Error al eliminar imagen: " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene la URL de una imagen
     */
    public String getImagenUrl(String nombreArchivo) {
        if (!StringUtils.hasText(nombreArchivo)) {
            return "/images/product-placeholder.png"; // Imagen por defecto
        }
        return "/uploads/" + nombreArchivo;
    }

    /**
     * Valida si la extensión del archivo es permitida
     */
    private boolean isValidExtension(String extension) {
        if (!StringUtils.hasText(extension)) {
            return false;
        }
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene la extensión del archivo
     */
    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex + 1) : "";
    }

    /**
     * Inicializar directorio de uploads
     */
    public void inicializarDirectorio() {
        try {
            Path uploadPath = Paths.get(appProperties.getUpload().getDir());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads", e);
        }
    }
}
