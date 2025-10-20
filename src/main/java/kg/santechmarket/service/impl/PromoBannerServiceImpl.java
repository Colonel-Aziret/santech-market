package kg.santechmarket.service.impl;

import kg.santechmarket.dto.PromoBannerDto;
import kg.santechmarket.entity.PromoBanner;
import kg.santechmarket.repository.PromoBannerRepository;
import kg.santechmarket.service.PromoBannerService;
import kg.santechmarket.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Value("${file-storage.local.base-url}")
    private String baseUrl;

    /**
     * Конвертирует entity в DTO с полными URL
     */
    private PromoBannerDto.BannerResponse toDto(PromoBanner banner) {
        return PromoBannerDto.BannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .subtitle(banner.getSubtitle())
                .imageUrl(UrlUtil.buildFullUrl(banner.getImageUrl(), baseUrl))
                .linkUrl(banner.getLinkUrl())
                .displayOrder(banner.getDisplayOrder())
                .isActive(banner.getIsActive())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .backgroundColor(banner.getBackgroundColor())
                .textColor(banner.getTextColor())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }

    @Override
    public List<PromoBannerDto.BannerResponse> getAllBanners() {
        log.debug("Получение всех баннеров, отсортированных по displayOrder");
        return promoBannerRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<PromoBannerDto.BannerResponse> getActiveBanners() {
        log.debug("Получение активных баннеров");
        return promoBannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<PromoBannerDto.BannerResponse> getCurrentActiveBanners() {
        log.debug("Получение активных баннеров с учетом текущей даты");
        return promoBannerRepository.findActiveAndCurrentBanners(LocalDateTime.now())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Page<PromoBannerDto.BannerResponse> getBannersWithFilters(Boolean isActive, Pageable pageable) {
        log.debug("Получение баннеров с фильтрами: isActive={}", isActive);
        return promoBannerRepository.findWithFilters(isActive, pageable)
                .map(this::toDto);
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
                    if (bannerUpdate.getTitle() != null) {
                        existingBanner.setTitle(bannerUpdate.getTitle());
                    }
                    if (bannerUpdate.getSubtitle() != null) {
                        existingBanner.setSubtitle(bannerUpdate.getSubtitle());
                    }
                    if (bannerUpdate.getImageUrl() != null) {
                        existingBanner.setImageUrl(bannerUpdate.getImageUrl());
                    }
                    if (bannerUpdate.getLinkUrl() != null) {
                        existingBanner.setLinkUrl(bannerUpdate.getLinkUrl());
                    }
                    if (bannerUpdate.getDisplayOrder() != null) {
                        existingBanner.setDisplayOrder(bannerUpdate.getDisplayOrder());
                    }
                    if (bannerUpdate.getIsActive() != null) {
                        existingBanner.setIsActive(bannerUpdate.getIsActive());
                    }
                    if (bannerUpdate.getStartDate() != null) {
                        existingBanner.setStartDate(bannerUpdate.getStartDate());
                    }
                    if (bannerUpdate.getEndDate() != null) {
                        existingBanner.setEndDate(bannerUpdate.getEndDate());
                    }
                    if (bannerUpdate.getBackgroundColor() != null) {
                        existingBanner.setBackgroundColor(bannerUpdate.getBackgroundColor());
                    }
                    if (bannerUpdate.getTextColor() != null) {
                        existingBanner.setTextColor(bannerUpdate.getTextColor());
                    }

                    return promoBannerRepository.save(existingBanner);
                })
                .orElseThrow(() -> new RuntimeException("Баннер с ID " + id + " не найден"));
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
}