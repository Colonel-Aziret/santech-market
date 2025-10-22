package kg.santechmarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kg.santechmarket.enums.UserRole;

/**
 * DTO для операций с пользователями
 */
public class UserDto {

    /**
     * DTO для обновления пользователя (частичное обновление)
     * Все поля опциональны - обновляются только переданные поля
     */
    @Schema(description = "Запрос на обновление пользователя")
    public record UpdateUserRequest(
            @Size(min = 3, max = 50, message = "Логин должен содержать от 3 до 50 символов")
            @Schema(description = "Логин пользователя", example = "admin", minLength = 3, maxLength = 50)
            String username,

            @Size(max = 100, message = "Имя не может превышать 100 символов")
            @Schema(description = "ФИО или название компании", example = "Иванов Иван Иванович", maxLength = 100)
            String fullName,

            @Email(message = "Некорректный формат email")
            @Schema(description = "Email адрес", example = "user@example.com")
            String email,

            @Pattern(
                    regexp = "^\\+996\\d{9}$",
                    message = "Номер телефона должен быть в формате +996XXXXXXXXX"
            )
            @Schema(description = "Номер телефона в формате +996XXXXXXXXX", example = "+996700123456", pattern = "^\\+996\\d{9}$")
            String phoneNumber,

            @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
            @Schema(description = "Новый пароль (если нужно изменить)", example = "newpassword123", minLength = 6)
            String password,

            @Schema(description = "Роль пользователя", example = "CLIENT", allowableValues = {"CLIENT", "MANAGER", "ADMIN"})
            UserRole role,

            @Schema(description = "Активен ли пользователь", example = "true")
            Boolean isActive
    ) {
    }
}
