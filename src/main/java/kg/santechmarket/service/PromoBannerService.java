package kg.santechmarket.service;

import kg.santechmarket.dto.PromoBannerDto;
import kg.santechmarket.entity.PromoBanner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления промо-баннерами
 */
public interface PromoBannerService {

    /**
     * Получить все баннеры для главной страницы (отсортированные по displayOrder)
     */
    List<PromoBannerDto.BannerResponse> getAllBanners();

    /**
     * Получить только активные баннеры
     */
    List<PromoBannerDto.BannerResponse> getActiveBanners();

    /**
     * Получить активные баннеры с учетом текущей даты (для отображения на фронтенде)
     */
    List<PromoBannerDto.BannerResponse> getCurrentActiveBanners();

    /**
     * Получить баннеры с фильтрацией и пагинацией
     */
    Page<PromoBannerDto.BannerResponse> getBannersWithFilters(Boolean isActive, Pageable pageable);

    /**
     * Получить баннер по ID
     */
    Optional<PromoBanner> findById(Long id);

    /**
     * Создать новый баннер
     */
    PromoBanner createBanner(PromoBanner banner);

    /**
     * Обновить баннер
     */
    PromoBanner updateBanner(Long id, PromoBanner bannerUpdate);

    /**
     * Удалить баннер
     */
    void deleteBanner(Long id);

    /**
     * Активировать баннер
     */
    void activateBanner(Long id);

    /**
     * Деактивировать баннер
     */
    void deactivateBanner(Long id);
}