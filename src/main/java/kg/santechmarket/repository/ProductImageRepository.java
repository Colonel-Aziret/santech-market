package kg.santechmarket.repository;

import kg.santechmarket.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с изображениями товаров
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /**
     * Найти все изображения товара, отсортированные по порядку отображения
     */
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);

    /**
     * Найти изображение товара по ID товара и ID изображения
     */
    Optional<ProductImage> findByProductIdAndId(Long productId, Long imageId);

    /**
     * Удалить все изображения товара
     */
    void deleteByProductId(Long productId);

    /**
     * Подсчитать количество изображений у товара
     */
    long countByProductId(Long productId);
}
