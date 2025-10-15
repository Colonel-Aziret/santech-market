package kg.santechmarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Запрос на добавление изображения к товару")
    public record AddImageRequest(
            @NotBlank(message = "URL изображения не может быть пустым")
            @Size(max = 255, message = "URL изображения не может превышать 255 символов")
            @Schema(description = "URL изображения", example = "https://example.com/images/product1.jpg", required = true, maxLength = 255)
            String imageUrl,

            @Schema(description = "Порядок отображения изображения (меньше = раньше)", example = "1")
            Integer displayOrder,

            @Size(max = 200, message = "Описание изображения не может превышать 200 символов")
            @Schema(description = "Альтернативный текст для изображения (для SEO и доступности)", example = "Труба полипропиленовая диаметр 20мм", maxLength = 200)
            String altText
    ) {
    }

    /**
     * DTO для обновления изображения
     */
    @Schema(description = "Запрос на обновление изображения товара")
    public record UpdateImageRequest(
            @Size(max = 255, message = "URL изображения не может превышать 255 символов")
            @Schema(description = "Новый URL изображения", example = "https://example.com/images/product1-updated.jpg", maxLength = 255)
            String imageUrl,

            @Schema(description = "Новый порядок отображения", example = "2")
            Integer displayOrder,

            @Size(max = 200, message = "Описание изображения не может превышать 200 символов")
            @Schema(description = "Новый альтернативный текст", example = "Обновленное описание", maxLength = 200)
            String altText
    ) {
    }

    /**
     * DTO для ответа с информацией об изображении
     */
    @Schema(description = "Информация об изображении товара")
    public record ImageResponse(
            @Schema(description = "ID изображения", example = "1")
            Long id,

            @Schema(description = "URL изображения", example = "https://example.com/images/product1.jpg")
            String imageUrl,

            @Schema(description = "Порядок отображения", example = "1")
            Integer displayOrder,

            @Schema(description = "Альтернативный текст изображения", example = "Труба полипропиленовая")
            String altText
    ) {
    }

    /**
     * DTO для загрузки нескольких изображений
     */
    @Schema(description = "Запрос на массовое добавление изображений")
    public record BulkAddImagesRequest(
            @NotNull(message = "Список изображений не может быть пустым")
            @Schema(description = "Список изображений для добавления", required = true)
            java.util.List<AddImageRequest> images
    ) {
    }
}
