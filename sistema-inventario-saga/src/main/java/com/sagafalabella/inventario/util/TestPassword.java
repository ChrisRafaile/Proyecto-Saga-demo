package com.sagafalabella.inventario.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password123";
        String storedHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";
        
        System.out.println("Contraseña a verificar: " + rawPassword);
        System.out.println("Hash almacenado: " + storedHash);
        
        boolean matches = encoder.matches(rawPassword, storedHash);
        System.out.println("¿Coincide? " + matches);
        
        // Probar con otras contraseñas comunes
        String[] passwords = {"password", "123456", "admin", "test", "password123"};
        for (String pwd : passwords) {
            boolean match = encoder.matches(pwd, storedHash);
            System.out.println("Probando '" + pwd + "': " + match);
        }
    }
}
