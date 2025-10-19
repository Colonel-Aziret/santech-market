package kg.santechmarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kg.santechmarket.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для ответа с заказом
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Заказ пользователя")
public class OrderResponseDTO {

    @Schema(description = "ID заказа", example = "1")
    private Long id;

    @Schema(description = "Уникальный номер заказа", example = "ORD-20251020-A1B2C3D4")
    private String orderNumber;

    @Schema(description = "ID пользователя", example = "10")
    private Long userId;

    @Schema(description = "Имя пользователя", example = "Иван Иванов")
    private String userName;

    @Schema(description = "Статус заказа", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "Общая сумма заказа в сомах", example = "120500.00")
    private BigDecimal totalAmount;

    @Schema(description = "Общее количество товаров", example = "15")
    private Integer totalItems;

    @Schema(description = "Комментарий клиента", example = "Доставка до 18:00")
    private String customerComment;

    @Schema(description = "Комментарий менеджера", example = "Заказ подтвержден, готовится к отправке")
    private String managerComment;

    @Schema(description = "Контактная информация", example = "{\"phone\":\"+996555123456\",\"address\":\"г. Бишкек, ул. Манаса 123\"}")
    private String contactInfo;

    @Schema(description = "Список товаров в заказе")
    private List<OrderItemDTO> items;

    @Schema(description = "Дата создания заказа", example = "2025-10-20T12:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Дата последнего обновления заказа", example = "2025-10-20T14:45:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Дата подтверждения заказа", example = "2025-10-20T13:00:00")
    private LocalDateTime confirmedAt;

    @Schema(description = "Дата завершения заказа", example = "2025-10-21T10:00:00")
    private LocalDateTime completedAt;
}
