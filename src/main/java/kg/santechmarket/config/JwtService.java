package kg.santechmarket.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import kg.santechmarket.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Сервис для работы с JWT токенами
 *
 * Основные функции:
 * - Генерация JWT токенов
 * - Валидация токенов
 * - Извлечение данных из токенов
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;
    private final long jwtExpirationMs;

    public JwtService(@Value("${spring.security.jwt.secret-key}") String secretKeyString,
                      @Value("${spring.security.jwt.expiration}") long jwtExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Генерация JWT токена для пользователя
     */
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtExpirationMs, ChronoUnit.MILLIS);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("fullName", user.getFullName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Генерация токена из Authentication объекта
     */
    public String generateToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return generateToken(user);
    }

    /**
     * Извлечение логина пользователя из токена
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Извлечение ID пользователя из токена
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Извлечение роли пользователя из токена
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Проверка валидности токена
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Некорректный JWT токен: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT токен истек: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Неподдерживаемый JWT токен: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims пустой: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка валидации JWT токена: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Проверка истекшего токена
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Получение времени истечения токена в миллисекундах
     */
    public long getExpirationTimeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration().getTime();
    }

    /**
     * Получение времени действия токена (в секундах от текущего момента)
     */
    public long getTimeToExpiration(String token) {
        long expirationTime = getExpirationTimeFromToken(token);
        long currentTime = System.currentTimeMillis();
        return Math.max(0, (expirationTime - currentTime) / 1000);
    }

    /**
     * Извлечение claims из токена
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Получить время жизни токена в миллисекундах (для ответа клиенту)
     */
    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}