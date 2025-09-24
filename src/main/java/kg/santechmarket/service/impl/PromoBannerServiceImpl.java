package kg.santechmarket.service.impl;

import kg.santechmarket.entity.PromoBanner;
import kg.santechmarket.repository.PromoBannerRepository;
import kg.santechmarket.service.PromoBannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PromoBannerServiceImpl implements PromoBannerService {

    private final PromoBannerRepository promoBannerRepository;

    @Override
    public List<PromoBanner> getActiveBanners() {
        log.debug("Получение всех активных баннеров");
        return promoBannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Override
    public List<PromoBanner> getCurrentBanners() {
        log.debug("Получение текущих активных баннеров");
        LocalDateTime now = LocalDateTime.now();
        return promoBannerRepository.findActiveBannersForCurrentTime(now);
    }

    @Override
    public Optional<PromoBanner> findById(Long id) {
        log.debug("Поиск баннера по ID: {}", id);
        return promoBannerRepository.findById(id);
    }

    @Override
    @Transactional
    public PromoBanner createBanner(PromoBanner banner) {
        log.info("Создание нового баннера: {}", banner.getTitle());
        return promoBannerRepository.save(banner);
    }

    @Override
    @Transactional
    public PromoBanner updateBanner(Long id, PromoBanner bannerUpdate) {
        log.info("Обновление баннера с ID: {}", id);

        return promoBannerRepository.findById(id)
                .map(existingBanner -> {
                    existingBanner.setTitle(bannerUpdate.getTitle());
                    existingBanner.setSubtitle(bannerUpdate.getSubtitle());
                    existingBanner.setImageUrl(bannerUpdate.getImageUrl());
                    existingBanner.setLinkUrl(bannerUpdate.getLinkUrl());
                    existingBanner.setDisplayOrder(bannerUpdate.getDisplayOrder());
                    existingBanner.setStartDate(bannerUpdate.getStartDate());
                    existingBanner.setEndDate(bannerUpdate.getEndDate());
                    existingBanner.setBackgroundColor(bannerUpdate.getBackgroundColor());
                    existingBanner.setTextColor(bannerUpdate.getTextColor());

                    return promoBannerRepository.save(existingBanner);
                })
                .orElseThrow(() -> new RuntimeException("Баннер с ID " + id + " не найден"));
    }

    @Override
    @Transactional
    public void activateBanner(Long id) {
        log.info("Активация баннера с ID: {}", id);

        promoBannerRepository.findById(id)
                .ifPresentOrElse(
                        banner -> {
                            banner.setIsActive(true);
                            promoBannerRepository.save(banner);
                        },
                        () -> {
                            throw new RuntimeException("Баннер с ID " + id + " не найден");
                        }
                );
    }

    @Override
    @Transactional
    public void deactivateBanner(Long id) {
        log.info("Деактивация баннера с ID: {}", id);

        promoBannerRepository.findById(id)
                .ifPresentOrElse(
                        banner -> {
                            banner.setIsActive(false);
                            promoBannerRepository.save(banner);
                        },
                        () -> {
                            throw new RuntimeException("Баннер с ID " + id + " не найден");
                        }
                );
    }

    @Override
    @Transactional
    public void deleteBanner(Long id) {
        log.info("Удаление баннера с ID: {}", id);

        if (!promoBannerRepository.existsById(id)) {
            throw new RuntimeException("Баннер с ID " + id + " не найден");
        }

        promoBannerRepository.deleteById(id);
    }

    @Override
    public long getActiveBannerCount() {
        log.debug("Подсчет количества активных баннеров");
        return promoBannerRepository.countByIsActiveTrue();
    }

    @Override
    public List<PromoBanner> getAllBanners() {
        log.debug("Получение всех баннеров");
        return promoBannerRepository.findAll();
    }
}