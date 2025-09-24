package kg.santechmarket.service;

import kg.santechmarket.entity.PromoBanner;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления промо-баннерами
 */
public interface PromoBannerService {

    /**
     * Получить все активные баннеры для главной страницы
     */
    List<PromoBanner> getActiveBanners();

    /**
     * Получить все активные баннеры с учетом временных рамок
     */
    List<PromoBanner> getCurrentBanners();

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
     * Активировать баннер
     */
    void activateBanner(Long id);

    /**
     * Деактивировать баннер
     */
    void deactivateBanner(Long id);

    /**
     * Удалить баннер
     */
    void deleteBanner(Long id);

    /**
     * Получить количество активных баннеров
     */
    long getActiveBannerCount();

    /**
     * Получить все баннеры (для админки)
     */
    List<PromoBanner> getAllBanners();
}