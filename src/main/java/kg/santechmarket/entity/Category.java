package kg.santechmarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Сущность категории товаров
 * Например: Трубы, Фитинги, Арматура, Канализация и т.д.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category extends BaseEntity {

    /**
     * Название категории (например, "Трубы", "Фитинги")
     */
    @NotBlank(message = "Название категории не может быть пустым")
    @Size(max = 100, message = "Название категории не может превышать 100 символов")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * Описание категории
     */
    @Size(max = 500, message = "Описание не может превышать 500 символов")
    @Column(name = "description")
    private String description;

    /**
     * URL изображения категории
     */
    @Size(max = 255, message = "URL изображения не может превышать 255 символов")
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * Порядок отображения на главном экране (чем меньше число, тем выше в списке)
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /**
     * Активна ли категория (отображается ли в приложении)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Товары в этой категории
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;
}