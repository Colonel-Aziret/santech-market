package kg.santechmarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO для элемента заказа
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Элемент заказа (товар)")
public class OrderItemDTO {

    @Schema(description = "ID элемента заказа", example = "1")
    private Long id;

    @Schema(description = "ID товара", example = "5")
    private Long productId;

    @Schema(description = "Название товара на момент заказа", example = "Смартфон Samsung Galaxy S23")
    private String productName;

    @Schema(description = "Количество товара", example = "2")
    private Integer quantity;

    @Schema(description = "Цена товара на момент заказа в сомах", example = "45000.00")
    private BigDecimal price;

    @Schema(description = "Общая стоимость (цена × количество)", example = "90000.00")
    private BigDecimal totalPrice;
}
