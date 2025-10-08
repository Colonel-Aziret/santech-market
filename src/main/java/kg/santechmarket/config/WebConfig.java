package kg.santechmarket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Конфигурация веб-приложения
 * <p>
 * Настройка раздачи статических файлов (изображений)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file-storage.local.upload-dir}")
    private String uploadDir;

    /**
     * Настройка обработчиков ресурсов для раздачи загруженных изображений
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Получение абсолютного пути к директории загрузок
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

        // Настройка раздачи изображений
        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadPath)
                .setCachePeriod(3600); // Кэширование на 1 час
    }
}
