package kg.santechmarket.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kg.santechmarket.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Реализация сервиса для отправки email-сообщений
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            log.info("Отправка простого письма на адрес: {}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Письмо успешно отправлено на адрес: {}", to);
        } catch (Exception e) {
            log.error("Ошибка при отправке письма на адрес: {}. Ошибка: {}", to, e.getMessage(), e);
            throw new RuntimeException("Не удалось отправить письмо: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendHtmlMessage(String to, String subject, String html) {
        try {
            log.info("Отправка HTML письма на адрес: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("HTML письмо успешно отправлено на адрес: {}", to);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке HTML письма на адрес: {}. Ошибка: {}", to, e.getMessage(), e);
            throw new RuntimeException("Не удалось отправить HTML письмо: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendMessageWithAttachment(String to, String subject, String text, String attachmentPath) {
        try {
            log.info("Отправка письма с вложением на адрес: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment(file.getFilename(), file);

            mailSender.send(message);
            log.info("Письмо с вложением успешно отправлено на адрес: {}", to);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке письма с вложением на адрес: {}. Ошибка: {}", to, e.getMessage(), e);
            throw new RuntimeException("Не удалось отправить письмо с вложением: " + e.getMessage(), e);
        }
    }
}
