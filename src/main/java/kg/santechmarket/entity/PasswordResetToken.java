package kg.santechmarket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Токен для сброса пароля
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 6)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    @lombok.Builder.Default
    private Boolean isUsed = false;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
