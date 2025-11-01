package kg.santechmarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Универсальный формат ответа с сообщением для API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ с сообщением")
public class MessageResponse {

    @Schema(description = "Сообщение", example = "Операция выполнена успешно")
    private String message;

    @Schema(description = "Временная метка")
    private LocalDateTime timestamp;

    public MessageResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
