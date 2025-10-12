package kg.santechmarket.repository;

import kg.santechmarket.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с категориями товаров (с поддержкой иерархии)
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
    List<Category> findActiveByNameContaining(@Param("name") String name);

    /**
     * Получить количество активных категорий
     */
    long countByIsActiveTrue();

    /**
     * Получить максимальный порядок отображения (для добавления новой категории в конец)
     */
    @Query("SELECT MAX(c.displayOrder) FROM Category c")
    Integer findMaxDisplayOrder();

    // ===== Методы для работы с иерархией =====

    /**
     * Найти все корневые категории (без родителя)
     */
    List<Category> findByParentIsNullOrderByDisplayOrderAsc();

    /**
     * Найти все активные корневые категории
     */
    List<Category> findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Найти все подкатегории для указанной категории
     */
    List<Category> findByParentIdOrderByDisplayOrderAsc(Long parentId);

    /**
     * Найти все активные подкатегории для указанной категории
     */
    List<Category> findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(Long parentId);

    /**
     * Проверить, есть ли у категории подкатегории
     */
    boolean existsByParentId(Long parentId);

    /**
     * Подсчитать количество подкатегорий
     */
    long countByParentId(Long parentId);

    /**
     * Найти категорию с загруженными подкатегориями
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subcategories WHERE c.id = :id")
    Optional<Category> findByIdWithSubcategories(@Param("id") Long id);

    /**
     * Получить все корневые категории с подкатегориями (один запрос)
     */
    @Query("SELECT DISTINCT c FROM Category c " +
            "LEFT JOIN FETCH c.subcategories " +
            "WHERE c.parent IS NULL " +
            "ORDER BY c.displayOrder")
    List<Category> findAllRootCategoriesWithSubcategories();

    /**
     * Получить полное дерево категорий (рекурсивный запрос для PostgreSQL)
     */
    @Query(value = "WITH RECURSIVE category_tree AS ( " +
            "  SELECT id, name, parent_id, display_order, 0 as level " +
            "  FROM categories " +
            "  WHERE parent_id IS NULL " +
            "  UNION ALL " +
            "  SELECT c.id, c.name, c.parent_id, c.display_order, ct.level + 1 " +
            "  FROM categories c " +
            "  INNER JOIN category_tree ct ON c.parent_id = ct.id " +
            ") " +
            "SELECT id FROM category_tree ORDER BY level, display_order",
            nativeQuery = true)
    List<Long> findAllCategoryIdsInTreeOrder();
}