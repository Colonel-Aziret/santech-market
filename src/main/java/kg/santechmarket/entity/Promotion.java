package kg.santechmarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сущность акций и скидок
 * Например: "РАСПРОДАЖА труб и герметики с 12.08 по 12.09"
 */
@Entity
@Table(name = "promotions", indexes = {
        @Index(name = "idx_promotion_active", columnList = "is_active"),
        @Index(name = "idx_promotion_dates", columnList = "start_date, end_date")
})
@Getter
@Setter
public class Promotion extends BaseEntity {

    /**
     * Название акции
     */
    @NotBlank(message = "Название акции не может быть пустым")
    @Size(max = 200, message = "Название акции не может превышать 200 символов")
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Описание акции
     */
    @Size(max = 1000, message = "Описание не может превышать 1000 символов")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * URL изображения для баннера акции
     */
    @Size(max = 255, message = "URL изображения не может превышать 255 символов")
    @Column(name = "banner_image_url")
    private String bannerImageUrl;

    /**
     * Процент скидки (если применимо)
     */
    @DecimalMin(value = "0.01", message = "Скидка должна быть больше 0")
    @DecimalMax(value = "99.99", message = "Скидка не может быть больше 99.99%")
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    /**
     * Дата начала акции
     */
    @NotNull(message = "Дата начала акции не может быть пустой")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * Дата окончания акции
     */
    @NotNull(message = "Дата окончания акции не может быть пустой")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * Активна ли акция
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Отображается ли акция на главной странице
     */
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    /**
     * Товары, участвующие в акции
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "promotion_products",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;

    /**
     * Категории, участвующие в акции
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "promotion_categories",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    /**
     * Проверить, активна ли акция в данный момент
     */
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return isActive &&
                now.isAfter(startDate) &&
                now.isBefore(endDate);
    }

    /**
     * Валидация дат акции
     */
    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Дата окончания акции не может быть раньше даты начала");
        }
    }
}