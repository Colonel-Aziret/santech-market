package kg.santechmarket.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        String existingHash = "$2a$12$yI8Kqm8lJUgMvjKVYlCMHOZpMl2OTVO3JmKK/OYSVfIGc/4I8VGFK";

        // Тестируем различные пароли против существующего хеша
        String[] passwords = {"admin123", "client123", "manager123", "password", "123456", "password123"};

        System.out.println("=== Тестирование существующего хеша ===");
        System.out.println("Hash: " + existingHash);

        for (String password : passwords) {
            boolean matches = encoder.matches(password, existingHash);
            System.out.println("Password '" + password + "': " + matches);
        }

        System.out.println("\n=== Генерация новых хешей ===");
        System.out.println("admin123: " + encoder.encode("admin123"));
        System.out.println("manager123: " + encoder.encode("manager123"));
        System.out.println("client123: " + encoder.encode("client123"));
    }
}