package kg.santechmarket.repository;

import kg.santechmarket.entity.Category;
import kg.santechmarket.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с товарами
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Найти товар по артикулу
     */
    Optional<Product> findBySku(String sku);

    /**
     * Получить все активные товары
     */
    Page<Product> findByIsActiveTrue(Pageable pageable);

    /**
     * Получить рекомендуемые товары для главной страницы
     */
    List<Product> findByIsActiveTrueAndIsFeaturedTrueOrderByCreatedAtDesc();

    /**
     * Получить товары по категории
     */
    Page<Product> findByCategoryAndIsActiveTrue(Category category, Pageable pageable);

    /**
     * Получить товары по ID категории
     */
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    /**
     * Получить товары по бренду
     */
    Page<Product> findByBrandAndIsActiveTrue(String brand, Pageable pageable);

    /**
     * Поиск товаров по названию
     */
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "AND p.isActive = true")
    Page<Product> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Расширенный поиск товаров (по названию, бренду, описанию, характеристикам)
     */
    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.specifications) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND p.isActive = true")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Фильтрация товаров по цене
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.isActive = true " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);

    /**
     * Комплексная фильтрация товаров
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.isActive = true " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:searchTerm IS NULL OR " +
            "    LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "    LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "    LOWER(p.specifications) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> findProductsWithFilters(@Param("categoryId") Long categoryId,
                                          @Param("brand") String brand,
                                          @Param("minPrice") BigDecimal minPrice,
                                          @Param("maxPrice") BigDecimal maxPrice,
                                          @Param("searchTerm") String searchTerm,
                                          Pageable pageable);

    /**
     * Получить все уникальные бренды активных товаров
     */
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.isActive = true AND p.brand IS NOT NULL ORDER BY p.brand")
    List<String> findDistinctBrands();

    /**
     * Товары со скидкой (есть старая цена)
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.oldPrice IS NOT NULL AND p.oldPrice > p.price")
    Page<Product> findDiscountedProducts(Pageable pageable);

    /**
     * Получить количество товаров в категории
     */
    long countByCategoryIdAndIsActiveTrue(Long categoryId);

    /**
     * Получить количество активных товаров
     */
    long countByIsActiveTrue();

    /**
     * Поиск товаров по конкретной характеристике (например, диаметр: 20)
     */
    @Query(value = "SELECT * FROM products p WHERE " +
            "p.is_active = true " +
            "AND p.specifications::text LIKE CONCAT('%\"', :specKey, '\": \"', :specValue, '\"%')",
            nativeQuery = true)
    Page<Product> findBySpecification(@Param("specKey") String specKey,
                                      @Param("specValue") String specValue,
                                      Pageable pageable);

    /**
     * Поиск товаров по нескольким характеристикам
     */
    @Query(value = "SELECT * FROM products p WHERE " +
            "p.is_active = true " +
            "AND (:diameter IS NULL OR p.specifications::text LIKE CONCAT('%\"diameter\": \"', :diameter, '%')) " +
            "AND (:pressure IS NULL OR p.specifications::text LIKE CONCAT('%\"pressure\": \"', :pressure, '%')) " +
            "AND (:material IS NULL OR p.specifications::text LIKE CONCAT('%\"material\": \"', :material, '%'))",
            nativeQuery = true)
    Page<Product> findByMultipleSpecifications(@Param("diameter") String diameter,
                                               @Param("pressure") String pressure,
                                               @Param("material") String material,
                                               Pageable pageable);

    /**
     * Похожие товары (той же категории, исключая текущий)
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.category.id = :categoryId " +
            "AND p.id != :excludeId " +
            "AND p.isActive = true " +
            "ORDER BY p.createdAt DESC")
    List<Product> findSimilarProducts(@Param("categoryId") Long categoryId,
                                      @Param("excludeId") Long excludeId,
                                      Pageable pageable);

    /**
     * Получить товары по категории включая все подкатегории
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.isActive = true " +
            "AND (p.category.id = :categoryId OR p.category.parent.id = :categoryId)")
    Page<Product> findByCategoryIdIncludingSubcategories(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Получить минимальную цену среди активных товаров
     */
    @Query("SELECT MIN(p.price) FROM Product p WHERE p.isActive = true")
    BigDecimal findMinPrice();

    /**
     * Получить максимальную цену среди активных товаров
     */
    @Query("SELECT MAX(p.price) FROM Product p WHERE p.isActive = true")
    BigDecimal findMaxPrice();

    /**
     * Получить все specifications активных товаров для извлечения уникальных значений
     */
    @Query("SELECT p.specifications FROM Product p WHERE p.isActive = true AND p.specifications IS NOT NULL")
    List<String> findAllSpecifications();
}