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

    @Schema(description = "ID товара", example = "5")
    private Long id;

    @Schema(description = "Дата создания записи")
    private java.time.LocalDateTime createdAt;

    @Schema(description = "Дата обновления записи")
    private java.time.LocalDateTime updatedAt;

    @Schema(description = "Название товара", example = "Труба полипропиленовая PN20 диаметр 20мм")
    private String name;

    @Schema(description = "Описание товара")
    private String description;

    @Schema(description = "Бренд товара", example = "PRO AQUA")
    private String brand;

    @Schema(description = "Артикул товара", example = "PPR-PN20-20")
    private String sku;

    @Schema(description = "URL главного изображения товара", example = "https://example.com/images/product1.jpg")
    private String imageUrl;

    @Schema(description = "Старая цена (если есть скидка)", example = "550.00")
    private BigDecimal oldPrice;

    @Schema(description = "Спецификации товара")
    private String specifications;

    @Schema(description = "Активен ли товар")
    private Boolean isActive;

    @Schema(description = "Является ли товар рекомендуемым")
    private Boolean isFeatured;

    @Schema(description = "Дополнительные изображения")
    private java.util.List<String> additionalImages;

    @Schema(description = "Количество товара в корзине", example = "3", minimum = "1")
    private Integer quantity;

    @Schema(description = "Цена за единицу товара в сомах", example = "250.00")
    private BigDecimal price;

    @Schema(description = "Общая стоимость позиции (цена × количество)", example = "750.00")
    private BigDecimal totalPrice;
}
