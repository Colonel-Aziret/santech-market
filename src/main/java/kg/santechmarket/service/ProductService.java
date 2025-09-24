package kg.santechmarket.service;

import kg.santechmarket.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс сервиса для работы с товарами
 */
public interface ProductService {

    /**
     * Найти товар по ID
     */
    Optional<Product> findById(Long id);

    /**
     * Найти активный товар по ID
     */
    Optional<Product> findActiveById(Long id);

    /**
     * Получить все активные товары с пагинацией
     */
    Page<Product> findAllActiveProducts(Pageable pageable);

    /**
     * Получить товары по категории
     */
    Page<Product> findProductsByCategory(Long categoryId, Pageable pageable);

    /**
     * Получить рекомендуемые товары для главной страницы
     */
    List<Product> getFeaturedProducts();

    /**
     * Поиск товаров по названию
     */
    Page<Product> searchProductsByName(String searchTerm, Pageable pageable);

    /**
     * Расширенный поиск товаров
     */
    Page<Product> searchProducts(String searchTerm, Pageable pageable);

    /**
     * Комплексная фильтрация товаров
     */
    Page<Product> findProductsWithFilters(Long categoryId, String brand,
                                          BigDecimal minPrice, BigDecimal maxPrice,
                                          String searchTerm, Pageable pageable);

    /**
     * Получить товары со скидкой
     */
    Page<Product> getDiscountedProducts(Pageable pageable);

    /**
     * Получить похожие товары
     */
    List<Product> getSimilarProducts(Long productId, int limit);

    /**
     * Получить все уникальные бренды
     */
    List<String> getAllBrands();

    /**
     * Создать новый товар
     */
    Product createProduct(Product product);

    /**
     * Обновить товар
     */
    Product updateProduct(Long id, Product productUpdate);

    /**
     * Деактивировать товар (мягкое удаление)
     */
    void deactivateProduct(Long id);

    /**
     * Активировать товар
     */
    void activateProduct(Long id);

    /**
     * Установить/убрать товар как рекомендуемый
     */
    void toggleFeatured(Long id);

    /**
     * Поиск товаров по конкретной характеристике
     */
    Page<Product> findBySpecification(String specKey, String specValue, Pageable pageable);

    /**
     * Поиск товаров по нескольким характеристикам (для труб)
     */
    Page<Product> findByMultipleSpecifications(String diameter, String pressure, String material, Pageable pageable);

    /**
     * Получить статистику товаров
     */
    long getTotalProductCount();

    long getActiveProductCount();

    long getProductCountByCategory(Long categoryId);
}