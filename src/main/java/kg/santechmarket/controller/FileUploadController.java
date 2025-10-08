package kg.santechmarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.santechmarket.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * REST контроллер для загрузки файлов (изображений)
 * <p>
 * Endpoints:
 * - POST /upload/image - загрузить изображение
 * - DELETE /upload/image - удалить изображение
 */
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Upload", description = "API для загрузки и управления файлами")
@SecurityRequirement(name = "JWT")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    // Разрешённые типы файлов
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    // Максимальный размер файла (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * Загрузить изображение
     *
     * @param file     файл изображения
     * @param category категория (products, categories, banners)
     * @return URL загруженного изображения
     */
    @PostMapping("/image")
    @Operation(summary = "Загрузить изображение", description = "Загружает изображение и возвращает URL для доступа")
    @ApiResponse(responseCode = "200", description = "Изображение успешно загружено")
    @ApiResponse(responseCode = "400", description = "Неверный формат файла или размер превышен")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> uploadImage(
            @Parameter(description = "Файл изображения", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Категория: products, categories, banners", required = true)
            @RequestParam("category") String category) {

        log.info("Запрос на загрузку изображения. Категория: {}, Имя файла: {}, Размер: {} байт",
                category, file.getOriginalFilename(), file.getSize());

        try {
            // Валидация файла
            validateFile(file);

            // Валидация категории
            validateCategory(category);

            // Сохранение файла
            String fileUrl = fileStorageService.store(file, category);

            // Получение полного URL
            String fullUrl = fileStorageService.getFullUrl(fileUrl);

            log.info("Изображение успешно загружено: {}", fullUrl);

            return ResponseEntity.ok(new UploadResponse(fileUrl, fullUrl));

        } catch (IllegalArgumentException e) {
            log.warn("Ошибка валидации файла: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new kg.santechmarket.dto.ErrorResponse(e.getMessage(), kg.santechmarket.enums.ErrorCode.FILE_INVALID_TYPE.getCode()));

        } catch (Exception e) {
            log.error("Ошибка загрузки файла", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new kg.santechmarket.dto.ErrorResponse("Не удалось загрузить файл: " + e.getMessage(),
                            kg.santechmarket.enums.ErrorCode.FILE_UPLOAD_FAILED.getCode()));
        }
    }

    /**
     * Удалить изображение
     *
     * @param fileUrl URL файла для удаления
     * @return статус операции
     */
    @DeleteMapping("/image")
    @Operation(summary = "Удалить изображение", description = "Удаляет изображение по URL")
    @ApiResponse(responseCode = "200", description = "Изображение успешно удалено")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> deleteImage(
            @Parameter(description = "URL изображения для удаления", required = true)
            @RequestParam("fileUrl") String fileUrl) {

        log.info("Запрос на удаление изображения: {}", fileUrl);

        try {
            fileStorageService.delete(fileUrl);
            log.info("Изображение успешно удалено: {}", fileUrl);
            return ResponseEntity.ok(new SuccessResponse("Изображение успешно удалено"));

        } catch (Exception e) {
            log.error("Ошибка удаления файла: {}", fileUrl, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new kg.santechmarket.dto.ErrorResponse("Не удалось удалить файл: " + e.getMessage(),
                            kg.santechmarket.enums.ErrorCode.FILE_DELETE_FAILED.getCode()));
        }
    }

    /**
     * Валидация загружаемого файла
     */
    private void validateFile(MultipartFile file) {
        // Проверка на пустой файл
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }

        // Проверка размера
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Размер файла превышает максимально допустимый (%d МБ)", MAX_FILE_SIZE / 1024 / 1024)
            );
        }

        // Проверка типа файла
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Недопустимый тип файла. Разрешены: " + String.join(", ", ALLOWED_CONTENT_TYPES)
            );
        }
    }

    /**
     * Валидация категории
     */
    private void validateCategory(String category) {
        List<String> allowedCategories = Arrays.asList("products", "categories", "banners");

        if (!allowedCategories.contains(category.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Недопустимая категория. Разрешены: " + String.join(", ", allowedCategories)
            );
        }
    }

    /**
     * DTO для ответа при успешной загрузке
     */
    public record UploadResponse(String relativePath, String fullUrl) {
    }

    /**
     * DTO для успешных операций
     */
    public record SuccessResponse(String message) {
    }
}
