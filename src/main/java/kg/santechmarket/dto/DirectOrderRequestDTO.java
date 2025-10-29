package kg.santechmarket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для создания заказа напрямую (без добавления в корзину)
 * Используется для функции "Оформить сейчас"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectOrderRequestDTO {

    /**
     * ID товара для заказа
     */
    @NotNull(message = "ID товара не может быть пустым")
    private Long productId;

    /**
     * Количество товара
     */
    @NotNull(message = "Количество не может быть пустым")
    @Min(value = 1, message = "Количество должно быть больше 0")
    private Integer quantity;

    /**
     * Комментарий к заказу от клиента
     */
    @Size(max = 1000, message = "Комментарий не может превышать 1000 символов")
    private String customerComment;

    /**
     * Контактная информация для заказа (JSON)
     * Например: {"phone": "+996555123456", "address": "г. Бишкек, ул. Манаса 123"}
     */
    private String contactInfo;
}
