package kg.santechmarket.config;

import kg.santechmarket.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Конфигурация безопасности Spring Security
 * <p>
 * Настройки:
 * - JWT аутентификация
 * - Stateless сессии
 * - Публичные и защищенные endpoints
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;

    /**
     * Настройка цепочки фильтров безопасности
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(UserService userService) {
        return new JwtAuthenticationFilter(jwtService, userService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource,
                                            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Публичные endpoints
                        .requestMatchers("/auth/login", "/auth/register", "/auth/refresh", "/auth/forgot-password", "/auth/reset-password").permitAll()
                        .requestMatchers("/categories/**").permitAll()
                        .requestMatchers("/products/**").permitAll()
                        .requestMatchers("/promo-banners/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/test/**").permitAll()

                        // Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/v3/api-docs/**").permitAll()

                        // Actuator endpoints
                        .requestMatchers("/actuator/health").permitAll()

                        // Все остальные требуют аутентификации
                        .anyRequest().authenticated()
                )
                // Добавляем JWT фильтр перед UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Encoder для хеширования паролей
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * AuthenticationManager для процесса аутентификации
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}