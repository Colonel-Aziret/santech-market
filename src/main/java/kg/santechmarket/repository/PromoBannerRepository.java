package kg.santechmarket.repository;

import kg.santechmarket.entity.PromoBanner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromoBannerRepository extends JpaRepository<PromoBanner, Long> {

    /**
     * Найти все баннеры, отсортированные по порядку отображения
     */
    List<PromoBanner> findAllByOrderByDisplayOrderAsc();

    /**
     * Найти только активные баннеры, отсортированные по порядку отображения
     */
    List<PromoBanner> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Найти активные баннеры с учетом даты начала и окончания
     */
    @Query("SELECT b FROM PromoBanner b WHERE b.isActive = true " +
            "AND (b.startDate IS NULL OR b.startDate <= :now) " +
            "AND (b.endDate IS NULL OR b.endDate >= :now) " +
            "ORDER BY b.displayOrder ASC")
    List<PromoBanner> findActiveAndCurrentBanners(@Param("now") LocalDateTime now);

    /**
     * Найти баннеры с фильтрацией и пагинацией
     */
    @Query("SELECT b FROM PromoBanner b WHERE " +
            "(:isActive IS NULL OR b.isActive = :isActive) " +
            "ORDER BY b.displayOrder ASC, b.createdAt DESC")
    Page<PromoBanner> findWithFilters(
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}