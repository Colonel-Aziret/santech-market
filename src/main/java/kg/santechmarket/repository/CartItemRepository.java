package kg.santechmarket.repository;

import kg.santechmarket.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с элементами корзины
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
