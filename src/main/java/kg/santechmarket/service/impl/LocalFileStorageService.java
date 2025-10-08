package kg.santechmarket.service.impl;

import kg.santechmarket.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Реализация хранения файлов на локальной файловой системе
 * <p>
 * Используется для разработки и тестирования
 */
@Service
@ConditionalOnProperty(name = "file-storage.type", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadPath;
    private final String baseUrl;

    public LocalFileStorageService(
            @Value("${file-storage.local.upload-dir}") String uploadDir,
            @Value("${file-storage.local.base-url}") String baseUrl) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;

        try {
            Files.createDirectories(this.uploadPath);
            log.info("Создана директория для загрузки файлов: {}", this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для загрузки файлов", e);
        }
    }

    @Override
    public String store(MultipartFile file, String category) {
        // Валидация файла
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Невозможно сохранить пустой файл");
        }

        // Получение оригинального имени файла
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Проверка на недопустимые символы
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Имя файла содержит недопустимую последовательность: " + originalFilename);
        }

        // Генерация уникального имени файла
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + extension;

        try {
            // Создание директории для категории если не существует
            Path categoryPath = this.uploadPath.resolve(category);
            Files.createDirectories(categoryPath);

            // Сохранение файла
            Path targetLocation = categoryPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Файл сохранён: {}", targetLocation);

            // Возврат относительного пути
            return "/images/" + category + "/" + newFilename;

        } catch (IOException e) {
            log.error("Ошибка сохранения файла: {}", originalFilename, e);
            throw new RuntimeException("Не удалось сохранить файл: " + originalFilename, e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        try {
            // Извлечение относительного пути из URL
            String relativePath = fileUrl.replace("/images/", "");
            Path filePath = this.uploadPath.resolve(relativePath);

            // Удаление файла
            Files.deleteIfExists(filePath);
            log.info("Файл удалён: {}", filePath);

        } catch (IOException e) {
            log.error("Ошибка удаления файла: {}", fileUrl, e);
            throw new RuntimeException("Не удалось удалить файл: " + fileUrl, e);
        }
    }

    @Override
    public boolean exists(String fileUrl) {
        try {
            String relativePath = fileUrl.replace("/images/", "");
            Path filePath = this.uploadPath.resolve(relativePath);
            return Files.exists(filePath);
        } catch (Exception e) {
            log.error("Ошибка проверки существования файла: {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public String getFullUrl(String relativePath) {
        return baseUrl + relativePath;
    }

    /**
     * Получить расширение файла
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }

        return filename.substring(lastDotIndex);
    }
}
