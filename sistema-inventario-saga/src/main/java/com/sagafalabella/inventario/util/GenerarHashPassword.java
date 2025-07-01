package com.sagafalabella.inventario.util;

/**
 * Genera hashes BCrypt para contraseñas de testing
 */
public class GenerarHashPassword {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java GenerarHashPassword <contraseña>");
            return;
        }
        
        String password = args[0];
        
        // Generar hash con BCrypt
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = 
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        
        String hash = encoder.encode(password);
        
        System.out.println("===============================================");
        System.out.println("Contraseña: " + password);
        System.out.println("Hash BCrypt: " + hash);
        System.out.println("===============================================");
        
        // Verificar que el hash funciona
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verificación: " + (matches ? "✅ CORRECTO" : "❌ ERROR"));
    }
}
