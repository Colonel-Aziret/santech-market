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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promo-banners")
@RequiredArgsConstructor
@Tag(name = "Промо-баннеры", description = "API для управления промо-баннерами на главной странице")
public class PromoBannerController {

    private final PromoBannerService promoBannerService;

    @GetMapping
    @Operation(
            summary = "Получить все баннеры с фильтрацией",
            description = """
                    Возвращает список всех промо-баннеров с возможностью фильтрации и сортировки.

                    Поддерживаемые фильтры:
                    - isActive - фильтр по активности (true/false, опционально)

                    Без параметра isActive возвращает все баннеры.
                    Поддерживает пагинацию и сортировку.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Успешно получен список баннеров")
    public ResponseEntity<Page<PromoBannerDto.BannerResponse>> getAllBannersWithFilters(
            @Parameter(description = "Фильтр по активности (true - только активные, false - только неактивные, null - все)")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Параметры пагинации и сортировки")
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<PromoBannerDto.BannerResponse> banners = promoBannerService.getBannersWithFilters(isActive, pageable);
        return ResponseEntity.ok(banners);
    }

    @GetMapping("/active")
    @Operation(
            summary = "Получить только активные баннеры",
            description = "Возвращает список только активных баннеров (isActive = true), отсортированных по displayOrder"
    )
    @ApiResponse(responseCode = "200", description = "Успешно получен список активных баннеров")
    public ResponseEntity<List<PromoBannerDto.BannerResponse>> getActiveBanners() {
        List<PromoBannerDto.BannerResponse> banners = promoBannerService.getActiveBanners();
        return ResponseEntity.ok(banners);
    }

    @GetMapping("/current")
    @Operation(
            summary = "Получить текущие активные баннеры",
            description = "Возвращает список баннеров, которые активны И находятся в пределах дат показа (startDate <= now <= endDate)"
    )
    @ApiResponse(responseCode = "200", description = "Успешно получен список текущих баннеров")
    public ResponseEntity<List<PromoBannerDto.BannerResponse>> getCurrentActiveBanners() {
        List<PromoBannerDto.BannerResponse> banners = promoBannerService.getCurrentActiveBanners();
        return ResponseEntity.ok(banners);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить баннер по ID", description = "Возвращает информацию о баннере по его идентификатору")
    @ApiResponse(responseCode = "200", description = "Баннер найден")
    @ApiResponse(responseCode = "404", description = "Баннер не найден")
    public ResponseEntity<PromoBanner> getBannerById(@Parameter(description = "ID баннера") @PathVariable Long id) {
        return promoBannerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Создать баннер", description = "Создает новый промо-баннер")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @ApiResponse(responseCode = "200", description = "Баннер успешно создан")
    @ApiResponse(responseCode = "401", description = "Не авторизован")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    public ResponseEntity<PromoBanner> createBanner(@Valid @RequestBody PromoBanner banner) {
        PromoBanner createdBanner = promoBannerService.createBanner(banner);
        return ResponseEntity.ok(createdBanner);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить баннер", description = "Обновляет информацию о баннере")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @ApiResponse(responseCode = "200", description = "Баннер успешно обновлен")
    @ApiResponse(responseCode = "401", description = "Не авторизован")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @ApiResponse(responseCode = "404", description = "Баннер не найден")
    public ResponseEntity<PromoBanner> updateBanner(
            @Parameter(description = "ID баннера") @PathVariable Long id,
            @Valid @RequestBody PromoBanner bannerUpdate) {
        PromoBanner updatedBanner = promoBannerService.updateBanner(id, bannerUpdate);
        return ResponseEntity.ok(updatedBanner);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Активировать баннер", description = "Активирует баннер (устанавливает isActive = true)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @ApiResponse(responseCode = "200", description = "Баннер активирован")
    @ApiResponse(responseCode = "401", description = "Не авторизован")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @ApiResponse(responseCode = "404", description = "Баннер не найден")
    public ResponseEntity<Void> activateBanner(@Parameter(description = "ID баннера") @PathVariable Long id) {
        promoBannerService.activateBanner(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Деактивировать баннер", description = "Деактивирует баннер (устанавливает isActive = false)")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @ApiResponse(responseCode = "200", description = "Баннер деактивирован")
    @ApiResponse(responseCode = "401", description = "Не авторизован")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @ApiResponse(responseCode = "404", description = "Баннер не найден")
    public ResponseEntity<Void> deactivateBanner(@Parameter(description = "ID баннера") @PathVariable Long id) {
        promoBannerService.deactivateBanner(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить баннер", description = "Полностью удаляет баннер из базы данных")
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "200", description = "Баннер удален")
    @ApiResponse(responseCode = "401", description = "Не авторизован")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется ADMIN)")
    @ApiResponse(responseCode = "404", description = "Баннер не найден")
    public ResponseEntity<Void> deleteBanner(@Parameter(description = "ID баннера") @PathVariable Long id) {
        promoBannerService.deleteBanner(id);
        return ResponseEntity.ok().build();
    }
}