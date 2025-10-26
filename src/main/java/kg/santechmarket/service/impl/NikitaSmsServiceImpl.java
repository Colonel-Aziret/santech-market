package kg.santechmarket.service.impl;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import kg.santechmarket.dto.SmsDto;
import kg.santechmarket.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

/**
 * Реализация сервиса отправки SMS через Nikita Mobile API
 * <p>
 * Использует XML-based API для отправки SMS сообщений
 * Поддерживает:
 * - Отправку реальных SMS
 * - Тестовый режим
 * - Полное логирование всех операций
 */
@Service
@Slf4j
public class NikitaSmsServiceImpl implements SmsService {

    private final RestTemplate restTemplate;
    private final JAXBContext jaxbContext;

    @Value("${app.nikita-sms.enabled:false}")
    private boolean enabled;

    @Value("${app.nikita-sms.test-mode:true}")
    private boolean testMode;

    @Value("${app.nikita-sms.api-url}")
    private String apiUrl;

    @Value("${app.nikita-sms.login}")
    private String login;

    @Value("${app.nikita-sms.password}")
    private String password;

    @Value("${app.nikita-sms.sender}")
    private String sender;

    public NikitaSmsServiceImpl() {
        this.restTemplate = new RestTemplate();
        try {
            this.jaxbContext = JAXBContext.newInstance(SmsDto.SmsRequest.class, SmsDto.SmsResponse.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Ошибка инициализации JAXB контекста для SMS", e);
        }
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        return sendSms(phoneNumber, message, UUID.randomUUID().toString());
    }

    @Override
    public boolean sendSms(String phoneNumber, String message, String messageId) {
        // Проверяем включен ли сервис
        if (!enabled) {
            log.info("SMS сервис отключен. Сообщение не отправлено: {} -> {}", phoneNumber, message);
            return false;
        }

        // Валидация входных данных
        validateInput(phoneNumber, message);

        try {
            // Создаем XML запрос
            SmsDto.SmsRequest request = buildSmsRequest(phoneNumber, message, messageId);

            // Логируем отправку
            log.info("Отправка SMS: номер={}, messageId={}, testMode={}",
                     maskPhoneNumber(phoneNumber), messageId, testMode);

            // Маршалим запрос в XML
            String requestXml = marshalRequest(request);
            log.debug("XML запрос: {}", requestXml);

            // Отправляем HTTP запрос
            ResponseEntity<String> responseEntity = sendHttpRequest(requestXml);

            // Парсим ответ
            SmsDto.SmsResponse response = unmarshalResponse(responseEntity.getBody());

            // Обрабатываем результат
            if (response.isSuccess()) {
                log.info("SMS успешно отправлено: номер={}, messageId={}, status={}, phones={}, sms={}",
                         maskPhoneNumber(phoneNumber), messageId, response.getStatus(),
                         response.getPhones(), response.getSmscnt());
                return true;
            } else {
                log.error("Ошибка отправки SMS: статус={}, номер={}",
                          response.getStatus(), maskPhoneNumber(phoneNumber));
                return false;
            }

        } catch (Exception e) {
            log.error("Исключение при отправке SMS на номер {}: {}", maskPhoneNumber(phoneNumber), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Валидация входных данных
     */
    private void validateInput(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Номер телефона не может быть пустым");
        }

        if (!phoneNumber.matches("^\\+996\\d{9}$")) {
            throw new IllegalArgumentException("Неверный формат номера телефона. Ожидается: +996XXXXXXXXX");
        }

        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Текст сообщения не может быть пустым");
        }

        if (message.length() > 1000) {
            throw new IllegalArgumentException("Текст сообщения слишком длинный (максимум 1000 символов)");
        }
    }

    /**
     * Создание XML запроса
     */
    private SmsDto.SmsRequest buildSmsRequest(String phoneNumber, String message, String messageId) {
        return SmsDto.SmsRequest.builder()
                .login(login)
                .pwd(password)
                .id(messageId)
                .sender(sender)
                .text(message)
                .phones(SmsDto.SmsRequest.PhoneElement.builder()
                        .phone(phoneNumber)
                        .build())
                .test(testMode ? 1 : 0)
                .build();
    }

    /**
     * Маршалинг запроса в XML
     */
    private String marshalRequest(SmsDto.SmsRequest request) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        StringWriter writer = new StringWriter();
        marshaller.marshal(request, writer);
        return writer.toString();
    }

    /**
     * Анмаршалинг ответа из XML
     */
    private SmsDto.SmsResponse unmarshalResponse(String xml) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(xml);
        return (SmsDto.SmsResponse) unmarshaller.unmarshal(reader);
    }

    /**
     * Отправка HTTP запроса
     */
    private ResponseEntity<String> sendHttpRequest(String xmlBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.set("Accept", "application/xml");

        HttpEntity<String> entity = new HttpEntity<>(xmlBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        log.debug("HTTP ответ: status={}, body={}", response.getStatusCode(), response.getBody());
        return response;
    }

    /**
     * Маскировка номера телефона для логов
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 10) {
            return "***";
        }
        String prefix = phoneNumber.substring(0, phoneNumber.length() - 6);
        String suffix = phoneNumber.substring(phoneNumber.length() - 3);
        return prefix + "***" + suffix;
    }
}
