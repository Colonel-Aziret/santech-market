package kg.santechmarket.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Интерфейс сервиса для хранения файлов
 * <p>
 * Поддерживает две реализации:
 * - LocalFileStorageService (для разработки)
 * - S3FileStorageService (для продакшна)
 */
public interface FileStorageService {

    /**
     * Сохранить файл
     *
     * @param file     загружаемый файл
     * @param category категория файла (products, categories, banners)
     * @return URL загруженного файла
     */
    String store(MultipartFile file, String category);

    /**
     * Удалить файл по URL
     *
     * @param fileUrl URL файла
     */
    void delete(String fileUrl);

    /**
     * Проверить существование файла
     *
     * @param fileUrl URL файла
     * @return true если файл существует
     */
    boolean exists(String fileUrl);

    /**
     * Получить полный URL для доступа к файлу
     *
     * @param relativePath относительный путь к файлу
     * @return полный URL
     */
    String getFullUrl(String relativePath);
}
