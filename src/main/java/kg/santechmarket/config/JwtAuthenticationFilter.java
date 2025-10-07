package kg.santechmarket.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр для проверки JWT токена в каждом запросе
 * <p>
 * Извлекает JWT из заголовка Authorization, валидирует его
 * и устанавливает аутентификацию в SecurityContext
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Основной метод фильтрации запросов
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Извлечение JWT токена из заголовка
            String jwt = extractJwtFromRequest(request);

            // Если токен найден и валиден
            if (jwt != null && jwtService.validateToken(jwt)) {
                // Извлекаем username из токена
                String username = jwtService.getUsernameFromToken(jwt);

                // Загружаем пользователя из базы данных
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Создаем объект аутентификации
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Устанавливаем детали запроса
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Устанавливаем аутентификацию в SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Установлена аутентификация для пользователя: {}", username);
            }
        } catch (Exception e) {
            log.error("Ошибка установки аутентификации пользователя: {}", e.getMessage());
        }

        // Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Извлечение JWT токена из заголовка Authorization
     *
     * @param request HTTP запрос
     * @return JWT токен или null, если токен отсутствует
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Проверяем формат: "Bearer <token>"
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Убираем "Bearer " (7 символов)
        }

        return null;
    }
}
