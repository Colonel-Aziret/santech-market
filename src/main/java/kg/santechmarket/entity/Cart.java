package kg.santechmarket.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность корзины пользователя
 * У каждого пользователя может быть только одна активная корзина
 */
@Entity
@Table(name = "carts")
@Getter
@Setter
public class Cart extends BaseEntity {

    /**
     * Пользователь, которому принадлежит корзина
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonBackReference
    private User user;

    /**
     * Товары в корзине
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    /**
     * Общая стоимость корзины (вычисляется автоматически)
     */
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * Общее количество товаров в корзине
     */
    @Column(name = "total_items")
    private Integer totalItems = 0;

    /**
     * Добавить товар в корзину
     *
     * @param product  товар
     * @param quantity количество
     */
    public void addItem(Product product, Integer quantity) {
        CartItem existingItem = items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(this);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getPrice());
            items.add(newItem);
        }

        recalculateTotal();
    }

    /**
     * Удалить товар из корзины
     *
     * @param productId ID товара
     */
    public void removeItem(Long productId) {
        CartItem itemToRemove = items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (itemToRemove != null) {
            items.remove(itemToRemove);
            itemToRemove.setCart(null);
            recalculateTotal();
        }
    }

    /**
     * Обновить количество товара в корзине
     *
     * @param productId ID товара
     * @param quantity  новое количество
     */
    public void updateItemQuantity(Long productId, Integer quantity) {
        if (quantity <= 0) {
            removeItem(productId);
            return;
        }

        items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    recalculateTotal();
                });
    }

    /**
     * Очистить корзину
     */
    public void clear() {
        items.clear();
        totalAmount = BigDecimal.ZERO;
        totalItems = 0;
    }

    /**
     * Увеличить количество товара на 1
     *
     * @param productId ID товара
     */
    public void incrementItemQuantity(Long productId) {
        items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(item.getQuantity() + 1);
                    recalculateTotal();
                });
    }

    /**
     * Уменьшить количество товара на 1 (если станет 0, товар удаляется)
     *
     * @param productId ID товара
     */
    public void decrementItemQuantity(Long productId) {
        CartItem item = items.stream()
                .filter(cartItem -> cartItem.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (item != null) {
            if (item.getQuantity() <= 1) {
                removeItem(productId);
            } else {
                item.setQuantity(item.getQuantity() - 1);
                recalculateTotal();
            }
        }
    }

    /**
     * Проверить, пустая ли корзина
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Получить количество уникальных товаров в корзине
     */
    public int getUniqueItemsCount() {
        return items.size();
    }

    /**
     * Пересчитать общую стоимость и количество товаров
     */
    private void recalculateTotal() {
        totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Публичный метод для пересчета итогов (используется в сервисе)
     */
    public void recalculateTotals() {
        recalculateTotal();
    }
}