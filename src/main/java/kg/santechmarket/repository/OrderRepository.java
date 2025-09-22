package kg.santechmarket.repository;

import kg.santechmarket.entity.Order;
import kg.santechmarket.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с заказами
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Найти заказ по номеру
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Получить заказы пользователя, отсортированные по дате создания (новые сначала)
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Получить заказы пользователя по статусу
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Получить все заказы по статусу
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    /**
     * Получить заказы за определенный период
     */
    @Query("SELECT o FROM Order o WHERE " +
            "o.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY o.createdAt DESC")
    List<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Получить заказ с товарами (Fetch Join для избежания N+1)
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.items oi " +
            "LEFT JOIN FETCH oi.product p " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    /**
     * Получить заказы пользователя с товарами
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.items oi " +
            "LEFT JOIN FETCH oi.product p " +
            "WHERE o.user.id = :userId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    /**
     * Поиск заказов по номеру или имени клиента
     */
    @Query("SELECT o FROM Order o " +
            "JOIN o.user u WHERE " +
            "(LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> searchOrders(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Получить количество заказов по статусу
     */
    long countByStatus(OrderStatus status);

    /**
     * Получить количество заказов пользователя
     */
    long countByUserId(Long userId);

    /**
     * Получить заказы, ожидающие обработки (старше определенного времени)
     */
    @Query("SELECT o FROM Order o WHERE " +
            "o.status = 'PENDING' " +
            "AND o.createdAt < :cutoffTime " +
            "ORDER BY o.createdAt")
    List<Order> findPendingOrdersOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Статистика заказов по статусам
     */
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderStatusStatistics();

    /**
     * Проверить существование заказа с номером
     */
    boolean existsByOrderNumber(String orderNumber);
}