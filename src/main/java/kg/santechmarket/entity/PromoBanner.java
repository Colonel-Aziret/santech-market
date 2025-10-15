package kg.santechmarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Сущность промо-баннера для главной страницы
 * Содержит только изображение и порядок отображения
 */
@Entity
@Table(name = "promo_banners")
@Getter
@Setter
public class PromoBanner extends BaseEntity {

    /**
     * URL изображения баннера
     */
    @NotBlank(message = "URL изображения не может быть пустым")
    @Size(max = 255, message = "URL изображения не может превышать 255 символов")
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    /**
     * Порядок отображения (чем меньше число, тем выше в списке)
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
}