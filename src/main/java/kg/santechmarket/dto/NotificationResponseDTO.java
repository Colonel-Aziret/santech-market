package kg.santechmarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kg.santechmarket.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для ответа с уведомлением
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Уведомление пользователя")
public class NotificationResponseDTO {

    @Schema(description = "ID уведомления", example = "1")
    private Long id;

    @Schema(description = "Тип уведомления", example = "ORDER_UPDATE")
    private NotificationType type;

    @Schema(description = "Заголовок уведомления", example = "Заказ оформлен")
    private String title;

    @Schema(description = "Содержимое уведомления", example = "Ваш заказ ORD-20251020-123 успешно оформлен")
    private String content;

    @Schema(description = "Прочитано ли уведомление", example = "false")
    private Boolean isRead;

    @Schema(description = "Дата прочтения уведомления", example = "2025-10-20T14:45:00")
    private LocalDateTime readAt;

    @Schema(description = "Дополнительные данные уведомления (JSON)", example = "{\"order_id\": 123}")
    private String metadata;

    @Schema(description = "Дата создания уведомления", example = "2025-10-20T12:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Дата последнего обновления уведомления", example = "2025-10-20T14:45:00")
    private LocalDateTime updatedAt;
}
