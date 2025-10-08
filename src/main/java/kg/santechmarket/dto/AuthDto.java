package kg.santechmarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для авторизации и аутентификации
 */
public class AuthDto {

    /**
     * DTO для входа в систему
     */
    public record LoginRequest(
            @NotBlank(message = "Логин не может быть пустым")
            @Size(min = 3, max = 50, message = "Логин должен содержать от 3 до 50 символов")
            String username,

            @NotBlank(message = "Пароль не может быть пустым")
            @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
            String password
    ) {
    }

    /**
     * DTO для ответа при успешной авторизации
     */
    public record LoginResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            UserInfo user
    ) {
    }

    /**
     * DTO для запроса обновления токена
     */
    public record RefreshTokenRequest(
            @NotBlank(message = "Refresh токен не может быть пустым")
            String refreshToken
    ) {
    }

    /**
     * DTO для ответа при обновлении токена
     */
    public record RefreshTokenResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn
    ) {
    }

    /**
     * DTO с информацией о пользователе
     */
    public record UserInfo(
            Long id,
            String username,
            String fullName,
            String email,
            String phoneNumber,
            String role,
            boolean isActive
    ) {
    }

    /**
     * DTO для обновления профиля пользователя
     */
    public record UpdateProfileRequest(
            @NotBlank(message = "Имя не может быть пустым")
            @Size(max = 100, message = "Имя не может превышать 100 символов")
            String fullName,

            String email,
            String phoneNumber
    ) {
    }

    /**
     * DTO для смены пароля
     */
    public record ChangePasswordRequest(
            @NotBlank(message = "Текущий пароль не может быть пустым")
            String currentPassword,

            @NotBlank(message = "Новый пароль не может быть пустым")
            @Size(min = 6, message = "Новый пароль должен содержать минимум 6 символов")
            String newPassword
    ) {
    }

    /**
     * DTO для регистрации нового пользователя
     */
    public record RegisterRequest(
            @NotBlank(message = "Логин не может быть пустым")
            @Size(min = 3, max = 50, message = "Логин должен содержать от 3 до 50 символов")
            String username,

            @NotBlank(message = "Пароль не может быть пустым")
            @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
            String password,

            @NotBlank(message = "ФИО/Название компании не может быть пустым")
            @Size(max = 100, message = "ФИО/Название компании не может превышать 100 символов")
            String fullName,

            @NotBlank(message = "Номер телефона не может быть пустым")
            @jakarta.validation.constraints.Pattern(
                    regexp = "^\\+996\\d{9}$",
                    message = "Номер телефона должен быть в формате +996XXXXXXXXX"
            )
            String phoneNumber,

            @jakarta.validation.constraints.Email(message = "Некорректный формат email")
            String email
    ) {
    }

    /**
     * DTO для ответа при успешной регистрации
     */
    public record RegisterResponse(
            Long id,
            String username,
            String fullName,
            String phoneNumber,
            String email,
            String status,
            String message
    ) {
    }
}