package kg.santechmarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO для изображений товаров
 */
public class ProductImageDto {

    /**
     * DTO для добавления изображения к товару
     */
    public record AddImageRequest(
            @NotBlank(message = "URL изображения не может быть пустым")
            @Size(max = 255, message = "URL изображения не может превышать 255 символов")
            String imageUrl,

            Integer displayOrder,

            @Size(max = 200, message = "Описание изображения не может превышать 200 символов")
            String altText
    ) {
    }

    /**
     * DTO для обновления изображения
     */
    public record UpdateImageRequest(
            @Size(max = 255, message = "URL изображения не может превышать 255 символов")
            String imageUrl,

            Integer displayOrder,

            @Size(max = 200, message = "Описание изображения не может превышать 200 символов")
            String altText
    ) {
    }

    /**
     * DTO для ответа с информацией об изображении
     */
    public record ImageResponse(
            Long id,
            String imageUrl,
            Integer displayOrder,
            String altText
    ) {
    }

    /**
     * DTO для загрузки нескольких изображений
     */
    public record BulkAddImagesRequest(
            @NotNull(message = "Список изображений не может быть пустым")
            java.util.List<AddImageRequest> images
    ) {
    }
}
