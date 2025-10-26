package kg.santechmarket.service;

/**
 * Сервис для отправки SMS сообщений
 * <p>
 * Основная функциональность:
 * - Отправка SMS через внешний API провайдера
 * - Обработка ошибок отправки
 * - Поддержка тестового режима
 */
public interface SmsService {

    /**
     * Отправляет SMS сообщение на указанный номер телефона
     *
     * @param phoneNumber номер телефона в формате +996XXXXXXXXX
     * @param message     текст сообщения
     * @return true если сообщение отправлено успешно, false в случае ошибки
     * @throws IllegalArgumentException если номер телефона или сообщение невалидны
     */
    boolean sendSms(String phoneNumber, String message);

    /**
     * Отправляет SMS сообщение с уникальным идентификатором
     * Используется для отслеживания отправки
     *
     * @param phoneNumber номер телефона в формате +996XXXXXXXXX
     * @param message     текст сообщения
     * @param messageId   уникальный идентификатор сообщения
     * @return true если сообщение отправлено успешно, false в случае ошибки
     * @throws IllegalArgumentException если параметры невалидны
     */
    boolean sendSms(String phoneNumber, String message, String messageId);

    /**
     * Проверяет доступность SMS сервиса
     *
     * @return true если сервис включен и настроен, false в противном случае
     */
    boolean isEnabled();
}
