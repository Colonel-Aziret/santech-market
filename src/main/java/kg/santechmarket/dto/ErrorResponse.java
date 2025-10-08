package kg.santechmarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Единый формат ответа с ошибкой для всего API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ с ошибкой")
public class ErrorResponse {

    @Schema(description = "Сообщение об ошибке (для фронтенда)", example = "Неверный логин или пароль")
    private String message;

    @Schema(description = "Код ошибки (для бэкенда)", example = "AUTH_INVALID_CREDENTIALS")
    private String code;

    @Schema(description = "Временная метка ошибки")
    private LocalDateTime timestamp;

    public ErrorResponse(String message, String code) {
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }
}
