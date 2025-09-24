package kg.santechmarket.repository;

import kg.santechmarket.entity.PromoBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromoBannerRepository extends JpaRepository<PromoBanner, Long> {

    /**
     * Найти все активные баннеры, отсортированные по порядку отображения
     */
    List<PromoBanner> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Найти активные баннеры, которые должны показываться в данный момент
     */
    @Query("SELECT p FROM PromoBanner p WHERE p.isActive = true " +
           "AND (p.startDate IS NULL OR p.startDate <= :currentTime) " +
           "AND (p.endDate IS NULL OR p.endDate >= :currentTime) " +
           "ORDER BY p.displayOrder ASC")
    List<PromoBanner> findActiveBannersForCurrentTime(LocalDateTime currentTime);

    /**
     * Подсчитать количество активных баннеров
     */
    long countByIsActiveTrue();
}