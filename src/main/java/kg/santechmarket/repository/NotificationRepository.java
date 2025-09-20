package kg.santechmarket.repository;

import kg.santechmarket.entity.Notification;
import kg.santechmarket.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с уведомлениями
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Получить уведомления пользователя, отсортированные по дате создания
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Получить непрочитанные уведомления пользователя
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * Получить непрочитанные уведомления пользователя (только ID для подсчета)
     */
    List<Notification> findByUserIdAndIsReadFalse(Long userId);

    /**
     * Получить уведомления пользователя по типу
     */
    List<Notification> findByUserIdAndType(Long userId, NotificationType type);

    /**
     * Подсчитать количество непрочитанных уведомлений пользователя
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * Подсчитать общее количество уведомлений пользователя
     */
    long countByUserId(Long userId);

    /**
     * Найти уведомления по типу за определенный период
     */
    @Query("SELECT n FROM Notification n WHERE " +
            "n.type = :type " +
            "AND n.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findByTypeAndDateRange(@Param("type") NotificationType type,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Удалить старые уведомления (старше N дней)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Удалить старые уведомления с помощью количества дней
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    default int deleteOldNotifications(int daysThreshold) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysThreshold);
        return deleteOldNotifications(cutoffDate);
    }

    /**
     * Получить статистику уведомлений по типам
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n GROUP BY n.type")
    List<Object[]> getNotificationStatsByType();

    /**
     * Найти последние уведомления пользователя определенного типа
     */
    @Query("SELECT n FROM Notification n WHERE " +
            "n.user.id = :userId " +
            "AND n.type = :type " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findLatestByUserAndType(@Param("userId") Long userId,
                                               @Param("type") NotificationType type,
                                               Pageable pageable);
}