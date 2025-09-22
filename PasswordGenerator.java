import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        System.out.println("admin123: " + encoder.encode("admin123"));
        System.out.println("manager123: " + encoder.encode("manager123"));
        System.out.println("client123: " + encoder.encode("client123"));
    }
}