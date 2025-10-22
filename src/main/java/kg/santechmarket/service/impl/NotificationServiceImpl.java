package kg.santechmarket.service.impl;

import kg.santechmarket.dto.NotificationResponseDTO;
import kg.santechmarket.entity.Notification;
import kg.santechmarket.entity.Order;
import kg.santechmarket.entity.User;
import kg.santechmarket.enums.NotificationType;
import kg.santechmarket.enums.OrderStatus;
import kg.santechmarket.repository.NotificationRepository;
import kg.santechmarket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с уведомлениями
 * <p>
 * Основные функции:
 * - Создание уведомлений для пользователей
 * - Отправка уведомлений о заказах
 * - Управление прочтением уведомлений
 * - Массовые уведомления
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Найти уведомление по ID
     */
    public Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id);
    }

    /**
     * Получить уведомления пользователя
     */
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Получить непрочитанные уведомления пользователя
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Получить количество непрочитанных уведомлений
     */
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Создать уведомление
     */
    @Transactional
    public Notification createNotification(Long userId, NotificationType type,
                                           String title, String content, String metadata) {
        log.debug("Создание уведомления для пользователя {}: {}", userId, title);

        Notification notification = new Notification();

        User user = new User();
        user.setId(userId);
        notification.setUser(user);

        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setMetadata(metadata);

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Уведомление создано: {} для пользователя {}", title, userId);

        return savedNotification;
    }

    /**
     * Отметить уведомление как прочитанное
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        log.debug("Отметка уведомления {} как прочитанное пользователем {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Уведомление не найдено: " + notificationId));

        // Проверяем, что уведомление принадлежит пользователю
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Уведомление не принадлежит пользователю");
        }

        if (!notification.getIsRead()) {
            notification.markAsRead();
            notificationRepository.save(notification);
            log.info("Уведомление {} отмечено как прочитанное", notificationId);
        }
    }

    /**
     * Отметить все уведомления пользователя как прочитанные
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Отметка всех уведомлений как прочитанные для пользователя: {}", userId);

        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);

        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }

        if (!unreadNotifications.isEmpty()) {
            notificationRepository.saveAll(unreadNotifications);
            log.info("Отмечено {} уведомлений как прочитанные для пользователя {}",
                    unreadNotifications.size(), userId);
        }
    }

    /**
     * Удалить уведомление
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        log.info("Удаление уведомления {} пользователем {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Уведомление не найдено: " + notificationId));

        // Проверяем, что уведомление принадлежит пользователю
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Уведомление не принадлежит пользователю");
        }

        notificationRepository.delete(notification);
        log.info("Уведомление {} удалено", notificationId);
    }

    /**
     * Отправить уведомление о создании заказа
     */
    @Transactional
    public void sendOrderCreatedNotification(Order order) {
        String title = "Заказ оформлен";
        String content = String.format(
                "Ваш заказ %s успешно оформлен на сумму %.2f с. Наш менеджер свяжется с вами для уточнения деталей.",
                order.getOrderNumber(), order.getTotalAmount()
        );
        String metadata = String.format("{\"order_id\": %d, \"order_number\": \"%s\"}",
                order.getId(), order.getOrderNumber());

        createNotification(order.getUser().getId(), NotificationType.ORDER_UPDATE, title, content, metadata);
    }

    /**
     * Отправить уведомление об изменении статуса заказа
     */
    @Transactional
    public void sendOrderStatusUpdateNotification(Order order, OrderStatus oldStatus) {
        String title = "Статус заказа изменен";
        String content = getStatusUpdateMessage(order.getOrderNumber(), order.getStatus());
        String metadata = String.format(
                "{\"order_id\": %d, \"order_number\": \"%s\", \"old_status\": \"%s\", \"new_status\": \"%s\"}",
                order.getId(), order.getOrderNumber(), oldStatus, order.getStatus()
        );

        createNotification(order.getUser().getId(), NotificationType.ORDER_UPDATE, title, content, metadata);
    }

    /**
     * Отправить уведомление об отмене заказа
     */
    @Transactional
    public void sendOrderCancelledNotification(Order order, String reason) {
        String title = "Заказ отменен";
        String content = String.format(
                "Ваш заказ %s был отменен. %s",
                order.getOrderNumber(),
                reason != null ? "Причина: " + reason : "Для получения подробной информации свяжитесь с нами."
        );
        String metadata = String.format(
                "{\"order_id\": %d, \"order_number\": \"%s\", \"cancel_reason\": \"%s\"}",
                order.getId(), order.getOrderNumber(), reason != null ? reason : ""
        );

        createNotification(order.getUser().getId(), NotificationType.ORDER_UPDATE, title, content, metadata);
    }

    /**
     * Отправить массовое уведомление всем пользователям
     */
    @Transactional
    public void sendBroadcastNotification(NotificationType type, String title, String content,
                                          List<Long> userIds, String metadata) {
        log.info("Отправка массового уведомления '{}' для {} пользователей", title, userIds.size());

        for (Long userId : userIds) {
            createNotification(userId, type, title, content, metadata);
        }

        log.info("Массовое уведомление отправлено {} пользователям", userIds.size());
    }

    /**
     * Отправить уведомление о скидке
     */
    @Transactional
    public void sendDiscountNotification(List<Long> userIds, String promotionTitle,
                                         String promotionDescription, Integer discountPercent) {
        String title = "Скидки!";
        String content = String.format("🎉 %s\n\n%s", promotionTitle, promotionDescription);
        String metadata = String.format(
                "{\"promotion_title\": \"%s\", \"discount_percent\": %d}",
                promotionTitle, discountPercent != null ? discountPercent : 0
        );

        sendBroadcastNotification(NotificationType.DISCOUNT, title, content, userIds, metadata);
    }

    /**
     * Очистка старых уведомлений (старше N дней)
     */
    @Transactional
    public void cleanupOldNotifications(int daysThreshold) {
        log.info("Очистка уведомлений старше {} дней", daysThreshold);

        int deletedCount = notificationRepository.deleteOldNotifications(daysThreshold);

        if (deletedCount > 0) {
            log.info("Удалено {} старых уведомлений", deletedCount);
        }
    }

    /**
     * Получить текст сообщения об изменении статуса
     */
    private String getStatusUpdateMessage(String orderNumber, OrderStatus status) {
        return switch (status) {
            case CONFIRMED ->
                    String.format("Заказ %s подтвержден и принят в обработку. Ожидайте звонка менеджера.", orderNumber);
            case PROCESSING -> String.format("Заказ %s находится в процессе сборки.", orderNumber);
            case READY -> String.format("Заказ %s готов к выдаче! Можете забрать его в удобное время.", orderNumber);
            case COMPLETED -> String.format("Заказ %s успешно завершен. Спасибо за покупку!", orderNumber);
            case CANCELLED -> String.format("Заказ %s был отменен.", orderNumber);
            default -> String.format("Статус заказа %s изменен.", orderNumber);
        };
    }

    /**
     * Конвертировать Notification entity в NotificationResponseDTO
     */
    public NotificationResponseDTO toNotificationResponseDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .metadata(notification.getMetadata())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}