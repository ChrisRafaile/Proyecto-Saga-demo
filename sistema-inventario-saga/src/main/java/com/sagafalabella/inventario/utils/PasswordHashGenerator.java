package com.sagafalabella.inventario.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad para generar hashes BCrypt de contraseñas
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password123";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("Contraseña original: " + rawPassword);
        System.out.println("Hash BCrypt: " + encodedPassword);
        
        // Verificar que el hash funciona
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("Verificación: " + matches);
        
        // Generar varios hashes para comparar
        System.out.println("\nGenerando múltiples hashes:");
        for (int i = 0; i < 3; i++) {
            String hash = encoder.encode(rawPassword);
            boolean verify = encoder.matches(rawPassword, hash);
            System.out.println("Hash " + (i+1) + ": " + hash + " | Verificación: " + verify);
        }
    }
}
