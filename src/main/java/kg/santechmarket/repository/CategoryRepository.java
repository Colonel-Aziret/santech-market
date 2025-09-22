package kg.santechmarket.repository;

import kg.santechmarket.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с категориями товаров
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Найти категорию по названию
     */
    Optional<Category> findByName(String name);

    /**
     * Проверить существование категории по названию
     */
    boolean existsByName(String name);

    /**
     * Получить все активные категории, отсортированные по порядку отображения
     */
    List<Category> findByIsActiveTrueOrderByDisplayOrder();

    /**
     * Получить все категории, отсортированные по порядку отображения
     */
    List<Category> findAllByOrderByDisplayOrder();

    /**
     * Найти активные категории по части названия
     */
    @Query("SELECT c FROM Category c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "AND c.isActive = true " +
            "ORDER BY c.displayOrder")
    List<Category> findActiveByNameContaining(String name);

    /**
     * Получить количество активных категорий
     */
    long countByIsActiveTrue();

    /**
     * Получить максимальный порядок отображения (для добавления новой категории в конец)
     */
    @Query("SELECT MAX(c.displayOrder) FROM Category c")
    Integer findMaxDisplayOrder();
}