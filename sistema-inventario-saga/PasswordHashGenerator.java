import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generar hash para cliente123
        String password = "cliente123";
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        // Verificar que el hash funciona
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification: " + matches);
        
        // También probemos con admin123
        String adminPassword = "admin123";
        String adminHash = "$2a$10$pahDQoVikfbl3Vvlq8yVOOhjBl2R0sbM4iPKNKjG2R5kTiFTpws8C";
        boolean adminMatches = encoder.matches(adminPassword, adminHash);
        System.out.println("Admin verification: " + adminMatches);
    }
}
