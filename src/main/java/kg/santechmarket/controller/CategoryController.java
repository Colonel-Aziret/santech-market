package kg.santechmarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.santechmarket.dto.CategoryDto;
import kg.santechmarket.entity.Category;
import kg.santechmarket.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Категории", description = "API для управления категориями товаров: просмотр, создание, редактирование, работа с иерархией (подкатегориями)")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Получить все активные категории", description = "Возвращает список всех активных категорий для каталога")
    @ApiResponse(responseCode = "200", description = "Успешно получен список категорий")
    public ResponseEntity<List<Category>> getAllActiveCategories() {
        List<Category> categories = categoryService.findAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/all")
    @Operation(summary = "Получить все категории", description = "Возвращает список всех категорий включая неактивные (для админки)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.findAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить категорию по ID", description = "Возвращает информацию о категории по её идентификатору")
    public ResponseEntity<Category> getCategoryById(@Parameter(description = "ID категории") @PathVariable Long id) {
        return categoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск категорий", description = "Поиск активных категорий по названию")
    public ResponseEntity<List<Category>> searchCategories(
            @Parameter(description = "Поисковый запрос") @RequestParam String name) {
        List<Category> categories = categoryService.searchActiveCategories(name);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/with-product-count")
    @Operation(summary = "Получить категории с количеством товаров", description = "Возвращает категории с количеством товаров в каждой")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<CategoryDto.CategoryWithProductCount>> getCategoriesWithProductCount() {
        List<CategoryDto.CategoryWithProductCount> categories = categoryService.getCategoriesWithProductCount();
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    @Operation(summary = "Создать категорию", description = "Создает новую категорию")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        Category createdCategory = categoryService.createCategory(category);
        return ResponseEntity.ok(createdCategory);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить категорию", description = "Обновляет информацию о категории")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Category> updateCategory(
            @Parameter(description = "ID категории") @PathVariable Long id,
            @Valid @RequestBody Category categoryUpdate) {
        Category updatedCategory = categoryService.updateCategory(id, categoryUpdate);
        return ResponseEntity.ok(updatedCategory);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Деактивировать категорию", description = "Деактивирует категорию (мягкое удаление)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateCategory(@Parameter(description = "ID категории") @PathVariable Long id) {
        categoryService.deactivateCategory(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Активировать категорию", description = "Активирует ранее деактивированную категорию")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateCategory(@Parameter(description = "ID категории") @PathVariable Long id) {
        categoryService.activateCategory(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/order")
    @Operation(summary = "Изменить порядок отображения", description = "Изменяет порядок отображения категории")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> updateDisplayOrder(
            @Parameter(description = "ID категории") @PathVariable Long id,
            @Parameter(description = "Новый порядок отображения") @RequestParam Integer newOrder) {
        categoryService.updateDisplayOrder(id, newOrder);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/move-up")
    @Operation(summary = "Переместить категорию вверх", description = "Перемещает категорию вверх в списке")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> moveCategoryUp(@Parameter(description = "ID категории") @PathVariable Long id) {
        categoryService.moveCategoryUp(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/move-down")
    @Operation(summary = "Переместить категорию вниз", description = "Перемещает категорию вниз в списке")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> moveCategoryDown(@Parameter(description = "ID категории") @PathVariable Long id) {
        categoryService.moveCategoryDown(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/total-count")
    @Operation(summary = "Получить общее количество категорий", description = "Возвращает общее количество категорий")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Long> getTotalCategoryCount() {
        long count = categoryService.getTotalCategoryCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/active-count")
    @Operation(summary = "Получить количество активных категорий", description = "Возвращает количество активных категорий")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Long> getActiveCategoryCount() {
        long count = categoryService.getActiveCategoryCount();
        return ResponseEntity.ok(count);
    }

    // ===== Эндпоинты для работы с иерархией (подкатегории) =====

    @GetMapping("/root")
    @Operation(summary = "Получить корневые категории", description = "Возвращает список корневых категорий (без родителя)")
    public ResponseEntity<List<Category>> getRootCategories() {
        List<Category> categories = categoryService.findActiveRootCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/tree")
    @Operation(summary = "Получить дерево категорий", description = "Возвращает полное дерево категорий с подкатегориями")
    public ResponseEntity<List<Category>> getCategoryTree() {
        List<Category> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/{parentId}/subcategories")
    @Operation(summary = "Получить подкатегории", description = "Возвращает список подкатегорий для указанной категории")
    public ResponseEntity<List<Category>> getSubcategories(
            @Parameter(description = "ID родительской категории") @PathVariable Long parentId) {
        List<Category> subcategories = categoryService.getActiveSubcategories(parentId);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/{categoryId}/path")
    @Operation(summary = "Получить путь категории", description = "Возвращает путь от корня до указанной категории (breadcrumbs)")
    public ResponseEntity<List<Category>> getCategoryPath(
            @Parameter(description = "ID категории") @PathVariable Long categoryId) {
        List<Category> path = categoryService.getCategoryPath(categoryId);
        return ResponseEntity.ok(path);
    }

    @PostMapping("/{parentId}/subcategories")
    @Operation(summary = "Создать подкатегорию", description = "Создает новую подкатегорию для указанной родительской категории")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Category> createSubcategory(
            @Parameter(description = "ID родительской категории") @PathVariable Long parentId,
            @Valid @RequestBody Category subcategory) {
        Category createdSubcategory = categoryService.createSubcategory(parentId, subcategory);
        return ResponseEntity.ok(createdSubcategory);
    }

    @PatchMapping("/{categoryId}/move-to-parent")
    @Operation(summary = "Переместить категорию к другому родителю", description = "Перемещает категорию к новой родительской категории")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Category> moveCategoryToParent(
            @Parameter(description = "ID категории") @PathVariable Long categoryId,
            @Parameter(description = "ID нового родителя (null для корневой)") @RequestParam(required = false) Long newParentId) {
        Category movedCategory = categoryService.moveCategoryToParent(categoryId, newParentId);
        return ResponseEntity.ok(movedCategory);
    }

    @GetMapping("/can-be-parent")
    @Operation(summary = "Проверить возможность родительства", description = "Проверяет, может ли одна категория быть родителем для другой")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Boolean> canBeParent(
            @Parameter(description = "ID потенциального родителя") @RequestParam Long potentialParentId,
            @Parameter(description = "ID дочерней категории") @RequestParam Long childId) {
        boolean canBe = categoryService.canBeParent(potentialParentId, childId);
        return ResponseEntity.ok(canBe);
    }
}