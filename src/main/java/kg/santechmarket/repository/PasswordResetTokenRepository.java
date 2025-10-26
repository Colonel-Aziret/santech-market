package kg.santechmarket.repository;

import kg.santechmarket.entity.PasswordResetToken;
import kg.santechmarket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Репозиторий для работы с токенами сброса пароля
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Найти токен по его значению
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Найти активный (не использованный и не истекший) токен по пользователю
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user = :user AND t.isUsed = false AND t.expiryDate > :now ORDER BY t.createdAt DESC")
    Optional<PasswordResetToken> findActiveTokenByUser(User user, Instant now);

    /**
     * Удалить все токены пользователя
     */
    void deleteByUser(User user);

    /**
     * Удалить истекшие токены
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :now OR t.isUsed = true")
    void deleteExpiredOrUsedTokens(Instant now);
}
