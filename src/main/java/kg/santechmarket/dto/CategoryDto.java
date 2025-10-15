package kg.santechmarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kg.santechmarket.entity.Category;

public record CategoryDto(

) {
    /**
     * DTO для категории с количеством товаров
     */
    @Schema(description = "Категория с информацией о количестве товаров")
    public record CategoryWithProductCount(
            @Schema(description = "Информация о категории")
            Category category,

            @Schema(description = "Количество активных товаров в категории", example = "42")
            long productCount
    ) {
    }
}
