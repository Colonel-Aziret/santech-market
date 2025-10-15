package kg.santechmarket.service.impl;

import kg.santechmarket.dto.PromoBannerDto;
import kg.santechmarket.entity.PromoBanner;
import kg.santechmarket.repository.PromoBannerRepository;
import kg.santechmarket.service.PromoBannerService;
import kg.santechmarket.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .imageUrl(UrlUtil.buildFullUrl(banner.getImageUrl(), baseUrl))
                .displayOrder(banner.getDisplayOrder())
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
    public Optional<PromoBanner> findById(Long id) {
        log.debug("Поиск баннера по ID: {}", id);
        return promoBannerRepository.findById(id);
    }

    @Override
    @Transactional
    public PromoBanner createBanner(PromoBanner banner) {
        log.info("Создание нового баннера с URL: {}", banner.getImageUrl());
        return promoBannerRepository.save(banner);
    }

    @Override
    @Transactional
    public PromoBanner updateBanner(Long id, PromoBanner bannerUpdate) {
        log.info("Обновление баннера с ID: {}", id);

        return promoBannerRepository.findById(id)
                .map(existingBanner -> {
                    if (bannerUpdate.getImageUrl() != null) {
                        existingBanner.setImageUrl(bannerUpdate.getImageUrl());
                    }
                    if (bannerUpdate.getDisplayOrder() != null) {
                        existingBanner.setDisplayOrder(bannerUpdate.getDisplayOrder());
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
}