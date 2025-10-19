package kg.santechmarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kg.santechmarket.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сущность заказа
 * Создается когда пользователь оформляет товары из корзины
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user", columnList = "user_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_number", columnList = "order_number")
})
@Getter
@Setter
public class Order extends BaseEntity {

    /**
     * Уникальный номер заказа (генерируется автоматически)
     */
    @NotBlank(message = "Номер заказа не может быть пустым")
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    /**
     * Пользователь, который оформил заказ
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"orders", "cart", "password", "notifications"})
    private User user;

    /**
     * Статус заказа
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * Общая сумма заказа
     */
    @NotNull(message = "Сумма заказа не может быть пустой")
    @DecimalMin(value = "0.01", message = "Сумма заказа должна быть больше 0")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Общее количество товаров в заказе
     */
    @Column(name = "total_items", nullable = false)
    private Integer totalItems = 0;

    /**
     * Комментарий к заказу от клиента
     */
    @Size(max = 1000, message = "Комментарий не может превышать 1000 символов")
    @Column(name = "customer_comment", columnDefinition = "TEXT")
    private String customerComment;

    /**
     * Комментарий менеджера (внутренний)
     */
    @Size(max = 1000, message = "Комментарий не может превышать 1000 символов")
    @Column(name = "manager_comment", columnDefinition = "TEXT")
    private String managerComment;

    /**
     * Контактная информация для заказа (JSON)
     * Например: {"phone": "+996555123456", "address": "г. Бишкек, ул. Манаса 123"}
     */
    @Column(name = "contact_info", columnDefinition = "TEXT")
    private String contactInfo;

    /**
     * Дата подтверждения заказа менеджером
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * Дата завершения заказа
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Товары в заказе
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnoreProperties({"order"})
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Генерация уникального номера заказа
     */
    @PrePersist
    private void generateOrderNumber() {
        if (orderNumber == null) {
            // Формат: ORD-YYYYMMDD-XXXX (где XXXX - случайные символы)
            String datePart = java.time.LocalDate.now().toString().replace("-", "");
            String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            orderNumber = "ORD-" + datePart + "-" + randomPart;
        }
    }

    /**
     * Создать заказ из корзины
     *
     * @param cart корзина пользователя
     */
    public static Order createFromCart(Cart cart) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setTotalAmount(cart.getTotalAmount());
        order.setTotalItems(cart.getTotalItems());

        // Переносим товары из корзины в заказ
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            order.getItems().add(orderItem);
        }

        return order;
    }

    /**
     * Обновить статус заказа с автоматической установкой временных меток
     */
    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;

        switch (newStatus) {
            case CONFIRMED -> this.confirmedAt = LocalDateTime.now();
            case COMPLETED -> this.completedAt = LocalDateTime.now();
        }
    }
}