package kg.santechmarket.dto;

import jakarta.xml.bind.annotation.*;
import lombok.*;

/**
 * DTOs для работы с Nikita Mobile SMS API
 */
public class SmsDto {

    /**
     * XML запрос для отправки SMS через Nikita Mobile API
     * <p>
     * Пример:
     * <pre>
     * {@code
     * <message>
     *     <login>partner_login</login>
     *     <pwd>partner_password</pwd>
     *     <id>unique_message_id</id>
     *     <sender>SanTech</sender>
     *     <text>Ваш код: 123456</text>
     *     <phones phone="+996555123456"/>
     *     <test>0</test>
     * </message>
     * }
     * </pre>
     */
    @XmlRootElement(name = "message")
    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmsRequest {

        /**
         * Логин партнера
         */
        @XmlElement(required = true)
        private String login;

        /**
         * Пароль партнера
         */
        @XmlElement(name = "pwd", required = true)
        private String pwd;

        /**
         * Уникальный идентификатор сообщения
         */
        @XmlElement(required = true)
        private String id;

        /**
         * Имя отправителя (до 11 латинских символов или 14 цифр)
         */
        @XmlElement(required = true)
        private String sender;

        /**
         * Текст сообщения
         */
        @XmlElement(required = true)
        private String text;

        /**
         * Информация о получателе
         */
        @XmlElement(name = "phones", required = true)
        private PhoneElement phones;

        /**
         * Тестовый режим (0 - отправить реально, 1 - тестовый режим)
         */
        @XmlElement
        private Integer test;

        /**
         * Элемент для указания номера телефона
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PhoneElement {
            @XmlAttribute(name = "phone", required = true)
            private String phone;
        }
    }

    /**
     * XML ответ от Nikita Mobile API
     * <p>
     * Пример успешного ответа:
     * <pre>
     * {@code
     * <response xmlns="http://Giper.mobi/schema/Message">
     *     <id>3</id>
     *     <status>0</status>
     *     <phones>1</phones>
     *     <smscnt>1</smscnt>
     * </response>
     * }
     * </pre>
     * <p>
     * Коды статусов:
     * - 0: Успешная отправка (в тестовом режиме)
     * - 2: Тестовый режим
     * - 1: Неверный логин/пароль
     * - 3: Ошибка в параметрах
     * - 4: Заблокированный аккаунт
     */
    @XmlRootElement(name = "response", namespace = "http://Giper.mobi/schema/Message")
    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmsResponse {

        /**
         * Идентификатор сообщения
         */
        @XmlElement(namespace = "http://Giper.mobi/schema/Message")
        private String id;

        /**
         * Код статуса отправки
         * 0 или 2 - успешно (2 для тестового режима)
         */
        @XmlElement(required = true, namespace = "http://Giper.mobi/schema/Message")
        private Integer status;

        /**
         * Количество телефонных номеров
         */
        @XmlElement(name = "phones", namespace = "http://Giper.mobi/schema/Message")
        private Integer phones;

        /**
         * Количество SMS сообщений
         */
        @XmlElement(name = "smscnt", namespace = "http://Giper.mobi/schema/Message")
        private Integer smscnt;

        /**
         * Проверка на успешную отправку
         */
        public boolean isSuccess() {
            // Статус 0 - успешная отправка
            // Статус 2 - тестовый режим (также считаем успехом)
            return status != null && (status == 0 || status == 2);
        }
    }
}
