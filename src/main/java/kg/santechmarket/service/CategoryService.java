package kg.santechmarket.service;

import kg.santechmarket.dto.CategoryDto;
import kg.santechmarket.entity.Category;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс сервиса для работы с категориями товаров
 */
public interface CategoryService {

    /**
     * Найти категорию по ID
     */
    Optional<Category> findById(Long id);

    /**
     * Найти категорию по названию
     */
    Optional<Category> findByName(String name);

    /**
     * Получить все активные категории для каталога
     */
    List<Category> findAllActiveCategories();

    /**
     * Получить все категории (для админки)
     */
    List<Category> findAllCategories();

    /**
     * Поиск активных категорий по названию
     */
    List<Category> searchActiveCategories(String name);

    /**
     * Создать новую категорию
     */
    Category createCategory(Category category);

    /**
     * Обновить категорию
     */
    Category updateCategory(Long id, Category categoryUpdate);

    /**
     * Деактивировать категорию (мягкое удаление)
     */
    void deactivateCategory(Long id);

    /**
     * Активировать категорию
     */
    void activateCategory(Long id);

    /**
     * Изменить порядок отображения категории
     */
    void updateDisplayOrder(Long id, Integer newOrder);

    /**
     * Переместить категорию вверх в списке
     */
    void moveCategoryUp(Long id);

    /**
     * Переместить категорию вниз в списке
     */
    void moveCategoryDown(Long id);

    /**
     * Получить статистику категорий
     */
    long getTotalCategoryCount();
    long getActiveCategoryCount();

    /**
     * Получить категорию с количеством товаров
     */
    List<CategoryDto.CategoryWithProductCount> getCategoriesWithProductCount();

    // ===== Методы для работы с иерархией (подкатегории) =====

    /**
     * Получить все корневые категории (для главной страницы)
     */
    List<Category> findAllRootCategories();

    /**
     * Получить все активные корневые категории
     */
    List<Category> findActiveRootCategories();

    /**
     * Получить дерево категорий с подкатегориями
     */
    List<Category> getCategoryTree();

    /**
     * Получить подкатегории для указанной категории
     */
    List<Category> getSubcategories(Long parentId);

    /**
     * Получить активные подкатегории
     */
    List<Category> getActiveSubcategories(Long parentId);

    /**
     * Создать подкатегорию
     */
    Category createSubcategory(Long parentId, Category subcategory);

    /**
     * Переместить категорию в другую родительскую категорию
     */
    Category moveCategoryToParent(Long categoryId, Long newParentId);

    /**
     * Получить путь от корня до категории (breadcrumbs)
     */
    List<Category> getCategoryPath(Long categoryId);

    /**
     * Проверить, может ли категория быть родителем для другой (предотвращение циклов)
     */
    boolean canBeParent(Long potentialParentId, Long childId);
}