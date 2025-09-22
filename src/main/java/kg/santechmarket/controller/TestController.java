package kg.santechmarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final PasswordEncoder passwordEncoder;

    @PostMapping("/hash")
    public String testHash(@RequestParam String password) {
        String existingHash = "$2a$12$yI8Kqm8lJUgMvjKVYlCMHOZpMl2OTVO3JmKK/OYSVfIGc/4I8VGFK";
        boolean matches = passwordEncoder.matches(password, existingHash);
        String newHash = passwordEncoder.encode(password);

        return String.format(
            "Password: %s\n" +
            "Matches existing hash: %s\n" +
            "New hash: %s\n" +
            "Existing hash: %s",
            password, matches, newHash, existingHash
        );
    }
}