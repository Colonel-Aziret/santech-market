package kg.santechmarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.santechmarket.dto.ProductImageDto;
import kg.santechmarket.entity.Product;
import kg.santechmarket.entity.ProductImage;
import kg.santechmarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Товары", description = "API для управления товарами: просмотр, поиск, фильтрация, управление (CRUD операции)")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(
            summary = "Получить все активные товары",
            description = "Возвращает постраничный список всех активных товаров с сортировкой и пагинацией. По умолчанию 20 товаров на странице."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список товаров успешно получен"
            )
    })
    public ResponseEntity<Page<Product>> getAllActiveProducts(
            @Parameter(description = "Параметры пагинации и сортировки (page, size, sort)")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.findAllActiveProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить товар по ID",
            description = "Возвращает полную информацию о товаре по его идентификатору, включая все характеристики и изображения"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Товар найден"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар не найден или неактивен"
            )
    })
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "ID товара", example = "1", required = true)
            @PathVariable Long id) {
        return productService.findActiveById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Получить товары по категории", description = "Возвращает постраничный список товаров в указанной категории")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @Parameter(description = "ID категории") @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.findProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/featured")
    @Operation(summary = "Получить рекомендуемые товары", description = "Возвращает список рекомендуемых товаров для главной страницы")
    public ResponseEntity<List<Product>> getFeaturedProducts() {
        List<Product> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Поиск товаров",
            description = "Полнотекстовый поиск товаров по названию и описанию. Результаты сортируются по релевантности."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результаты поиска получены")
    })
    public ResponseEntity<Page<Product>> searchProducts(
            @Parameter(description = "Поисковый запрос", example = "труба полипропиленовая", required = true)
            @RequestParam String query,
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.searchProducts(query, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/filter")
    @Operation(
            summary = "Фильтрация товаров",
            description = """
                    Комплексная фильтрация товаров по различным параметрам:
                    - Категория товара
                    - Бренд/производитель
                    - Диапазон цен (от и до)
                    - Текстовый поиск

                    Все параметры опциональны и могут комбинироваться.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отфильтрованный список товаров получен")
    })
    public ResponseEntity<Page<Product>> filterProducts(
            @Parameter(description = "ID категории для фильтрации", example = "5")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Бренд/производитель", example = "PRO AQUA")
            @RequestParam(required = false) String brand,

            @Parameter(description = "Минимальная цена в сомах", example = "100")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Максимальная цена в сомах", example = "5000")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Текстовый поиск", example = "труба")
            @RequestParam(required = false) String search,

            @Parameter(description = "Параметры пагинации")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.findProductsWithFilters(
                categoryId, brand, minPrice, maxPrice, search, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/discounted")
    @Operation(summary = "Получить товары со скидкой", description = "Возвращает постраничный список товаров со скидкой")
    public ResponseEntity<Page<Product>> getDiscountedProducts(@PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.getDiscountedProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}/similar")
    @Operation(summary = "Получить похожие товары", description = "Возвращает список товаров, похожих на указанный")
    public ResponseEntity<List<Product>> getSimilarProducts(
            @Parameter(description = "ID товара") @PathVariable Long id,
            @Parameter(description = "Максимальное количество") @RequestParam(defaultValue = "10") int limit) {
        List<Product> products = productService.getSimilarProducts(id, limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/brands")
    @Operation(summary = "Получить все бренды", description = "Возвращает список всех уникальных брендов")
    public ResponseEntity<List<String>> getAllBrands() {
        List<String> brands = productService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/search-by-spec")
    @Operation(summary = "Поиск по характеристике", description = "Поиск товаров по конкретной характеристике")
    public ResponseEntity<Page<Product>> searchBySpecification(
            @Parameter(description = "Ключ характеристики") @RequestParam String specKey,
            @Parameter(description = "Значение характеристики") @RequestParam String specValue,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.findBySpecification(specKey, specValue, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search-pipes")
    @Operation(summary = "Поиск труб по характеристикам", description = "Специализированный поиск труб по диаметру, давлению и материалу")
    public ResponseEntity<Page<Product>> searchPipesBySpecs(
            @Parameter(description = "Диаметр трубы") @RequestParam(required = false) String diameter,
            @Parameter(description = "Рабочее давление") @RequestParam(required = false) String pressure,
            @Parameter(description = "Материал") @RequestParam(required = false) String material,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productService.findByMultipleSpecifications(diameter, pressure, material, pageable);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @Operation(
            summary = "Создать новый товар",
            description = """
                    Создание нового товара в системе. Доступно только для ADMIN и MANAGER.

                    Обязательные поля:
                    - name (название)
                    - price (цена)
                    - categoryId (ID категории)

                    Опциональные поля:
                    - description, brand, specifications, isActive, isFeatured и др.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется ADMIN или MANAGER)")
    })
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.ok(createdProduct);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить товар",
            description = "Полное обновление информации о товаре. Доступно только для ADMIN и MANAGER."
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Товар не найден")
    })
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "ID товара", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody Product productUpdate) {
        Product updatedProduct = productService.updateProduct(id, productUpdate);
        return ResponseEntity.ok(updatedProduct);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "Деактивировать товар",
            description = "Мягкое удаление товара - товар скрывается из каталога, но остается в базе данных. Доступно только для ADMIN и MANAGER."
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар деактивирован"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Товар не найден")
    })
    public ResponseEntity<Void> deactivateProduct(
            @Parameter(description = "ID товара", example = "1", required = true)
            @PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Активировать товар",
            description = "Восстановление ранее деактивированного товара - товар снова становится видимым в каталоге. Доступно только для ADMIN и MANAGER."
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар активирован"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Товар не найден")
    })
    public ResponseEntity<Void> activateProduct(
            @Parameter(description = "ID товара", example = "1", required = true)
            @PathVariable Long id) {
        productService.activateProduct(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/toggle-featured")
    @Operation(summary = "Переключить статус рекомендуемого", description = "Добавляет или убирает товар из рекомендуемых")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> toggleFeatured(@Parameter(description = "ID товара") @PathVariable Long id) {
        productService.toggleFeatured(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/total-count")
    @Operation(summary = "Получить общее количество товаров", description = "Возвращает общее количество товаров")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Long> getTotalProductCount() {
        long count = productService.getTotalProductCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/active-count")
    @Operation(summary = "Получить количество активных товаров", description = "Возвращает количество активных товаров")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Long> getActiveProductCount() {
        long count = productService.getActiveProductCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/category/{categoryId}/count")
    @Operation(summary = "Получить количество товаров в категории", description = "Возвращает количество товаров в указанной категории")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Long> getProductCountByCategory(@Parameter(description = "ID категории") @PathVariable Long categoryId) {
        long count = productService.getProductCountByCategory(categoryId);
        return ResponseEntity.ok(count);
    }

    // ===== Управление изображениями товаров =====

    @GetMapping("/{productId}/images")
    @Operation(summary = "Получить все изображения товара", description = "Возвращает список всех изображений товара")
    public ResponseEntity<List<ProductImageDto.ImageResponse>> getProductImages(
            @Parameter(description = "ID товара") @PathVariable Long productId) {
        List<ProductImage> images = productService.getProductImages(productId);
        List<ProductImageDto.ImageResponse> response = images.stream()
                .map(img -> new ProductImageDto.ImageResponse(
                        img.getId(),
                        img.getImageUrl(),
                        img.getDisplayOrder(),
                        img.getAltText()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/images")
    @Operation(summary = "Добавить изображение к товару", description = "Добавляет новое изображение к товару")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ProductImageDto.ImageResponse> addProductImage(
            @Parameter(description = "ID товара") @PathVariable Long productId,
            @Valid @RequestBody ProductImageDto.AddImageRequest request) {
        ProductImage image = productService.addImageToProduct(
                productId,
                request.imageUrl(),
                request.displayOrder(),
                request.altText()
        );
        ProductImageDto.ImageResponse response = new ProductImageDto.ImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getDisplayOrder(),
                image.getAltText()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{productId}/images/{imageId}")
    @Operation(summary = "Обновить изображение товара", description = "Обновляет информацию об изображении товара")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ProductImageDto.ImageResponse> updateProductImage(
            @Parameter(description = "ID товара") @PathVariable Long productId,
            @Parameter(description = "ID изображения") @PathVariable Long imageId,
            @Valid @RequestBody ProductImageDto.UpdateImageRequest request) {
        ProductImage image = productService.updateProductImage(
                productId,
                imageId,
                request.imageUrl(),
                request.displayOrder(),
                request.altText()
        );
        ProductImageDto.ImageResponse response = new ProductImageDto.ImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getDisplayOrder(),
                image.getAltText()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/images/{imageId}/order")
    @Operation(summary = "Изменить порядок изображения", description = "Изменяет порядок отображения изображения")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ProductImageDto.ImageResponse> updateImageOrder(
            @Parameter(description = "ID товара") @PathVariable Long productId,
            @Parameter(description = "ID изображения") @PathVariable Long imageId,
            @Parameter(description = "Новый порядок") @RequestParam Integer order) {
        ProductImage image = productService.updateImageOrder(productId, imageId, order);
        ProductImageDto.ImageResponse response = new ProductImageDto.ImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getDisplayOrder(),
                image.getAltText()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @Operation(summary = "Удалить изображение товара", description = "Удаляет изображение товара")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteProductImage(
            @Parameter(description = "ID товара") @PathVariable Long productId,
            @Parameter(description = "ID изображения") @PathVariable Long imageId) {
        productService.deleteProductImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }
}