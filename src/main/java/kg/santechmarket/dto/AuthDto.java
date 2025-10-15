package kg.santechmarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для авторизации и аутентификации
 */
public class AuthDto {

    /**
     * DTO для входа в систему
     */
    @Schema(description = "Запрос на вход в систему")
    public record LoginRequest(
            @NotBlank(message = "Логин не может быть пустым")
            @Size(min = 3, max = 50, message = "Логин должен содержать от 3 до 50 символов")
            @Schema(description = "Логин пользователя", example = "admin", minLength = 3, maxLength = 50)
            String username,

            @NotBlank(message = "Пароль не может быть пустым")
            @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
            @Schema(description = "Пароль пользователя", example = "password123", minLength = 6)
            String password
    ) {
    }

    /**
     * DTO для ответа при успешной авторизации
     */
    @Schema(description = "Ответ при успешной авторизации")
    public record LoginResponse(
            @Schema(description = "Access токен для авторизации запросов", example = "eyJhbGciOiJIUzUxMiJ9...")
            String accessToken,

            @Schema(description = "Refresh токен для обновления access токена", example = "550e8400-e29b-41d4-a716-446655440000")
            String refreshToken,

            @Schema(description = "Тип токена", example = "Bearer")
            String tokenType,

            @Schema(description = "Время жизни access токена в секундах", example = "86400")
            Long expiresIn,

            @Schema(description = "Информация о пользователе")
            UserInfo user
    ) {
    }

    /**
     * DTO для запроса обновления токена
     */
    @Schema(description = "Запрос на обновление access токена")
    public record RefreshTokenRequest(
            @NotBlank(message = "Refresh токен не может быть пустым")
            @Schema(description = "Refresh токен", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            String refreshToken
    ) {
    }

    /**
     * DTO для ответа при обновлении токена
     */
    @Schema(description = "Ответ при обновлении токена")
    public record RefreshTokenResponse(
            @Schema(description = "Новый access токен", example = "eyJhbGciOiJIUzUxMiJ9...")
            String accessToken,

            @Schema(description = "Новый refresh токен", example = "550e8400-e29b-41d4-a716-446655440000")
            String refreshToken,

            @Schema(description = "Тип токена", example = "Bearer")
            String tokenType,

            @Schema(description = "Время жизни access токена в секундах", example = "86400")
            Long expiresIn
    ) {
    }

    /**
     * DTO с информацией о пользователе
     */
    @Schema(description = "Информация о пользователе")
    public record UserInfo(
            @Schema(description = "ID пользователя", example = "1")
            Long id,

            @Schema(description = "Логин пользователя", example = "admin")
            String username,

            @Schema(description = "ФИО или название компании", example = "Иванов Иван Иванович")
            String fullName,

            @Schema(description = "Email адрес", example = "user@example.com")
            String email,

            @Schema(description = "Номер телефона в формате +996XXXXXXXXX", example = "+996700123456")
            String phoneNumber,

            @Schema(description = "Роль пользователя", example = "CLIENT", allowableValues = {"CLIENT", "MANAGER", "ADMIN"})
            String role,

            @Schema(description = "Активен ли пользователь", example = "true")
            boolean isActive
    ) {
    }

    /**
     * DTO для обновления профиля пользователя
     */
    @Schema(description = "Запрос на обновление профиля пользователя")
    public record UpdateProfileRequest(
            @NotBlank(message = "Имя не может быть пустым")
            @Size(max = 100, message = "Имя не может превышать 100 символов")
            @Schema(description = "ФИО или название компании", example = "Иванов Иван Иванович", required = true, maxLength = 100)
            String fullName,

            @jakarta.validation.constraints.Email(message = "Некорректный формат email")
            @Schema(description = "Email адрес", example = "user@example.com")
            String email,

            @NotBlank(message = "Номер телефона не может быть пустым")
            @jakarta.validation.constraints.Pattern(
                    regexp = "^\\+996\\d{9}$",
                    message = "Номер телефона должен быть в формате +996XXXXXXXXX"
            )
            @Schema(description = "Номер телефона в формате +996XXXXXXXXX", example = "+996700123456", required = true, pattern = "^\\+996\\d{9}$")
            String phoneNumber
    ) {
    }

    /**
     * DTO для смены пароля
     */
    @Schema(description = "Запрос на смену пароля")
    public record ChangePasswordRequest(
            @NotBlank(message = "Текущий пароль не может быть пустым")
            @Schema(description = "Текущий пароль", example = "oldpassword", required = true)
            String currentPassword,

            @NotBlank(message = "Новый пароль не может быть пустым")
            @Size(min = 6, message = "Новый пароль должен содержать минимум 6 символов")
            @Schema(description = "Новый пароль (минимум 6 символов)", example = "newpassword123", required = true, minLength = 6)
            String newPassword
    ) {
    }

    /**
     * DTO для регистрации нового пользователя
     */
    @Schema(description = "Запрос на регистрацию нового пользователя")
    public record RegisterRequest(
            @NotBlank(message = "Логин не может быть пустым")
            @Size(min = 3, max = 50, message = "Логин должен содержать от 3 до 50 символов")
            @Schema(description = "Логин пользователя (3-50 символов)", example = "ivanov_ivan", required = true, minLength = 3, maxLength = 50)
            String username,

            @NotBlank(message = "Пароль не может быть пустым")
            @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
            @Schema(description = "Пароль (минимум 6 символов)", example = "password123", required = true, minLength = 6)
            String password,

            @NotBlank(message = "ФИО/Название компании не может быть пустым")
            @Size(max = 100, message = "ФИО/Название компании не может превышать 100 символов")
            @Schema(description = "ФИО или название компании", example = "Иванов Иван Иванович", required = true, maxLength = 100)
            String fullName,

            @NotBlank(message = "Номер телефона не может быть пустым")
            @jakarta.validation.constraints.Pattern(
                    regexp = "^\\+996\\d{9}$",
                    message = "Номер телефона должен быть в формате +996XXXXXXXXX"
            )
            @Schema(description = "Номер телефона в формате +996XXXXXXXXX", example = "+996700123456", required = true, pattern = "^\\+996\\d{9}$")
            String phoneNumber,

            @jakarta.validation.constraints.Email(message = "Некорректный формат email")
            @Schema(description = "Email адрес (опционально)", example = "user@example.com")
            String email
    ) {
    }

    /**
     * DTO для ответа при успешной регистрации
     */
    @Schema(description = "Ответ при успешной регистрации")
    public record RegisterResponse(
            @Schema(description = "ID созданного пользователя", example = "1")
            Long id,

            @Schema(description = "Логин пользователя", example = "ivanov_ivan")
            String username,

            @Schema(description = "ФИО или название компании", example = "Иванов Иван Иванович")
            String fullName,

            @Schema(description = "Номер телефона", example = "+996700123456")
            String phoneNumber,

            @Schema(description = "Email адрес", example = "user@example.com")
            String email,

            @Schema(description = "Статус пользователя", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
            String status,

            @Schema(description = "Сообщение для пользователя", example = "Регистрация успешна! Ваша заявка на рассмотрении.")
            String message
    ) {
    }
}