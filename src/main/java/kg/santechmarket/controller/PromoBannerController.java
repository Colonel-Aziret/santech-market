package kg.santechmarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.santechmarket.dto.PromoBannerDto;
import kg.santechmarket.entity.PromoBanner;
import kg.santechmarket.service.PromoBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promo-banners")
@RequiredArgsConstructor
@Tag(name = "Promo Banner Management", description = "API для управления промо-баннерами")
public class PromoBannerController {

    private final PromoBannerService promoBannerService;

    @GetMapping
    @Operation(summary = "Получить все активные баннеры", description = "Возвращает список активных промо-баннеров для главной страницы")
    @ApiResponse(responseCode = "200", description = "Успешно получен список баннеров")
    public ResponseEntity<List<PromoBannerDto.BannerResponse>> getActiveBanners() {
        List<PromoBannerDto.BannerResponse> banners = promoBannerService.getCurrentBanners();
        return ResponseEntity.ok(banners);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить баннер по ID", description = "Возвращает информацию о баннере по его идентификатору")
    public ResponseEntity<PromoBanner> getBannerById(@Parameter(description = "ID баннера") @PathVariable Long id) {
        return promoBannerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    @Operation(summary = "Получить все баннеры", description = "Возвращает список всех баннеров (для админки)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<PromoBannerDto.BannerResponse>> getAllBanners() {
        List<PromoBannerDto.BannerResponse> banners = promoBannerService.getAllBanners();
        return ResponseEntity.ok(banners);
    }

    @PostMapping
    @Operation(summary = "Создать баннер", description = "Создает новый промо-баннер")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PromoBanner> createBanner(@Valid @RequestBody PromoBanner banner) {
        PromoBanner createdBanner = promoBannerService.createBanner(banner);
        return ResponseEntity.ok(createdBanner);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить баннер", description = "Обновляет информацию о баннере")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PromoBanner> updateBanner(
            @Parameter(description = "ID баннера") @PathVariable Long id,
            @Valid @RequestBody PromoBanner bannerUpdate) {
        PromoBanner updatedBanner = promoBannerService.updateBanner(id, bannerUpdate);
        return ResponseEntity.ok(updatedBanner);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Активировать баннер", description = "Активирует баннер")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> activateBanner(@Parameter(description = "ID баннера") @PathVariable Long id) {
        promoBannerService.activateBanner(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Деактивировать баннер", description = "Деактивирует баннер")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deactivateBanner(@Parameter(description = "ID баннера") @PathVariable Long id) {
        promoBannerService.deactivateBanner(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить баннер", description = "Полностью удаляет баннер")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBanner(@Parameter(description = "ID баннера") @PathVariable Long id) {
        promoBannerService.deleteBanner(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/active-count")
    @Operation(summary = "Получить количество активных баннеров", description = "Возвращает количество активных баннеров")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Long> getActiveBannerCount() {
        long count = promoBannerService.getActiveBannerCount();
        return ResponseEntity.ok(count);
    }
}