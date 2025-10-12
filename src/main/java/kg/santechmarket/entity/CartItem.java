package kg.santechmarket.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Сущность элемента корзины
 * Связывает товар с корзиной и хранит количество
 */
@Entity
@Table(name = "cart_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cart_id", "product_id"})
})
@Getter
@Setter
public class CartItem extends BaseEntity {

    /**
     * Корзина, к которой относится этот элемент
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonBackReference
    private Cart cart;

    /**
     * Товар в корзине
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Количество товара
     */
    @NotNull(message = "Количество не может быть пустым")
    @Min(value = 1, message = "Количество должно быть больше 0")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Цена товара на момент добавления в корзину
     * (фиксируем цену, чтобы она не изменилась при изменении цены товара)
     */
    @NotNull(message = "Цена не может быть пустой")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Получить общую стоимость этого элемента (цена * количество)
     */
    public BigDecimal getTotalPrice() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}