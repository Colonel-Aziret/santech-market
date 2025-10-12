package kg.santechmarket.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Сущность изображения товара
 * Хранит дополнительные фотографии товара
 */
@Entity
@Table(name = "product_images", indexes = {
        @Index(name = "idx_product_image_product", columnList = "product_id"),
        @Index(name = "idx_product_image_order", columnList = "display_order")
})
@Getter
@Setter
public class ProductImage extends BaseEntity {

    /**
     * Продукт, к которому относится изображение
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;

    /**
     * URL изображения
     */
    @NotBlank(message = "URL изображения не может быть пустым")
    @Size(max = 255, message = "URL изображения не может превышать 255 символов")
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    /**
     * Порядок отображения (для сортировки)
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /**
     * Описание изображения (alt текст)
     */
    @Size(max = 200, message = "Описание изображения не может превышать 200 символов")
    @Column(name = "alt_text")
    private String altText;
}
