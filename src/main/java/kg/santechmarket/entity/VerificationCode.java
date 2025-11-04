package kg.santechmarket.entity;

import jakarta.persistence.*;
import kg.santechmarket.enums.VerificationType;
import lombok.*;

import java.time.Instant;

/**
 * Код верификации для различных операций (смена email, телефона, сброс пароля)
 */
@Entity
@Table(name = "verification_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 6-значный код верификации
     */
    @Column(nullable = false, length = 6)
    private String code;

    /**
     * Пользователь, запросивший код
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Тип верификации
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationType type;

    /**
     * Email на который отправлен код (может отличаться от текущего при смене email)
     */
    @Column(nullable = false)
    private String targetEmail;

    /**
     * Новое значение (новый email или новый телефон), которое будет установлено после верификации
     */
    @Column(length = 100)
    private String newValue;

    /**
     * Время истечения кода
     */
    @Column(nullable = false)
    private Instant expiryDate;

    /**
     * Время создания кода
     */
    @Column(nullable = false)
    private Instant createdAt;

    /**
     * Использован ли код
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    /**
     * Количество попыток использования
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
