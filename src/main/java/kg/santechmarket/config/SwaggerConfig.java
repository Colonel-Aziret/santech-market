package kg.santechmarket.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SanTech Market API",
                version = "1.0.0",
                description = """
                        REST API для системы электронной коммерции SanTech Market.

                        Основные возможности:
                        • Управление пользователями с ролевым доступом
                        • Каталог товаров с категориями и фильтрацией
                        • Корзина покупок
                        • Система заказов с отслеживанием статуса
                        • JWT аутентификация

                        Роли пользователей:
                        • CLIENT - клиент магазина
                        • MANAGER - менеджер по продажам
                        • ADMIN - администратор системы
                        """,
                contact = @Contact(
                        name = "SanTech Market Support",
                        email = "support@santechmarket.kg"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        description = "Development Server",
                        url = "http://localhost:8080/api/v1"
                ),
                @Server(
                        description = "Local Server (Port 8083)",
                        url = "http://localhost:8083/api/v1"
                )
        }
)
@SecurityScheme(
        name = "JWT",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = """
                JWT токен для аутентификации.

                Получить токен можно через endpoint /auth/login

                Пример использования:
                Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
                """
)
public class SwaggerConfig {
}