package kg.santechmarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Сущность промо-баннера для главной страницы
 * Например: "РАСПРОДАЖА труб и герметики с 12.08 по 12.09"
 */
@Entity
@Table(name = "promo_banners")
@Getter
@Setter
public class PromoBanner extends BaseEntity {

    /**
     * Заголовок баннера
     */
    @NotBlank(message = "Заголовок баннера не может быть пустым")
    @Size(max = 200, message = "Заголовок не может превышать 200 символов")
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Подзаголовок или описание акции
     */
    @Size(max = 500, message = "Описание не может превышать 500 символов")
    @Column(name = "subtitle")
    private String subtitle;

    /**
     * URL изображения баннера
     */
    @Size(max = 255, message = "URL изображения не может превышать 255 символов")
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * Ссылка при клике на баннер (например, на категорию или страницу акции)
     */
    @Size(max = 255, message = "URL ссылки не может превышать 255 символов")
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
     * Цвет фона баннера (для мобильного приложения)
     */
    @Size(max = 7, message = "Цвет должен быть в формате #RRGGBB")
    @Column(name = "background_color")
    private String backgroundColor;

    /**
     * Цвет текста (для мобильного приложения)
     */
    @Size(max = 7, message = "Цвет должен быть в формате #RRGGBB")
    @Column(name = "text_color")
    private String textColor;
}