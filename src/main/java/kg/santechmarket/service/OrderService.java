package kg.santechmarket.service;

import kg.santechmarket.dto.OrderResponseDTO;
import kg.santechmarket.entity.Order;
import kg.santechmarket.enums.OrderStatus;
import kg.santechmarket.service.impl.OrderServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Интерфейс сервиса для работы с заказами
 */
public interface OrderService {

    /**
     * Найти заказ по ID
     */
    Optional<Order> findById(Long id);

    /**
     * Найти заказ по номеру
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Получить заказ с товарами
     */
    Optional<Order> findByIdWithItems(Long id);

    /**
     * Создать заказ из корзины пользователя
     */
    Order createOrderFromCart(Long userId, String customerComment, String contactInfo);

    /**
     * Создать заказ напрямую без добавления в корзину (функция "Оформить сейчас")
     */
    Order createDirectOrder(Long userId, Long productId, Integer quantity, String customerComment, String contactInfo);

    /**
     * Получить заказы пользователя
     */
    Page<Order> getUserOrders(Long userId, Pageable pageable);

    /**
     * Получить заказы пользователя с товарами
     */
    List<Order> getUserOrdersWithItems(Long userId);

    /**
     * Получить заказы по статусу
     */
    Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable);

    /**
     * Поиск заказов
     */
    Page<Order> searchOrders(String searchTerm, Pageable pageable);

    /**
     * Обновить статус заказа
     */
    Order updateOrderStatus(Long orderId, OrderStatus newStatus, String managerComment);

    /**
     * Подтвердить заказ (PENDING -> CONFIRMED)
     */
    Order confirmOrder(Long orderId, String managerComment);

    /**
     * Начать обработку заказа (CONFIRMED -> PROCESSING)
     */
    Order startProcessingOrder(Long orderId, String managerComment);

    /**
     * Подготовить заказ к выдаче (PROCESSING -> READY)
     */
    Order markOrderReady(Long orderId, String managerComment);

    /**
     * Завершить заказ (READY -> COMPLETED)
     */
    Order completeOrder(Long orderId, String managerComment);

    /**
     * Отменить заказ
     */
    Order cancelOrder(Long orderId, String cancelReason);

    /**
     * Получить заказы за период
     */
    List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Получить просроченные заказы (в статусе PENDING более N часов)
     */
    List<Order> getOverdueOrders(int hoursThreshold);

    /**
     * Получить статистику заказов
     */
    OrderServiceImpl.OrderStatistics getOrderStatistics();

    /**
     * Получить количество заказов пользователя
     */
    long getUserOrderCount(Long userId);

    /**
     * Конвертировать Order entity в OrderResponseDTO
     */
    OrderResponseDTO toOrderResponseDTO(Order order);

    /**
     * Проверить, является ли пользователь владельцем заказа
     */
    boolean isOrderOwner(Long orderId, Long userId);

    /**
     * Проверить, является ли пользователь владельцем заказа по номеру
     */
    boolean isOrderOwnerByNumber(String orderNumber, Long userId);

    /**
     * DTO для статистики заказов
     */
    record OrderStatistics(long totalOrders, Map<OrderStatus, Long> statusCounts) {
    }
}