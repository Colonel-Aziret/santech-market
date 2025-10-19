package kg.santechmarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Сущность элемента заказа
 * Хранит снимок товара на момент оформления заказа
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem extends BaseEntity {

    /**
     * Заказ, к которому относится этот элемент
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"items", "user"})
    private Order order;

    /**
     * Товар в заказе
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"category", "promotions"})
    private Product product;

    /**
     * Название товара на момент заказа (снимок)
     * Сохраняем, чтобы история заказов была корректной даже при изменении названия товара
     */
    @Column(name = "product_name", nullable = false)
    private String productName;

    /**
     * Количество товара
     */
    @NotNull(message = "Количество не может быть пустым")
    @Min(value = 1, message = "Количество должно быть больше 0")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Цена товара на момент заказа
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

    /**
     * Автоматически устанавливаем название товара при сохранении
     */
    @PrePersist
    @PreUpdate
    private void updateProductName() {
        if (product != null && productName == null) {
            productName = product.getName();
        }
    }
}