package kg.santechmarket.repository;

import kg.santechmarket.entity.Cart;
import kg.santechmarket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с корзинами
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Найти корзину пользователя
     */
    Optional<Cart> findByUser(User user);

    /**
     * Найти корзину по ID пользователя
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Проверить существование корзины у пользователя
     */
    boolean existsByUserId(Long userId);

    /**
     * Получить корзину с товарами для пользователя
     * (Fetch Join для избежания N+1 проблемы)
     */
    @Query("SELECT c FROM Cart c " +
            "LEFT JOIN FETCH c.items ci " +
            "LEFT JOIN FETCH ci.product p " +
            "LEFT JOIN FETCH p.category " +
            "WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    /**
     * Удалить корзину пользователя
     */
    void deleteByUserId(Long userId);
}