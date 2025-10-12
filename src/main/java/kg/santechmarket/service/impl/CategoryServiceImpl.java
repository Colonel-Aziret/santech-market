package kg.santechmarket.service.impl;

import kg.santechmarket.dto.CategoryDto;
import kg.santechmarket.entity.Category;
import kg.santechmarket.repository.CategoryRepository;
import kg.santechmarket.repository.ProductRepository;
import kg.santechmarket.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для работы с категориями товаров
 * <p>
 * Основные функции:
 * - CRUD операции с категориями
 * - Управление порядком отображения
 * - Получение активных категорий для каталога
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Override
    public List<Category> findAllActiveCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrder();
        log.debug("Найдено {} активных категорий", categories.size());
        return categories;
    }

    @Override
    public List<Category> findAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrder();
    }

    @Override
    public List<Category> searchActiveCategories(String name) {
        log.debug("Поиск категорий по названию: '{}'", name);
        return categoryRepository.findActiveByNameContaining(name);
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        log.info("Создание новой категории: {}", category.getName());

        // Проверяем уникальность названия
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Категория с таким названием уже существует: " + category.getName());
        }

        // Валидация
        validateCategory(category);

        // Устанавливаем порядок отображения в конец списка, если не указан
        if (category.getDisplayOrder() == null || category.getDisplayOrder() <= 0) {
            Integer maxOrder = categoryRepository.findMaxDisplayOrder();
            category.setDisplayOrder(maxOrder != null ? maxOrder + 1 : 1);
        }

        // По умолчанию категория активна
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Категория создана: {} с ID {} и порядком {}",
                savedCategory.getName(), savedCategory.getId(), savedCategory.getDisplayOrder());

        return savedCategory;
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category categoryUpdate) {
        log.info("Обновление категории с ID: {}", id);

        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));

        // Проверяем уникальность названия только если название изменилось
        if (!existingCategory.getName().equals(categoryUpdate.getName())) {
            if (categoryRepository.existsByName(categoryUpdate.getName())) {
                throw new IllegalArgumentException("Категория с таким названием уже существует: " + categoryUpdate.getName());
            }
        }

        // Валидация
        validateCategory(categoryUpdate);

        // Обновляем поля
        existingCategory.setName(categoryUpdate.getName());
        existingCategory.setDescription(categoryUpdate.getDescription());
        existingCategory.setImageUrl(categoryUpdate.getImageUrl());
        existingCategory.setDisplayOrder(categoryUpdate.getDisplayOrder());
        existingCategory.setIsActive(categoryUpdate.getIsActive());

        Category savedCategory = categoryRepository.save(existingCategory);
        log.info("Категория обновлена: {}", savedCategory.getName());

        return savedCategory;
    }

    @Override
    @Transactional
    public void deactivateCategory(Long id) {
        log.info("Деактивация категории с ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));

        // Проверяем, есть ли активные товары в этой категории
        long activeProductCount = productRepository.countByCategoryIdAndIsActiveTrue(id);
        if (activeProductCount > 0) {
            throw new IllegalArgumentException(
                    String.format("Нельзя деактивировать категорию '%s' - в ней есть %d активных товаров",
                            category.getName(), activeProductCount));
        }

        category.setIsActive(false);
        categoryRepository.save(category);

        log.info("Категория деактивирована: {}", category.getName());
    }

    @Override
    @Transactional
    public void activateCategory(Long id) {
        log.info("Активация категории с ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));

        category.setIsActive(true);
        categoryRepository.save(category);

        log.info("Категория активирована: {}", category.getName());
    }

    @Override
    @Transactional
    public void updateDisplayOrder(Long id, Integer newOrder) {
        log.info("Обновление порядка категории с ID: {} на {}", id, newOrder);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));

        if (newOrder <= 0) {
            throw new IllegalArgumentException("Порядок отображения должен быть больше 0");
        }

        category.setDisplayOrder(newOrder);
        categoryRepository.save(category);

        log.info("Порядок категории '{}' изменен на {}", category.getName(), newOrder);
    }

    @Override
    @Transactional
    public void moveCategoryUp(Long id) {
        log.info("Перемещение категории с ID: {} вверх", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));

        if (category.getDisplayOrder() > 1) {
            updateDisplayOrder(id, category.getDisplayOrder() - 1);
        }
    }

    @Override
    @Transactional
    public void moveCategoryDown(Long id) {
        log.info("Перемещение категории с ID: {} вниз", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));

        Integer maxOrder = categoryRepository.findMaxDisplayOrder();
        if (maxOrder != null && category.getDisplayOrder() < maxOrder) {
            updateDisplayOrder(id, category.getDisplayOrder() + 1);
        }
    }

    @Override
    public long getTotalCategoryCount() {
        return categoryRepository.count();
    }

    @Override
    public long getActiveCategoryCount() {
        return categoryRepository.countByIsActiveTrue();
    }

    @Override
    public List<CategoryDto.CategoryWithProductCount> getCategoriesWithProductCount() {
        List<Category> categories = findAllActiveCategories();

        return categories.stream()
                .map(category -> {
                    long productCount = productRepository.countByCategoryIdAndIsActiveTrue(category.getId());
                    return new CategoryDto.CategoryWithProductCount(category, productCount);
                })
                .toList();
    }

    /**
     * Валидация категории
     */
    private void validateCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название категории не может быть пустым");
        }

        if (category.getName().length() > 100) {
            throw new IllegalArgumentException("Название категории не может превышать 100 символов");
        }

        if (category.getDescription() != null && category.getDescription().length() > 500) {
            throw new IllegalArgumentException("Описание категории не может превышать 500 символов");
        }

        if (category.getDisplayOrder() != null && category.getDisplayOrder() <= 0) {
            throw new IllegalArgumentException("Порядок отображения должен быть больше 0");
        }
    }

    // ===== Имплементация методов для иерархии (подкатегории) =====

    @Override
    public List<Category> findAllRootCategories() {
        log.debug("Получение всех корневых категорий");
        return categoryRepository.findByParentIsNullOrderByDisplayOrderAsc();
    }

    @Override
    public List<Category> findActiveRootCategories() {
        log.debug("Получение активных корневых категорий");
        return categoryRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Override
    public List<Category> getCategoryTree() {
        log.debug("Получение дерева категорий");
        return categoryRepository.findAllRootCategoriesWithSubcategories();
    }

    @Override
    public List<Category> getSubcategories(Long parentId) {
        log.debug("Получение подкатегорий для категории с ID: {}", parentId);

        // Проверяем существование родительской категории
        if (!categoryRepository.existsById(parentId)) {
            throw new IllegalArgumentException("Категория не найдена: " + parentId);
        }

        return categoryRepository.findByParentIdOrderByDisplayOrderAsc(parentId);
    }

    @Override
    public List<Category> getActiveSubcategories(Long parentId) {
        log.debug("Получение активных подкатегорий для категории с ID: {}", parentId);

        // Проверяем существование родительской категории
        if (!categoryRepository.existsById(parentId)) {
            throw new IllegalArgumentException("Категория не найдена: " + parentId);
        }

        return categoryRepository.findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(parentId);
    }

    @Override
    @Transactional
    public Category createSubcategory(Long parentId, Category subcategory) {
        log.info("Создание подкатегории для категории с ID: {}", parentId);

        // Проверяем существование родительской категории
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Родительская категория не найдена: " + parentId));

        // Устанавливаем родителя
        subcategory.setParent(parent);

        // Создаём подкатегорию через обычный метод create
        return createCategory(subcategory);
    }

    @Override
    @Transactional
    public Category moveCategoryToParent(Long categoryId, Long newParentId) {
        log.info("Перемещение категории {} к новому родителю {}", categoryId, newParentId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + categoryId));

        // Если newParentId == null, делаем категорию корневой
        if (newParentId == null) {
            category.setParent(null);
            log.info("Категория {} теперь корневая", categoryId);
        } else {
            // Проверяем, что новый родитель существует
            Category newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new IllegalArgumentException("Новая родительская категория не найдена: " + newParentId));

            // Проверяем на циклические ссылки
            if (!canBeParent(newParentId, categoryId)) {
                throw new IllegalArgumentException("Нельзя переместить категорию: это создаст циклическую ссылку");
            }

            category.setParent(newParent);
            log.info("Категория {} перемещена к родителю {}", categoryId, newParentId);
        }

        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getCategoryPath(Long categoryId) {
        log.debug("Получение пути для категории с ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + categoryId));

        List<Category> path = new ArrayList<>();
        Category current = category;

        // Идём от категории к корню
        while (current != null) {
            path.add(current);
            current = current.getParent();
        }

        // Разворачиваем список, чтобы путь был от корня к текущей категории
        Collections.reverse(path);

        return path;
    }

    @Override
    public boolean canBeParent(Long potentialParentId, Long childId) {
        log.debug("Проверка, может ли категория {} быть родителем для {}", potentialParentId, childId);

        // Категория не может быть родителем самой себе
        if (potentialParentId.equals(childId)) {
            return false;
        }

        // Проверяем, не является ли potentialParent потомком child
        Category potentialParent = categoryRepository.findById(potentialParentId).orElse(null);
        if (potentialParent == null) {
            return false;
        }

        // Идём вверх по иерархии от potentialParent
        Category current = potentialParent.getParent();
        while (current != null) {
            if (current.getId().equals(childId)) {
                // child является предком potentialParent - циклическая ссылка
                return false;
            }
            current = current.getParent();
        }

        return true;
    }
}