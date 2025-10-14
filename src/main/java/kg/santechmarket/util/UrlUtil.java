package kg.santechmarket.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

/**
 * Утилита для работы с URL
 */
@UtilityClass
public class UrlUtil {

    /**
     * Строит полный URL из относительного пути
     * Если путь уже содержит протокол (http/https), возвращает его без изменений
     *
     * @param path относительный путь или полный URL
     * @param baseUrl базовый URL (например, http://localhost:8080/api/v1)
     * @return полный URL
     */
    public static String buildFullUrl(String path, String baseUrl) {
        if (!StringUtils.hasText(path)) {
            return null;
        }

        // Если URL уже содержит протокол, возвращаем как есть
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }

        // Если нет базового URL, возвращаем путь как есть
        if (!StringUtils.hasText(baseUrl)) {
            return path;
        }

        // Убираем слэш в конце baseUrl и в начале path
        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String cleanPath = path.startsWith("/") ? path : "/" + path;

        return cleanBaseUrl + cleanPath;
    }
}
