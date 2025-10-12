package kg.santechmarket.dto;

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
public class CartItemDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private String productBrand;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}
