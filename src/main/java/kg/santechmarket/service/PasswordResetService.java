package kg.santechmarket.service;

import kg.santechmarket.entity.PasswordResetToken;
import kg.santechmarket.entity.User;
import kg.santechmarket.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

/**
 * Сервис для работы с токенами сброса пароля
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final NotificationService notificationService;
    private final SmsService smsService;

    @Value("${app.password-reset.expiration-minutes:15}")
    private Integer expirationMinutes;

    /**
     * Создание токена сброса пароля для пользователя
     */
    @Transactional
    public PasswordResetToken createPasswordResetToken(User user) {
        // Удаляем старые токены пользователя
        passwordResetTokenRepository.deleteByUser(user);

        // Генерируем 6-значный код
        String token = generateSixDigitCode();

        // Создаем токен с временем истечения
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(Instant.now().plusSeconds(expirationMinutes * 60L))
                .isUsed(false)
                .build();

        resetToken = passwordResetTokenRepository.save(resetToken);
        log.info("Создан токен сброса пароля для пользователя: {} (действителен {} минут)",
                user.getUsername(), expirationMinutes);

        // Формируем сообщение с кодом
        String message = String.format("Ваш код для сброса пароля: %s. Код действителен %d минут.",
                token, expirationMinutes);

        // Отправляем SMS, если сервис включен
        if (smsService.isEnabled()) {
            try {
                boolean smsSent = smsService.sendSms(user.getPhoneNumber(), message, resetToken.getId().toString());
                if (smsSent) {
                    log.info("SMS с кодом сброса пароля отправлено на номер: {}",
                            maskPhoneNumber(user.getPhoneNumber()));
                } else {
                    log.warn("Не удалось отправить SMS, создаём внутреннее уведомление");
                    createFallbackNotification(user, message);
                }
            } catch (Exception e) {
                log.error("Ошибка при отправке SMS: {}", e.getMessage());
                createFallbackNotification(user, message);
            }
        } else {
            // Если SMS сервис отключен, создаём внутреннее уведомление
            log.info("SMS сервис отключен, создаём внутреннее уведомление");
            createFallbackNotification(user, message);
        }

        return resetToken;
    }

    /**
     * Поиск активного токена по значению
     */
    public Optional<PasswordResetToken> findActiveToken(String token) {
        Optional<PasswordResetToken> resetToken = passwordResetTokenRepository.findByToken(token);

        if (resetToken.isPresent()) {
            PasswordResetToken rt = resetToken.get();

            // Проверяем, не истек ли токен и не использован ли он
            if (rt.getIsUsed()) {
                log.warn("Попытка использования уже использованного токена");
                return Optional.empty();
            }

            if (rt.getExpiryDate().isBefore(Instant.now())) {
                log.warn("Попытка использования истекшего токена");
                passwordResetTokenRepository.delete(rt);
                return Optional.empty();
            }

            return Optional.of(rt);
        }

        return Optional.empty();
    }

    /**
     * Отметить токен как использованный
     */
    @Transactional
    public void markTokenAsUsed(PasswordResetToken token) {
        token.setIsUsed(true);
        passwordResetTokenRepository.save(token);
        log.info("Токен сброса пароля отмечен как использованный для пользователя: {}",
                token.getUser().getUsername());
    }

    /**
     * Удалить все токены пользователя
     */
    @Transactional
    public void deleteByUser(User user) {
        passwordResetTokenRepository.deleteByUser(user);
        log.debug("Удалены все токены сброса пароля пользователя: {}", user.getUsername());
    }

    /**
     * Очистка истекших и использованных токенов (для scheduled задачи)
     */
    @Transactional
    public void deleteExpiredOrUsedTokens() {
        passwordResetTokenRepository.deleteExpiredOrUsedTokens(Instant.now());
        log.debug("Удалены истекшие и использованные токены сброса пароля");
    }

    /**
     * Генерация случайного 6-значного кода
     */
    private String generateSixDigitCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Создание внутреннего уведомления как запасной вариант
     */
    private void createFallbackNotification(User user, String message) {
        notificationService.createNotification(
                user.getId(),
                kg.santechmarket.enums.NotificationType.PASSWORD_RESET,
                "Сброс пароля",
                message,
                null
        );
        log.info("Создано внутреннее уведомление для пользователя: {}", user.getUsername());
    }

    /**
     * Маскировка номера телефона (показываем только последние 3 цифры)
     */
    public String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 7) {
            return phoneNumber;
        }

        String prefix = phoneNumber.substring(0, phoneNumber.length() - 6);
        String suffix = phoneNumber.substring(phoneNumber.length() - 3);
        return prefix + "***" + suffix;
    }
}
