package kg.santechmarket.dto;

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
        private String title;
        private String subtitle;
        private String imageUrl;  // Полный URL с базовым адресом
        private String linkUrl;
        private Integer displayOrder;
        private Boolean isActive;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String backgroundColor;
        private String textColor;
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
        private String title;
        private String subtitle;
        private String imageUrl;  // Относительный путь или полный URL
        private String linkUrl;
        private Integer displayOrder;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String backgroundColor;
        private String textColor;
    }

    /**
     * Запрос на обновление баннера
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateBannerRequest {
        private String title;
        private String subtitle;
        private String imageUrl;  // Относительный путь или полный URL
        private String linkUrl;
        private Integer displayOrder;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String backgroundColor;
        private String textColor;
    }
}
