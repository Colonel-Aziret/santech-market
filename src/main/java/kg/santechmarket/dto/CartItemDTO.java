package kg.santechmarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO для элемента корзины
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Элемент корзины покупок")
public class CartItemDTO {

    @Schema(description = "ID элемента корзины", example = "1")
    private Long id;

    @Schema(description = "ID товара", example = "5")
    private Long productId;

    @Schema(description = "Название товара", example = "Труба полипропиленовая PN20 диаметр 20мм")
    private String productName;

    @Schema(description = "URL главного изображения товара", example = "https://example.com/images/product1.jpg")
    private String productImageUrl;

    @Schema(description = "Бренд товара", example = "PRO AQUA")
    private String productBrand;

    @Schema(description = "Количество товара в корзине", example = "3", minimum = "1")
    private Integer quantity;

    @Schema(description = "Цена за единицу товара в сомах", example = "250.00")
    private BigDecimal price;

    @Schema(description = "Общая стоимость позиции (цена × количество)", example = "750.00")
    private BigDecimal totalPrice;
}
