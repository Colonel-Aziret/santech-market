package kg.santechmarket.service;

import kg.santechmarket.entity.User;
import kg.santechmarket.entity.VerificationCode;
import kg.santechmarket.enums.VerificationType;
import kg.santechmarket.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

/**
 * Сервис для работы с кодами верификации
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;

    @Value("${app.verification.expiration-minutes:15}")
    private Integer expirationMinutes;

    @Value("${app.verification.max-attempts:5}")
    private Integer maxAttempts;

    /**
     * Создание кода верификации для смены email
     */
    @Transactional
    public VerificationCode createEmailChangeCode(User user, String newEmail) {
        // Удаляем старые коды этого типа для пользователя
        verificationCodeRepository.deleteByUserAndType(user, VerificationType.EMAIL_CHANGE);

        // Генерируем 6-значный код
        String code = generateSixDigitCode();

        // Создаем код верификации
        VerificationCode verificationCode = VerificationCode.builder()
                .user(user)
                .code(code)
                .type(VerificationType.EMAIL_CHANGE)
                .targetEmail(newEmail)
                .newValue(newEmail)
                .expiryDate(Instant.now().plusSeconds(expirationMinutes * 60L))
                .isUsed(false)
                .attemptCount(0)
                .build();

        verificationCode = verificationCodeRepository.save(verificationCode);
        log.info("Создан код верификации для смены email пользователя: {} (действителен {} минут)",
                user.getUsername(), expirationMinutes);

        // Отправляем код на новый email
        try {
            emailService.sendEmailChangeVerificationCode(newEmail, code, user.getFullName());
            log.info("Код верификации отправлен на новый email: {}", maskEmail(newEmail));
        } catch (Exception e) {
            log.error("Ошибка при отправке email с кодом верификации: {}", e.getMessage());
            throw new RuntimeException("Не удалось отправить код верификации на email");
        }

        return verificationCode;
    }

    /**
     * Создание кода верификации для смены телефона
     */
    @Transactional
    public VerificationCode createPhoneChangeCode(User user, String newPhone) {
        // Удаляем старые коды этого типа для пользователя
        verificationCodeRepository.deleteByUserAndType(user, VerificationType.PHONE_CHANGE);

        // Генерируем 6-значный код
        String code = generateSixDigitCode();

        // Создаем код верификации
        VerificationCode verificationCode = VerificationCode.builder()
                .user(user)
                .code(code)
                .type(VerificationType.PHONE_CHANGE)
                .targetEmail(user.getEmail())
                .newValue(newPhone)
                .expiryDate(Instant.now().plusSeconds(expirationMinutes * 60L))
                .isUsed(false)
                .attemptCount(0)
                .build();

        verificationCode = verificationCodeRepository.save(verificationCode);
        log.info("Создан код верификации для смены телефона пользователя: {} (действителен {} минут)",
                user.getUsername(), expirationMinutes);

        // Отправляем код на текущий email
        try {
            emailService.sendPhoneChangeVerificationCode(user.getEmail(), code, user.getFullName(), newPhone);
            log.info("Код верификации отправлен на email: {}", maskEmail(user.getEmail()));
        } catch (Exception e) {
            log.error("Ошибка при отправке email с кодом верификации: {}", e.getMessage());
            throw new RuntimeException("Не удалось отправить код верификации на email");
        }

        return verificationCode;
    }

    /**
     * Создание кода верификации для сброса пароля через email
     */
    @Transactional
    public VerificationCode createPasswordResetCode(User user) {
        // Удаляем старые коды этого типа для пользователя
        verificationCodeRepository.deleteByUserAndType(user, VerificationType.PASSWORD_RESET);

        // Генерируем 6-значный код
        String code = generateSixDigitCode();

        // Создаем код верификации
        VerificationCode verificationCode = VerificationCode.builder()
                .user(user)
                .code(code)
                .type(VerificationType.PASSWORD_RESET)
                .targetEmail(user.getEmail())
                .expiryDate(Instant.now().plusSeconds(expirationMinutes * 60L))
                .isUsed(false)
                .attemptCount(0)
                .build();

        verificationCode = verificationCodeRepository.save(verificationCode);
        log.info("Создан код верификации для сброса пароля пользователя: {} (действителен {} минут)",
                user.getUsername(), expirationMinutes);

        // Отправляем код на email
        try {
            emailService.sendPasswordResetVerificationCode(user.getEmail(), code, user.getFullName());
            log.info("Код сброса пароля отправлен на email: {}", maskEmail(user.getEmail()));
        } catch (Exception e) {
            log.error("Ошибка при отправке email с кодом верификации: {}", e.getMessage());
            throw new RuntimeException("Не удалось отправить код верификации на email");
        }

        return verificationCode;
    }

    /**
     * Проверка и использование кода верификации
     */
    @Transactional
    public VerificationCode verifyCode(String code, User user, VerificationType type) {
        Optional<VerificationCode> verificationCodeOpt = verificationCodeRepository
                .findByCodeAndUserAndType(code, user, type, Instant.now());

        if (verificationCodeOpt.isEmpty()) {
            log.warn("Неверный или истекший код верификации для пользователя: {}", user.getUsername());
            throw new IllegalArgumentException("Неверный или истекший код верификации");
        }

        VerificationCode verificationCode = verificationCodeOpt.get();

        // Проверяем количество попыток
        if (verificationCode.getAttemptCount() >= maxAttempts) {
            log.warn("Превышено количество попыток для кода верификации пользователя: {}", user.getUsername());
            verificationCodeRepository.delete(verificationCode);
            throw new IllegalArgumentException("Превышено количество попыток. Запросите новый код.");
        }

        // Увеличиваем счетчик попыток
        verificationCode.setAttemptCount(verificationCode.getAttemptCount() + 1);

        // Если код не совпадает
        if (!verificationCode.getCode().equals(code)) {
            verificationCodeRepository.save(verificationCode);
            log.warn("Неверный код верификации для пользователя: {} (попытка {}/{})",
                    user.getUsername(), verificationCode.getAttemptCount(), maxAttempts);
            throw new IllegalArgumentException("Неверный код верификации");
        }

        // Отмечаем код как использованный
        verificationCode.setIsUsed(true);
        verificationCodeRepository.save(verificationCode);

        log.info("Код верификации успешно подтвержден для пользователя: {}", user.getUsername());
        return verificationCode;
    }

    /**
     * Проверка существования активного кода
     */
    public boolean hasActiveCode(User user, VerificationType type) {
        return verificationCodeRepository.existsActiveCodeByUserAndType(user, type, Instant.now());
    }

    /**
     * Получение активного кода
     */
    public Optional<VerificationCode> getActiveCode(User user, VerificationType type) {
        return verificationCodeRepository.findActiveCodeByUserAndType(user, type, Instant.now());
    }

    /**
     * Удаление всех кодов пользователя
     */
    @Transactional
    public void deleteByUser(User user) {
        verificationCodeRepository.deleteByUser(user);
        log.debug("Удалены все коды верификации пользователя: {}", user.getUsername());
    }

    /**
     * Очистка истекших и использованных кодов (для scheduled задачи)
     */
    @Transactional
    public void deleteExpiredOrUsedCodes() {
        verificationCodeRepository.deleteExpiredOrUsedCodes(Instant.now());
        log.debug("Удалены истекшие и использованные коды верификации");
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
     * Маскировка email (показываем только первые 2 символа и домен)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return "**@" + domain;
        }

        return localPart.substring(0, 2) + "***@" + domain;
    }
}
