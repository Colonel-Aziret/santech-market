package kg.santechmarket.service.impl;

import kg.santechmarket.entity.Cart;
import kg.santechmarket.entity.Order;
import kg.santechmarket.enums.OrderStatus;
import kg.santechmarket.repository.OrderRepository;
import kg.santechmarket.service.CartService;
import kg.santechmarket.service.NotificationService;
import kg.santechmarket.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для работы с заказами
 * <p>
 * Основные функции:
 * - Создание заказа из корзины
 * - Управление статусами заказов
 * - Получение истории заказов
 * - Статистика для менеджеров
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final NotificationService notificationService;

    /**
     * Найти заказ по ID
     */
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * Найти заказ по номеру
     */
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    /**
     * Получить заказ с товарами
     */
    public Optional<Order> findByIdWithItems(Long id) {
        return orderRepository.findByIdWithItems(id);
    }

    /**
     * Создать заказ из корзины пользователя
     */
    @Transactional
    public Order createOrderFromCart(Long userId, String customerComment, String contactInfo) {
        log.info("Создание заказа из корзины пользователя: {}", userId);

        // Валидируем корзину
        cartService.validateCartForCheckout(userId);

        // Синхронизируем цены в корзине
        Cart cart = cartService.syncCartPrices(userId);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста или не найдена");
        }

        // Создаем заказ из корзины
        Order order = Order.createFromCart(cart);
        order.setCustomerComment(customerComment);
        order.setContactInfo(contactInfo);

        // Сохраняем заказ
        Order savedOrder = orderRepository.save(order);

        // Очищаем корзину после успешного создания заказа
        cartService.clearUserCart(userId);

        // Отправляем уведомление пользователю
        notificationService.sendOrderCreatedNotification(savedOrder);

        log.info("Заказ создан: {} на сумму {} с. для пользователя {}",
                savedOrder.getOrderNumber(), savedOrder.getTotalAmount(), userId);

        return savedOrder;
    }

    /**
     * Получить заказы пользователя
     */
    public Page<Order> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Получить заказы пользователя с товарами
     */
    public List<Order> getUserOrdersWithItems(Long userId) {
        return orderRepository.findByUserIdWithItems(userId);
    }

    /**
     * Получить заказы по статусу
     */
    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    /**
     * Поиск заказов
     */
    public Page<Order> searchOrders(String searchTerm, Pageable pageable) {
        return orderRepository.searchOrders(searchTerm, pageable);
    }

    /**
     * Обновить статус заказа
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus, String managerComment) {
        log.info("Обновление статуса заказа {} на {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден: " + orderId));

        OrderStatus oldStatus = order.getStatus();

        // Валидируем переход статуса
        validateStatusTransition(oldStatus, newStatus);

        // Обновляем статус
        order.updateStatus(newStatus);

        // Добавляем комментарий менеджера, если указан
        if (managerComment != null && !managerComment.trim().isEmpty()) {
            String existingComment = order.getManagerComment();
            String newComment = existingComment == null ?
                    managerComment : existingComment + "\n" + managerComment;
            order.setManagerComment(newComment);
        }

        Order savedOrder = orderRepository.save(order);

        // Отправляем уведомление об изменении статуса
        notificationService.sendOrderStatusUpdateNotification(savedOrder, oldStatus);

        log.info("Статус заказа {} изменен с {} на {}",
                order.getOrderNumber(), oldStatus, newStatus);

        return savedOrder;
    }

    /**
     * Подтвердить заказ (PENDING -> CONFIRMED)
     */
    @Transactional
    public Order confirmOrder(Long orderId, String managerComment) {
        return updateOrderStatus(orderId, OrderStatus.CONFIRMED, managerComment);
    }

    /**
     * Начать обработку заказа (CONFIRMED -> PROCESSING)
     */
    @Transactional
    public Order startProcessingOrder(Long orderId, String managerComment) {
        return updateOrderStatus(orderId, OrderStatus.PROCESSING, managerComment);
    }

    /**
     * Подготовить заказ к выдаче (PROCESSING -> READY)
     */
    @Transactional
    public Order markOrderReady(Long orderId, String managerComment) {
        return updateOrderStatus(orderId, OrderStatus.READY, managerComment);
    }

    /**
     * Завершить заказ (READY -> COMPLETED)
     */
    @Transactional
    public Order completeOrder(Long orderId, String managerComment) {
        return updateOrderStatus(orderId, OrderStatus.COMPLETED, managerComment);
    }

    /**
     * Отменить заказ
     */
    @Transactional
    public Order cancelOrder(Long orderId, String cancelReason) {
        log.info("Отмена заказа: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден: " + orderId));

        // Проверяем, можно ли отменить заказ
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Нельзя отменить завершенный заказ");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Заказ уже отменен");
        }

        OrderStatus oldStatus = order.getStatus();
        order.updateStatus(OrderStatus.CANCELLED);

        // Добавляем причину отмены в комментарий
        String comment = "ОТМЕНЕН: " + (cancelReason != null ? cancelReason : "Без указания причины");
        order.setManagerComment(comment);

        Order savedOrder = orderRepository.save(order);

        // Уведомление об отмене
        notificationService.sendOrderCancelledNotification(savedOrder, cancelReason);

        log.info("Заказ {} отменен. Причина: {}", order.getOrderNumber(), cancelReason);

        return savedOrder;
    }

    /**
     * Получить заказы за период
     */
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersByDateRange(startDate, endDate);
    }

    /**
     * Получить просроченные заказы (в статусе PENDING более N часов)
     */
    public List<Order> getOverdueOrders(int hoursThreshold) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursThreshold);
        return orderRepository.findPendingOrdersOlderThan(cutoffTime);
    }

    /**
     * Получить статистику заказов
     */
    public OrderStatistics getOrderStatistics() {
        Map<OrderStatus, Long> statusCounts = orderRepository.getOrderStatusStatistics()
                .stream()
                .collect(Collectors.toMap(
                        row -> (OrderStatus) row[0],
                        row -> (Long) row[1]
                ));

        long totalOrders = orderRepository.count();

        return new OrderStatistics(totalOrders, statusCounts);
    }

    /**
     * Получить количество заказов пользователя
     */
    public long getUserOrderCount(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    /**
     * Валидация перехода статуса
     */
    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        // Определяем допустимые переходы статусов
        boolean isValidTransition = switch (from) {
            case PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING -> to == OrderStatus.READY || to == OrderStatus.CANCELLED;
            case READY -> to == OrderStatus.COMPLETED || to == OrderStatus.CANCELLED;
            case COMPLETED -> false; // Завершенный заказ нельзя изменить
            case CANCELLED -> false; // Отмененный заказ нельзя изменить
        };

        if (!isValidTransition) {
            throw new IllegalArgumentException(
                    String.format("Недопустимый переход статуса с %s на %s", from, to));
        }
    }

    /**
     * DTO для статистики заказов
     */
    public record OrderStatistics(long totalOrders, Map<OrderStatus, Long> statusCounts) {
    }
}