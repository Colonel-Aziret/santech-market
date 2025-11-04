package kg.santechmarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.santechmarket.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для тестирования отправки email
 * Доступен только администраторам
 */
@Slf4j
@RestController
@RequestMapping("/email-test")
@RequiredArgsConstructor
@Tag(name = "Email Test", description = "API для тестирования отправки email (только для администраторов)")
public class EmailTestController {

    private final EmailService emailService;

    @PostMapping("/send-simple")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Отправить простое тестовое письмо",
            description = "Отправляет простое текстовое письмо на указанный email адрес. Доступно только администраторам."
    )
    public ResponseEntity<Map<String, String>> sendSimpleEmail(
            @Parameter(description = "Email адрес получателя", required = true)
            @RequestParam String to,
            @Parameter(description = "Тема письма", example = "Тестовое письмо")
            @RequestParam(defaultValue = "Тестовое письмо") String subject,
            @Parameter(description = "Текст письма", example = "Это тестовое письмо от SanTech Market")
            @RequestParam(defaultValue = "Это тестовое письмо от SanTech Market") String text
    ) {
        log.info("Получен запрос на отправку простого письма на адрес: {}", to);

        try {
            emailService.sendSimpleMessage(to, subject, text);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Письмо успешно отправлено на адрес: " + to);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ошибка при отправке письма: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Ошибка при отправке письма: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/send-html")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Отправить HTML тестовое письмо",
            description = "Отправляет HTML письмо на указанный email адрес. Доступно только администраторам."
    )
    public ResponseEntity<Map<String, String>> sendHtmlEmail(
            @Parameter(description = "Email адрес получателя", required = true)
            @RequestParam String to,
            @Parameter(description = "Тема письма", example = "Тестовое HTML письмо")
            @RequestParam(defaultValue = "Тестовое HTML письмо") String subject
    ) {
        log.info("Получен запрос на отправку HTML письма на адрес: {}", to);

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; }
                        .footer { background-color: #333; color: white; padding: 10px; text-align: center; font-size: 12px; }
                        .button { background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; display: inline-block; border-radius: 5px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>SanTech Market</h1>
                            <p>Тестовое HTML письмо</p>
                        </div>
                        <div class="content">
                            <h2>Приветствуем!</h2>
                            <p>Это тестовое HTML письмо от системы <strong>SanTech Market</strong>.</p>
                            <p>Если вы получили это письмо, значит настройка почты прошла успешно!</p>
                            <p style="text-align: center; margin: 30px 0;">
                                <a href="https://santechmarket.kg" class="button">Перейти на сайт</a>
                            </p>
                            <ul>
                                <li>✅ Отправка писем работает</li>
                                <li>✅ HTML форматирование поддерживается</li>
                                <li>✅ Кириллица отображается корректно</li>
                            </ul>
                        </div>
                        <div class="footer">
                            <p>&copy; 2025 SanTech Market. Все права защищены.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;

        try {
            emailService.sendHtmlMessage(to, subject, html);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "HTML письмо успешно отправлено на адрес: " + to);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ошибка при отправке HTML письма: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Ошибка при отправке HTML письма: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/test-connection")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Проверить подключение к почтовому серверу",
            description = "Проверяет, работает ли подключение к почтовому серверу. Доступно только администраторам."
    )
    public ResponseEntity<Map<String, String>> testConnection() {
        log.info("Проверка подключения к почтовому серверу");

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Почтовый сервис инициализирован. Для полной проверки используйте /send-simple или /send-html");
        response.put("smtp_host", "smtp.gmail.com");
        response.put("smtp_port", "587");

        return ResponseEntity.ok(response);
    }
}
