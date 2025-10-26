package kg.santechmarket.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kg.santechmarket.dto.FilterMetadataResponse;
import kg.santechmarket.entity.Category;
import kg.santechmarket.entity.Product;
import kg.santechmarket.entity.ProductImage;
import kg.santechmarket.repository.CategoryRepository;
import kg.santechmarket.repository.ProductImageRepository;
import kg.santechmarket.repository.ProductRepository;
import kg.santechmarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для работы с товарами
 * <p>
 * Основные функции:
 * - CRUD операции с товарами
 * - Поиск и фильтрация товаров
 * - Получение рекомендуемых товаров
 * - Управление активностью товаров
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    /**
     * Найти товар по ID
     */
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Найти активный товар по ID
     */
    public Optional<Product> findActiveById(Long id) {
        return productRepository.findById(id)
                .filter(Product::getIsActive);
    }

    /**
     * Получить все активные товары с пагинацией
     */
    public Page<Product> findAllActiveProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable);
    }

    /**
     * Получить товары по категории
     */
    public Page<Product> findProductsByCategory(Long categoryId, Pageable pageable) {
        log.debug("Поиск товаров для категории: {}", categoryId);
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
    }

    /**
     * Получить товары по категории включая все подкатегории
     */
    public Page<Product> findProductsByCategoryIncludingSubcategories(Long categoryId, Pageable pageable) {
        log.debug("Поиск товаров для категории {} включая подкатегории", categoryId);
        return productRepository.findByCategoryIdIncludingSubcategories(categoryId, pageable);
    }

    /**
     * Получить рекомендуемые товары для главной страницы
     */
    public List<Product> getFeaturedProducts() {
        List<Product> featured = productRepository.findByIsActiveTrueAndIsFeaturedTrueOrderByCreatedAtDesc();
        log.debug("Найдено {} рекомендуемых товаров", featured.size());
        return featured;
    }

    /**
     * Поиск товаров по названию
     */
    public Page<Product> searchProductsByName(String searchTerm, Pageable pageable) {
        log.debug("Поиск товаров по запросу: '{}'", searchTerm);
        return productRepository.findByNameContainingIgnoreCase(searchTerm, pageable);
    }

    /**
     * Расширенный поиск товаров
     */
    public Page<Product> searchProducts(String searchTerm, Pageable pageable) {
        log.debug("Расширенный поиск товаров по запросу: '{}'", searchTerm);
        return productRepository.searchProducts(searchTerm, pageable);
    }

    /**
     * Комплексная фильтрация товаров
     */
    public Page<Product> findProductsWithFilters(Long categoryId, String brand,
                                                 BigDecimal minPrice, BigDecimal maxPrice,
                                                 String searchTerm, Pageable pageable) {
        log.debug("Фильтрация товаров: категория={}, бренд={}, цена={}-{}, поиск='{}'",
                categoryId, brand, minPrice, maxPrice, searchTerm);

        return productRepository.findProductsWithFilters(categoryId, brand, minPrice, maxPrice, searchTerm, pageable);
    }

    /**
     * Получить товары со скидкой
     */
    public Page<Product> getDiscountedProducts(Pageable pageable) {
        return productRepository.findDiscountedProducts(pageable);
    }

    /**
     * Получить похожие товары
     */
    public List<Product> getSimilarProducts(Long productId, int limit) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return List.of();
        }

        Product product = productOpt.get();
        Pageable pageable = PageRequest.of(0, limit);

        return productRepository.findSimilarProducts(product.getCategory().getId(), productId, pageable);
    }

    /**
     * Получить все уникальные бренды
     */
    public List<String> getAllBrands() {
        return productRepository.findDistinctBrands();
    }

    /**
     * Создать новый товар
     */
    @Transactional
    public Product createProduct(Product product) {
        log.info("Создание нового товара: {}", product.getName());

        // Проверяем существование категории
        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));

        product.setCategory(category);

        // Валидация
        validateProduct(product);

        // По умолчанию товар активен
        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }

        // По умолчанию товар не рекомендуемый
        if (product.getIsFeatured() == null) {
            product.setIsFeatured(false);
        }

        Product savedProduct = productRepository.save(product);
        log.info("Товар создан: {} с ID {}", savedProduct.getName(), savedProduct.getId());

        return savedProduct;
    }

    /**
     * Обновить товар
     */
    @Transactional
    public Product updateProduct(Long id, Product productUpdate) {
        log.info("Обновление товара с ID: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден: " + id));

        // Проверяем категорию, если она изменилась
        if (!existingProduct.getCategory().getId().equals(productUpdate.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(productUpdate.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));
            existingProduct.setCategory(newCategory);
        }

        // Обновляем поля
        existingProduct.setName(productUpdate.getName());
        existingProduct.setDescription(productUpdate.getDescription());
        existingProduct.setPrice(productUpdate.getPrice());
        existingProduct.setOldPrice(productUpdate.getOldPrice());
        existingProduct.setBrand(productUpdate.getBrand());
        existingProduct.setSku(productUpdate.getSku());
        existingProduct.setImageUrl(productUpdate.getImageUrl());
        existingProduct.setSpecifications(productUpdate.getSpecifications());
        existingProduct.setIsActive(productUpdate.getIsActive());
        existingProduct.setIsFeatured(productUpdate.getIsFeatured());

        // Валидация
        validateProduct(existingProduct);

        Product savedProduct = productRepository.save(existingProduct);
        log.info("Товар обновлен: {}", savedProduct.getName());

        return savedProduct;
    }

    /**
     * Деактивировать товар (мягкое удаление)
     */
    @Transactional
    public void deactivateProduct(Long id) {
        log.info("Деактивация товара с ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден: " + id));

        product.setIsActive(false);
        productRepository.save(product);

        log.info("Товар деактивирован: {}", product.getName());
    }

    /**
     * Активировать товар
     */
    @Transactional
    public void activateProduct(Long id) {
        log.info("Активация товара с ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден: " + id));

        product.setIsActive(true);
        productRepository.save(product);

        log.info("Товар активирован: {}", product.getName());
    }

    /**
     * Установить/убрать товар как рекомендуемый
     */
    @Transactional
    public void toggleFeatured(Long id) {
        log.info("Переключение статуса 'рекомендуемый' для товара с ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден: " + id));

        product.setIsFeatured(!product.getIsFeatured());
        productRepository.save(product);

        log.info("Товар {} теперь {}", product.getName(),
                product.getIsFeatured() ? "рекомендуемый" : "обычный");
    }

    /**
     * Получить статистику товаров
     */
    public long getTotalProductCount() {
        return productRepository.count();
    }

    public long getActiveProductCount() {
        return productRepository.countByIsActiveTrue();
    }

    public long getProductCountByCategory(Long categoryId) {
        return productRepository.countByCategoryIdAndIsActiveTrue(categoryId);
    }

    /**
     * Поиск товаров по конкретной характеристике
     */
    public Page<Product> findBySpecification(String specKey, String specValue, Pageable pageable) {
        log.debug("Поиск товаров по характеристике: {}={}", specKey, specValue);
        return productRepository.findBySpecification(specKey, specValue, pageable);
    }

    /**
     * Поиск товаров по нескольким характеристикам (для труб)
     */
    public Page<Product> findByMultipleSpecifications(String diameter, String pressure, String material, Pageable pageable) {
        log.debug("Поиск товаров по характеристикам: диаметр={}, давление={}, материал={}",
                diameter, pressure, material);
        return productRepository.findByMultipleSpecifications(diameter, pressure, material, pageable);
    }

    /**
     * Валидация товара
     */
    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название товара не может быть пустым");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Цена товара должна быть больше 0");
        }

        if (product.getOldPrice() != null &&
                product.getOldPrice().compareTo(product.getPrice()) <= 0) {
            throw new IllegalArgumentException("Старая цена должна быть больше текущей цены");
        }

        if (product.getCategory() == null) {
            throw new IllegalArgumentException("Категория товара обязательна");
        }
    }

    /**
     * Добавить изображение к товару
     */
    @Transactional
    @Override
    public ProductImage addImageToProduct(Long productId, String imageUrl, Integer displayOrder, String altText) {
        log.info("Добавление изображения к товару с ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден: " + productId));

        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImageUrl(imageUrl);
        productImage.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        productImage.setAltText(altText);

        ProductImage savedImage = productImageRepository.save(productImage);
        log.info("Изображение добавлено к товару {} с ID {}", product.getName(), savedImage.getId());

        return savedImage;
    }

    /**
     * Получить все изображения товара
     */
    @Override
    public List<ProductImage> getProductImages(Long productId) {
        log.debug("Получение изображений для товара с ID: {}", productId);
        return productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
    }

    /**
     * Удалить изображение товара
     */
    @Transactional
    @Override
    public void deleteProductImage(Long productId, Long imageId) {
        log.info("Удаление изображения с ID {} у товара с ID {}", imageId, productId);

        ProductImage image = productImageRepository.findByProductIdAndId(productId, imageId)
                .orElseThrow(() -> new IllegalArgumentException("Изображение не найдено"));

        productImageRepository.delete(image);
        log.info("Изображение удалено");
    }

    /**
     * Обновить порядок отображения изображения
     */
    @Transactional
    @Override
    public ProductImage updateImageOrder(Long productId, Long imageId, Integer newOrder) {
        log.info("Обновление порядка изображения с ID {} у товара с ID {}", imageId, productId);

        ProductImage image = productImageRepository.findByProductIdAndId(productId, imageId)
                .orElseThrow(() -> new IllegalArgumentException("Изображение не найдено"));

        image.setDisplayOrder(newOrder);
        ProductImage updatedImage = productImageRepository.save(image);
        log.info("Порядок изображения обновлен на {}", newOrder);

        return updatedImage;
    }

    /**
     * Обновить изображение товара
     */
    @Transactional
    @Override
    public ProductImage updateProductImage(Long productId, Long imageId, String imageUrl, Integer displayOrder, String altText) {
        log.info("Обновление изображения с ID {} у товара с ID {}", imageId, productId);

        ProductImage image = productImageRepository.findByProductIdAndId(productId, imageId)
                .orElseThrow(() -> new IllegalArgumentException("Изображение не найдено"));

        if (imageUrl != null) {
            image.setImageUrl(imageUrl);
        }
        if (displayOrder != null) {
            image.setDisplayOrder(displayOrder);
        }
        if (altText != null) {
            image.setAltText(altText);
        }

        ProductImage updatedImage = productImageRepository.save(image);
        log.info("Изображение обновлено");

        return updatedImage;
    }

    /**
     * Получить метаданные для фильтров (списки доступных значений)
     */
    @Override
    public FilterMetadataResponse getFilterMetadata() {
        log.info("Получение метаданных для фильтров");

        // Получить все бренды
        List<String> brands = productRepository.findDistinctBrands();

        // Получить диапазон цен
        BigDecimal minPrice = productRepository.findMinPrice();
        BigDecimal maxPrice = productRepository.findMaxPrice();

        // Получить все specifications для извлечения уникальных значений
        List<String> allSpecifications = productRepository.findAllSpecifications();

        // Извлечь уникальные значения из specifications
        Set<String> diameters = new HashSet<>();
        Set<String> pressures = new HashSet<>();
        Set<String> materials = new HashSet<>();
        Set<String> reinforcements = new HashSet<>();
        Set<String> lengths = new HashSet<>();
        Set<String> purposes = new HashSet<>();
        Set<String> wallThicknesses = new HashSet<>();

        ObjectMapper objectMapper = new ObjectMapper();

        for (String spec : allSpecifications) {
            try {
                JsonNode node = objectMapper.readTree(spec);

                // Извлечь диаметры
                if (node.has("diameter")) {
                    diameters.add(node.get("diameter").asText());
                }

                // Извлечь давления
                if (node.has("pressure")) {
                    pressures.add(node.get("pressure").asText());
                }

                // Извлечь материалы
                if (node.has("material")) {
                    materials.add(node.get("material").asText());
                }

                // Извлечь армирование
                if (node.has("reinforcement")) {
                    reinforcements.add(node.get("reinforcement").asText());
                }

                // Извлечь длину
                if (node.has("length")) {
                    lengths.add(node.get("length").asText());
                }

                // Извлечь назначение
                if (node.has("purpose")) {
                    purposes.add(node.get("purpose").asText());
                }

                // Извлечь толщину стенки
                if (node.has("wall_thickness")) {
                    wallThicknesses.add(node.get("wall_thickness").asText());
                }

            } catch (Exception e) {
                log.warn("Ошибка парсинга specifications: {}", e.getMessage());
            }
        }

        // Построить список фильтров
        List<FilterMetadataResponse.FilterItem> filters = new ArrayList<>();

        // Добавить фильтр брендов
        if (!brands.isEmpty()) {
            filters.add(FilterMetadataResponse.FilterItem.builder()
                    .key("brand")
                    .label("Бренд")
                    .list(brands.stream().sorted().collect(Collectors.toList()))
                    .build());
        }

        // Добавить фильтр диаметров
        if (!diameters.isEmpty()) {
            filters.add(FilterMetadataResponse.FilterItem.builder()
                    .key("diameter")
                    .label("Диаметр")
                    .list(diameters.stream().sorted().collect(Collectors.toList()))
                    .build());
        }

        // Добавить фильтр давлений
        if (!pressures.isEmpty()) {
            filters.add(FilterMetadataResponse.FilterItem.builder()
                    .key("pressure")
                    .label("Давление")
                    .list(pressures.stream().sorted().collect(Collectors.toList()))
                    .build());
        }

        // Добавить фильтр материалов
        if (!materials.isEmpty()) {
            filters.add(FilterMetadataResponse.FilterItem.builder()
                    .key("material")
                    .label("Материал")
                    .list(materials.stream().sorted().collect(Collectors.toList()))
                    .build());
        }

        // Добавить фильтр армирования
        if (!reinforcements.isEmpty()) {
            filters.add(FilterMetadataResponse.FilterItem.builder()
                    .key("reinforcement")
                    .label("Армирование")
                    .list(reinforcements.stream().sorted().collect(Collectors.toList()))
                    .build());
        }

        // Добавить фильтр длин
        if (!lengths.isEmpty()) {
            filters.add(FilterMetadataResponse.FilterItem.builder()
                    .key("length")
                    .label("Длина")
                    .list(lengths.stream().sorted().collect(Collectors.toList()))
                    .build());
        }

        // Добавить фильтр назначений
        if (!purposes.isEmpty()) {
            filters.add(FilterMetadataResponse.FilterItem.builder()
                    .key("purpose")
                    .label("Назначение")
                    .list(purposes.stream().sorted().collect(Collectors.toList()))
                    .build());
        }

        // Добавить фильтр толщин стенки
        if (!wallThicknesses.isEmpty()) {
            filters.add(FilterMetadataResponse.FilterItem.builder()
                    .key("wall_thickness")
                    .label("Толщина стенки")
                    .list(wallThicknesses.stream().sorted().collect(Collectors.toList()))
                    .build());
        }

        // Построить ответ
        FilterMetadataResponse response = FilterMetadataResponse.builder()
                .filters(filters)
                .priceRange(FilterMetadataResponse.PriceRange.builder()
                        .min(minPrice != null ? minPrice : BigDecimal.ZERO)
                        .max(maxPrice != null ? maxPrice : BigDecimal.ZERO)
                        .build())
                .build();

        log.info("Метаданные фильтров получены: {} фильтров, {} брендов, {} диаметров, {} давлений",
                filters.size(), brands.size(), diameters.size(), pressures.size());

        return response;
    }
}