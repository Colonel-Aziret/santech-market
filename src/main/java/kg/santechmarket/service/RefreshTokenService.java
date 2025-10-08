package kg.santechmarket.service;

import kg.santechmarket.entity.RefreshToken;
import kg.santechmarket.entity.User;
import kg.santechmarket.enums.ErrorCode;
import kg.santechmarket.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с refresh токенами
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${spring.security.jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    /**
     * Создание нового refresh токена для пользователя
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Удаляем старые токены пользователя
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.debug("Создан refresh токен для пользователя: {}", user.getUsername());

        return refreshToken;
    }

    /**
     * Поиск refresh токена по значению
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Проверка валидности refresh токена
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh токен истёк. Пожалуйста, выполните вход заново");
        }
        return token;
    }

    /**
     * Удаление refresh токена
     */
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Удаление всех токенов пользователя
     */
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
        log.debug("Удалены все refresh токены пользователя: {}", user.getUsername());
    }

    /**
     * Очистка истекших токенов (для scheduled задачи)
     */
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens();
        log.debug("Удалены истекшие refresh токены");
    }
}
