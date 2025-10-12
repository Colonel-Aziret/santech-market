package kg.santechmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для ответа с корзиной
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {

    private Long id;
    private List<CartItemDTO> items;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private Integer uniqueItemsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
