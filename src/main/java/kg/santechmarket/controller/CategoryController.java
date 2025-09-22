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
@Tag(name = "Category Management", description = "API для управления категориями товаров")
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
}