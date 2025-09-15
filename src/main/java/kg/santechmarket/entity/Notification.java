package kg.santechmarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kg.santechmarket.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Сущность уведомлений пользователей
 * Используется для информирования о скидках, акциях, изменениях заказов
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user", columnList = "user_id"),
        @Index(name = "idx_notification_read", columnList = "is_read"),
        @Index(name = "idx_notification_type", columnList = "type")
})
@Getter
@Setter
public class Notification extends BaseEntity {

    /**
     * Пользователь, которому предназначено уведомление
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Тип уведомления
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    /**
     * Заголовок уведомления
     */
    @NotBlank(message = "Заголовок уведомления не может быть пустым")
    @Size(max = 200, message = "Заголовок не может превышать 200 символов")
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Содержимое уведомления
     */
    @NotBlank(message = "Содержимое уведомления не может быть пустым")
    @Size(max = 1000, message = "Содержимое не может превышать 1000 символов")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Прочитано ли уведомление пользователем
     */
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /**
     * Дата прочтения уведомления
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Дополнительные данные уведомления (JSON)
     * Например: {"order_id": 123, "discount_percent": 20}
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Отметить уведомление как прочитанное
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}