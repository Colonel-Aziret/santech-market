package kg.santechmarket.service;

import kg.santechmarket.entity.Notification;
import kg.santechmarket.entity.Order;
import kg.santechmarket.enums.NotificationType;
import kg.santechmarket.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс сервиса для работы с уведомлениями
 */
public interface NotificationService {

    /**
     * Найти уведомление по ID
     */
    Optional<Notification> findById(Long id);

    /**
     * Получить уведомления пользователя
     */
    Page<Notification> getUserNotifications(Long userId, Pageable pageable);

    /**
     * Получить непрочитанные уведомления пользователя
     */
    List<Notification> getUnreadNotifications(Long userId);

    /**
     * Получить количество непрочитанных уведомлений
     */
    long getUnreadNotificationCount(Long userId);

    /**
     * Создать уведомление
     */
    Notification createNotification(Long userId, NotificationType type,
                                    String title, String content, String metadata);

    /**
     * Отметить уведомление как прочитанное
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * Отметить все уведомления пользователя как прочитанные
     */
    void markAllAsRead(Long userId);

    /**
     * Удалить уведомление
     */
    void deleteNotification(Long notificationId, Long userId);

    /**
     * Отправить уведомление о создании заказа
     */
    void sendOrderCreatedNotification(Order order);

    /**
     * Отправить уведомление об изменении статуса заказа
     */
    void sendOrderStatusUpdateNotification(Order order, OrderStatus oldStatus);

    /**
     * Отправить уведомление об отмене заказа
     */
    void sendOrderCancelledNotification(Order order, String reason);

    /**
     * Отправить массовое уведомление всем пользователям
     */
    void sendBroadcastNotification(NotificationType type, String title, String content,
                                   List<Long> userIds, String metadata);

    /**
     * Отправить уведомление о скидке
     */
    void sendDiscountNotification(List<Long> userIds, String promotionTitle,
                                  String promotionDescription, Integer discountPercent);

    /**
     * Очистка старых уведомлений (старше N дней)
     */
    void cleanupOldNotifications(int daysThreshold);

    /**
     * Конвертировать Notification entity в NotificationResponseDTO
     */
    kg.santechmarket.dto.NotificationResponseDTO toNotificationResponseDTO(Notification notification);
}