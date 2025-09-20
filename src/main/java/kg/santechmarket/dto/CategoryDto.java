package kg.santechmarket.dto;

import kg.santechmarket.entity.Category;

public record CategoryDto(

) {
    /**
     * DTO для категории с количеством товаров
     */
    public record CategoryWithProductCount(Category category, long productCount) {
    }
}
