package kg.santechmarket.service;

/**
 * Сервис для отправки email-сообщений
 */
public interface EmailService {

    /**
     * Отправка простого текстового письма
     *
     * @param to      адрес получателя
     * @param subject тема письма
     * @param text    текст письма
     */
    void sendSimpleMessage(String to, String subject, String text);

    /**
     * Отправка HTML письма
     *
     * @param to      адрес получателя
     * @param subject тема письма
     * @param html    HTML содержимое письма
     */
    void sendHtmlMessage(String to, String subject, String html);

    /**
     * Отправка письма с вложением
     *
     * @param to           адрес получателя
     * @param subject      тема письма
     * @param text         текст письма
     * @param attachmentPath путь к файлу вложения
     */
    void sendMessageWithAttachment(String to, String subject, String text, String attachmentPath);

    /**
     * Отправка кода верификации для смены email
     *
     * @param to       новый email адрес
     * @param code     6-значный код
     * @param userName имя пользователя
     */
    void sendEmailChangeVerificationCode(String to, String code, String userName);

    /**
     * Отправка кода верификации для смены телефона
     *
     * @param to       email адрес
     * @param code     6-значный код
     * @param userName имя пользователя
     * @param newPhone новый номер телефона
     */
    void sendPhoneChangeVerificationCode(String to, String code, String userName, String newPhone);

    /**
     * Отправка кода верификации для сброса пароля
     *
     * @param to       email адрес
     * @param code     6-значный код
     * @param userName имя пользователя
     */
    void sendPasswordResetVerificationCode(String to, String code, String userName);
}
