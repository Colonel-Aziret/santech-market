package kg.santechmarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Сущность товара
 * Например: "Труба PPR PN25 арм. стеклов. 20x3,4 (100м)"
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_brand", columnList = "brand"),
        @Index(name = "idx_product_active", columnList = "is_active")
})
@Getter
@Setter
public class Product extends BaseEntity {

    /**
     * Название товара
     */
    @NotBlank(message = "Название товара не может быть пустым")
    @Size(max = 200, message = "Название товара не может превышать 200 символов")
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Описание товара
     */
    @Size(max = 1000, message = "Описание не может превышать 1000 символов")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Цена товара
     */
    @NotNull(message = "Цена товара не может быть пустой")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Старая цена (для отображения скидки)
     */
    @DecimalMin(value = "0.01", message = "Старая цена должна быть больше 0")
    @Column(name = "old_price", precision = 10, scale = 2)
    private BigDecimal oldPrice;

    /**
     * Бренд/производитель (например, "Lammin")
     */
    @Size(max = 100, message = "Бренд не может превышать 100 символов")
    @Column(name = "brand")
    private String brand;

    /**
     * Артикул товара
     */
    @Size(max = 100, message = "Артикул не может превышать 100 символов")
    @Column(name = "sku")
    private String sku;

    /**
     * Главное изображение товара
     */
    @Size(max = 255, message = "URL изображения не может превышать 255 символов")
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * Дополнительные изображения товара (JSON массив URL)
     */
    @Column(name = "additional_images", columnDefinition = "TEXT")
    private String additionalImages;

    /**
     * Характеристики товара (JSON объект)
     * Например: {"diameter": "20 мм", "pressure": "PN25", "length": "100м"}
     */
    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;

    /**
     * Активен ли товар (отображается ли в каталоге)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Является ли товар рекомендуемым (для главной страницы)
     */
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    /**
     * Категория товара
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Элементы корзины с этим товаром
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems;

    /**
     * Элементы заказов с этим товаром
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;
}