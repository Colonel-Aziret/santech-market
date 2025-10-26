package kg.santechmarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO для метаданных фильтров товаров
 * Содержит списки всех доступных значений для фильтрации
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Метаданные для фильтров товаров")
public class FilterMetadataResponse {

    @Builder.Default
    @Schema(description = "Список всех доступных фильтров")
    private List<FilterItem> filters = new ArrayList<>();

    @Schema(description = "Диапазон цен")
    private PriceRange priceRange;

    /**
     * DTO для элемента фильтра
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Элемент фильтра с ключом, названием и списком значений")
    public static class FilterItem {

        @Schema(description = "Ключ фильтра (используется в запросах)", example = "brand")
        private String key;

        @Schema(description = "Название фильтра (отображается пользователю)", example = "Бренд")
        private String label;

        @Schema(description = "Список доступных значений", example = "[\"Lammin\", \"PRO AQUA\", \"VALTEC\"]")
        private List<String> list;
    }

    /**
     * DTO для диапазона цен
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Диапазон цен в каталоге")
    public static class PriceRange {

        @Schema(description = "Минимальная цена в сомах", example = "40.00")
        private BigDecimal min;

        @Schema(description = "Максимальная цена в сомах", example = "50000.00")
        private BigDecimal max;
    }
}
