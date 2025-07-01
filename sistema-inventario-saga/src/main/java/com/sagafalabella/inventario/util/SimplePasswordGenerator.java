package com.sagafalabella.inventario.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SimplePasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generar hash para "password123"
        String password = "password123";
        String hash = encoder.encode(password);
        
        System.out.println("=== HASH GENERADO ===");
        System.out.println("Contraseña: " + password);
        System.out.println("Hash: " + hash);
        
        // Verificar que funciona
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verificación: " + (matches ? "✅ CORRECTO" : "❌ ERROR"));
        
        // Probar también con "admin123"
        String password2 = "admin123";
        String hash2 = encoder.encode(password2);
        
        System.out.println("\n=== HASH GENERADO ===");
        System.out.println("Contraseña: " + password2);
        System.out.println("Hash: " + hash2);
        
        boolean matches2 = encoder.matches(password2, hash2);
        System.out.println("Verificación: " + (matches2 ? "✅ CORRECTO" : "❌ ERROR"));
    }
}
