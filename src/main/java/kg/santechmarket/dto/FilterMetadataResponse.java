package kg.santechmarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    @Schema(description = "Список всех брендов", example = "[\"Lammin\", \"PRO AQUA\", \"VALTEC\"]")
    private List<String> brands;

    @Schema(description = "Список всех диаметров", example = "[\"20 мм\", \"25 мм\", \"32 мм\"]")
    private List<String> diameters;

    @Schema(description = "Список всех давлений", example = "[\"PN10\", \"PN20\", \"PN25\"]")
    private List<String> pressures;

    @Schema(description = "Список всех материалов", example = "[\"PPR\", \"PVC\", \"Metal\"]")
    private List<String> materials;

    @Schema(description = "Список всех типов армирования", example = "[\"Без армирования\", \"Стекловолокно\", \"Алюминий\"]")
    private List<String> reinforcements;

    @Schema(description = "Список всех длин бухт/труб", example = "[\"28 м\", \"40 м\", \"60 м\", \"100 м\"]")
    private List<String> lengths;

    @Schema(description = "Список всех назначений", example = "[\"Холодная вода\", \"Горячая вода\", \"Универсальные\"]")
    private List<String> purposes;

    @Schema(description = "Список всех толщин стенки", example = "[\"SDR6\", \"SDR7.4\"]")
    private List<String> wallThicknesses;

    @Schema(description = "Диапазон цен")
    private PriceRange priceRange;

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
