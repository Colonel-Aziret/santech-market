package kg.santechmarket.repository;

import kg.santechmarket.entity.PromoBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoBannerRepository extends JpaRepository<PromoBanner, Long> {

    /**
     * Найти все баннеры, отсортированные по порядку отображения
     */
    List<PromoBanner> findAllByOrderByDisplayOrderAsc();
}