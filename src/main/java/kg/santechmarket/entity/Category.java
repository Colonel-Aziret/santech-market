package kg.santechmarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Сущность категории товаров с поддержкой иерархии
 * Например:
 * - Трубы (родитель)
 *   - Полипропиленовые трубы (подкатегория)
 *   - Металлопластиковые трубы (подкатегория)
 */
@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_category_parent", columnList = "parent_id"),
        @Index(name = "idx_category_active", columnList = "is_active")
})
@Getter
@Setter
public class Category extends BaseEntity {

    /**
     * Название категории (например, "Трубы", "Полипропиленовые трубы")
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
     * Порядок отображения (чем меньше число, тем выше в списке)
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /**
     * Активна ли категория (отображается ли в приложении)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Родительская категория (null для корневых категорий)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference
    private Category parent;

    /**
     * Подкатегории
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("displayOrder ASC, name ASC")
    @JsonManagedReference
    private List<Category> subcategories = new ArrayList<>();

    /**
     * Товары в этой категории
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    /**
     * Проверить, является ли категория корневой (не имеет родителя)
     */
    public boolean isRootCategory() {
        return parent == null;
    }

    /**
     * Проверить, есть ли у категории подкатегории
     */
    public boolean hasSubcategories() {
        return subcategories != null && !subcategories.isEmpty();
    }

    /**
     * Получить уровень вложенности категории (0 для корневых)
     */
    public int getLevel() {
        int level = 0;
        Category current = this.parent;
        while (current != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }
}