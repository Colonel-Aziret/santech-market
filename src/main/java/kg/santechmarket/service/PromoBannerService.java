package kg.santechmarket.service;

import kg.santechmarket.dto.PromoBannerDto;
import kg.santechmarket.entity.PromoBanner;

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
}