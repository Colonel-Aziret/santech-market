package kg.santechmarket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для промо-баннеров
 */
public class PromoBannerDto {

    /**
     * Ответ с данными баннера
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BannerResponse {
        private Long id;
        private String imageUrl;
        private Integer displayOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * Запрос на создание баннера
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBannerRequest {
        @NotBlank(message = "URL изображения не может быть пустым")
        private String imageUrl;

        private Integer displayOrder;
    }

    /**
     * Запрос на обновление баннера
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateBannerRequest {
        private String imageUrl;
        private Integer displayOrder;
    }
}
