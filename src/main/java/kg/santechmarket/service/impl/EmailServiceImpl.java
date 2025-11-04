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

    @Override
    public void sendEmailChangeVerificationCode(String to, String code, String userName) {
        String subject = "Подтверждение смены email адреса - SanTech Market";
        String html = buildEmailChangeTemplate(code, userName);
        sendHtmlMessage(to, subject, html);
    }

    @Override
    public void sendPhoneChangeVerificationCode(String to, String code, String userName, String newPhone) {
        String subject = "Подтверждение смены номера телефона - SanTech Market";
        String html = buildPhoneChangeTemplate(code, userName, newPhone);
        sendHtmlMessage(to, subject, html);
    }

    @Override
    public void sendPasswordResetVerificationCode(String to, String code, String userName) {
        String subject = "Код для сброса пароля - SanTech Market";
        String html = buildPasswordResetTemplate(code, userName);
        sendHtmlMessage(to, subject, html);
    }

    /**
     * Шаблон письма для смены email
     */
    private String buildEmailChangeTemplate(String code, String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; }
                        .container { max-width: 600px; margin: 20px auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 0 20px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 28px; }
                        .content { padding: 40px 30px; }
                        .code-box { background-color: #f8f9fa; border: 2px dashed #667eea; border-radius: 10px; padding: 20px; text-align: center; margin: 30px 0; }
                        .code { font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px; font-family: 'Courier New', monospace; }
                        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 5px; }
                        .footer { background-color: #333; color: white; padding: 20px; text-align: center; font-size: 14px; }
                        .button { background-color: #667eea; color: white; padding: 12px 30px; text-decoration: none; display: inline-block; border-radius: 5px; margin: 20px 0; }
                        ul { padding-left: 20px; }
                        li { margin: 10px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 SanTech Market</h1>
                            <p style="margin: 10px 0 0 0; font-size: 18px;">Подтверждение смены email</p>
                        </div>
                        <div class="content">
                            <p>Здравствуйте, <strong>%s</strong>!</p>
                            <p>Вы запросили смену email адреса в системе SanTech Market.</p>

                            <div class="code-box">
                                <p style="margin: 0 0 10px 0; font-size: 14px; color: #666;">Ваш код подтверждения:</p>
                                <div class="code">%s</div>
                                <p style="margin: 15px 0 0 0; font-size: 12px; color: #999;">Код действителен в течение 15 минут</p>
                            </div>

                            <div class="warning">
                                <strong>⚠️ Важно!</strong>
                                <ul style="margin: 10px 0 0 0;">
                                    <li>Никому не сообщайте этот код</li>
                                    <li>Если вы не запрашивали смену email, проигнорируйте это письмо</li>
                                    <li>При необходимости свяжитесь с поддержкой</li>
                                </ul>
                            </div>

                            <p style="color: #666; margin-top: 30px;">
                                Если у вас возникли вопросы, обратитесь в службу поддержки.
                            </p>
                        </div>
                        <div class="footer">
                            <p style="margin: 0;">&copy; 2024 SanTech Market. Все права защищены.</p>
                            <p style="margin: 10px 0 0 0; font-size: 12px;">Это автоматическое письмо, не отвечайте на него.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, code);
    }

    /**
     * Шаблон письма для смены телефона
     */
    private String buildPhoneChangeTemplate(String code, String userName, String newPhone) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; }
                        .container { max-width: 600px; margin: 20px auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 0 20px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%); color: white; padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 28px; }
                        .content { padding: 40px 30px; }
                        .code-box { background-color: #f8f9fa; border: 2px dashed #4CAF50; border-radius: 10px; padding: 20px; text-align: center; margin: 30px 0; }
                        .code { font-size: 36px; font-weight: bold; color: #4CAF50; letter-spacing: 8px; font-family: 'Courier New', monospace; }
                        .phone-info { background-color: #e8f5e9; padding: 15px; border-radius: 5px; margin: 20px 0; }
                        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 5px; }
                        .footer { background-color: #333; color: white; padding: 20px; text-align: center; font-size: 14px; }
                        ul { padding-left: 20px; }
                        li { margin: 10px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>📱 SanTech Market</h1>
                            <p style="margin: 10px 0 0 0; font-size: 18px;">Подтверждение смены телефона</p>
                        </div>
                        <div class="content">
                            <p>Здравствуйте, <strong>%s</strong>!</p>
                            <p>Вы запросили смену номера телефона в системе SanTech Market.</p>

                            <div class="phone-info">
                                <strong>Новый номер телефона:</strong> <code>%s</code>
                            </div>

                            <div class="code-box">
                                <p style="margin: 0 0 10px 0; font-size: 14px; color: #666;">Ваш код подтверждения:</p>
                                <div class="code">%s</div>
                                <p style="margin: 15px 0 0 0; font-size: 12px; color: #999;">Код действителен в течение 15 минут</p>
                            </div>

                            <div class="warning">
                                <strong>⚠️ Важно!</strong>
                                <ul style="margin: 10px 0 0 0;">
                                    <li>Никому не сообщайте этот код</li>
                                    <li>Если вы не запрашивали смену номера, проигнорируйте это письмо</li>
                                    <li>При необходимости свяжитесь с поддержкой</li>
                                </ul>
                            </div>

                            <p style="color: #666; margin-top: 30px;">
                                Если у вас возникли вопросы, обратитесь в службу поддержки.
                            </p>
                        </div>
                        <div class="footer">
                            <p style="margin: 0;">&copy; 2024 SanTech Market. Все права защищены.</p>
                            <p style="margin: 10px 0 0 0; font-size: 12px;">Это автоматическое письмо, не отвечайте на него.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, newPhone, code);
    }

    /**
     * Шаблон письма для сброса пароля
     */
    private String buildPasswordResetTemplate(String code, String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; }
                        .container { max-width: 600px; margin: 20px auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 0 20px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #f44336 0%%, #e91e63 100%%); color: white; padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 28px; }
                        .content { padding: 40px 30px; }
                        .code-box { background-color: #f8f9fa; border: 2px dashed #f44336; border-radius: 10px; padding: 20px; text-align: center; margin: 30px 0; }
                        .code { font-size: 36px; font-weight: bold; color: #f44336; letter-spacing: 8px; font-family: 'Courier New', monospace; }
                        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 5px; }
                        .footer { background-color: #333; color: white; padding: 20px; text-align: center; font-size: 14px; }
                        ul { padding-left: 20px; }
                        li { margin: 10px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔑 SanTech Market</h1>
                            <p style="margin: 10px 0 0 0; font-size: 18px;">Сброс пароля</p>
                        </div>
                        <div class="content">
                            <p>Здравствуйте, <strong>%s</strong>!</p>
                            <p>Вы запросили сброс пароля в системе SanTech Market.</p>

                            <div class="code-box">
                                <p style="margin: 0 0 10px 0; font-size: 14px; color: #666;">Ваш код для сброса пароля:</p>
                                <div class="code">%s</div>
                                <p style="margin: 15px 0 0 0; font-size: 12px; color: #999;">Код действителен в течение 15 минут</p>
                            </div>

                            <div class="warning">
                                <strong>⚠️ Безопасность!</strong>
                                <ul style="margin: 10px 0 0 0;">
                                    <li>Никому не сообщайте этот код</li>
                                    <li>Если вы не запрашивали сброс пароля, немедленно свяжитесь с поддержкой</li>
                                    <li>После смены пароля используйте надежную комбинацию</li>
                                </ul>
                            </div>

                            <p style="color: #666; margin-top: 30px;">
                                Если у вас возникли вопросы, обратитесь в службу поддержки.
                            </p>
                        </div>
                        <div class="footer">
                            <p style="margin: 0;">&copy; 2024 SanTech Market. Все права защищены.</p>
                            <p style="margin: 10px 0 0 0; font-size: 12px;">Это автоматическое письмо, не отвечайте на него.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, code);
    }
}
