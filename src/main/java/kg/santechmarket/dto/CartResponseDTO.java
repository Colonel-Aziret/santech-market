package kg.santechmarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO для ответа с корзиной
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Корзина покупок пользователя")
public class CartResponseDTO {

    @Schema(description = "ID корзины", example = "1")
    private Long id;

    @Builder.Default
    @Schema(description = "Список товаров в корзине")
    private List<CartItemDTO> items = new ArrayList<>();

    @Schema(description = "Общая сумма всех товаров в корзине в сомах", example = "15750.50")
    private BigDecimal totalAmount;

    @Schema(description = "Общее количество единиц товара (сумма quantity всех позиций)", example = "25")
    private Integer totalItems;

    @Schema(description = "Количество уникальных товаров в корзине", example = "7")
    private Integer uniqueItemsCount;

    @Schema(description = "Дата создания корзины", example = "2025-10-16T12:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Дата последнего обновления корзины", example = "2025-10-16T14:45:00")
    private LocalDateTime updatedAt;
}
