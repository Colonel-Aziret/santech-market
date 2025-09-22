package kg.santechmarket.service.impl;

import kg.santechmarket.entity.Category;
import kg.santechmarket.entity.Product;
import kg.santechmarket.repository.CategoryRepository;
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
import java.util.List;
import java.util.Optional;

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
        existingProduct.setAdditionalImages(productUpdate.getAdditionalImages());
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
}