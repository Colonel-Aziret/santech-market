package kg.santechmarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Сущность промо-баннера для главной страницы
 * Содержит информацию о баннере, включая заголовки, изображение, ссылку и период активности
 */
@Entity
@Table(name = "promo_banners")
@Getter
@Setter
public class PromoBanner extends BaseEntity {

    /**
     * Заголовок баннера
     */
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(max = 100, message = "Заголовок не может превышать 100 символов")
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Подзаголовок/описание баннера
     */
    @Size(max = 500, message = "Подзаголовок не может превышать 500 символов")
    @Column(name = "subtitle")
    private String subtitle;

    /**
     * URL изображения баннера
     */
    @NotBlank(message = "URL изображения не может быть пустым")
    @Size(max = 255, message = "URL изображения не может превышать 255 символов")
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    /**
     * URL ссылки при клике на баннер
     */
    @Size(max = 500, message = "URL ссылки не может превышать 500 символов")
    @Column(name = "link_url")
    private String linkUrl;

    /**
     * Порядок отображения (чем меньше число, тем выше в списке)
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /**
     * Активен ли баннер
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Дата начала показа баннера
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /**
     * Дата окончания показа баннера
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * Цвет фона баннера (hex)
     */
    @Size(max = 7, message = "Цвет фона должен быть в формате #RRGGBB")
    @Column(name = "background_color")
    private String backgroundColor;

    /**
     * Цвет текста баннера (hex)
     */
    @Size(max = 7, message = "Цвет текста должен быть в формате #RRGGBB")
    @Column(name = "text_color")
    private String textColor;

    /**
     * Проверить, активен ли баннер в данный момент
     */
    public boolean isCurrentlyActive() {
        if (!isActive) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }

        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }

        return true;
    }
}