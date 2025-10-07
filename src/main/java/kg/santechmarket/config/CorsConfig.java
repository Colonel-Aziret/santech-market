package kg.santechmarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Конфигурация CORS (Cross-Origin Resource Sharing)
 * <p>
 * Разрешает запросы с фронтенд приложения на localhost:3000
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Разрешенные origins
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // Разрешенные HTTP методы
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Разрешенные заголовки
        configuration.setAllowedHeaders(List.of("*"));

        // Разрешить отправку credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Максимальное время кеширования preflight запроса
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
