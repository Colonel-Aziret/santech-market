package kg.santechmarket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kg.santechmarket.dto.ErrorResponse;
import kg.santechmarket.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Основной метод фильтрации запросов
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Пропускаем публичные endpoints без проверки токена
            String path = request.getRequestURI();
            if (isPublicEndpoint(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Извлечение JWT токена из заголовка
            String jwt = extractJwtFromRequest(request);

            // Если токен найден
            if (jwt != null) {
                // Проверяем валидность токена
                if (jwtService.validateToken(jwt)) {
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
                } else {
                    // Токен невалиден - проверяем, не истек ли он
                    if (jwtService.isTokenExpired(jwt)) {
                        log.warn("JWT токен истек");
                        sendUnauthorizedResponse(response, "Токен истек, пожалуйста, авторизуйтесь заново", ErrorCode.AUTH_TOKEN_EXPIRED);
                        return;
                    } else {
                        log.warn("JWT токен невалиден");
                        sendUnauthorizedResponse(response, "Невалидный токен", ErrorCode.AUTH_TOKEN_INVALID);
                        return;
                    }
                }
            }
        } catch (ExpiredJwtException e) {
            log.warn("JWT токен истек: {}", e.getMessage());
            sendUnauthorizedResponse(response, "Токен истек, пожалуйста, авторизуйтесь заново", ErrorCode.AUTH_TOKEN_EXPIRED);
            return;
        } catch (Exception e) {
            log.error("Ошибка установки аутентификации пользователя: {}", e.getMessage());
            sendUnauthorizedResponse(response, "Ошибка аутентификации", ErrorCode.AUTH_UNAUTHORIZED);
            return;
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

    /**
     * Отправка ответа 401 Unauthorized с JSON телом
     *
     * @param response HTTP ответ
     * @param message  Сообщение об ошибке
     * @param errorCode Код ошибки
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message, ErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = new ErrorResponse(message, errorCode.getCode());
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
    }

    /**
     * Проверка, является ли endpoint публичным (не требует аутентификации)
     *
     * @param path URI запроса
     * @return true если endpoint публичный
     */
    private boolean isPublicEndpoint(String path) {
        // Удаляем контекстный путь /api/v1 если есть
        String cleanPath = path.replaceFirst("^/api/v1", "");

        return cleanPath.equals("/auth/login") ||
               cleanPath.equals("/auth/register") ||
               cleanPath.equals("/auth/refresh") ||
               cleanPath.startsWith("/categories") ||
               cleanPath.startsWith("/products") ||
               cleanPath.startsWith("/promo-banners") ||
               cleanPath.startsWith("/test") ||
               cleanPath.startsWith("/swagger-ui") ||
               cleanPath.startsWith("/api-docs") ||
               cleanPath.startsWith("/v3/api-docs") ||
               cleanPath.equals("/actuator/health");
    }
}
