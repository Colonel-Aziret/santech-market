package kg.santechmarket.repository;

import kg.santechmarket.entity.User;
import kg.santechmarket.entity.VerificationCode;
import kg.santechmarket.enums.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Репозиторий для работы с кодами верификации
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * Найти код по его значению
     */
    Optional<VerificationCode> findByCode(String code);

    /**
     * Найти активный код по пользователю и типу
     */
    @Query("SELECT v FROM VerificationCode v WHERE v.user = :user AND v.type = :type AND v.isUsed = false AND v.expiryDate > :now ORDER BY v.createdAt DESC")
    Optional<VerificationCode> findActiveCodeByUserAndType(User user, VerificationType type, Instant now);

    /**
     * Найти активный код по коду, пользователю и типу
     */
    @Query("SELECT v FROM VerificationCode v WHERE v.code = :code AND v.user = :user AND v.type = :type AND v.isUsed = false AND v.expiryDate > :now")
    Optional<VerificationCode> findByCodeAndUserAndType(String code, User user, VerificationType type, Instant now);

    /**
     * Удалить все коды пользователя определенного типа
     */
    void deleteByUserAndType(User user, VerificationType type);

    /**
     * Удалить все коды пользователя
     */
    void deleteByUser(User user);

    /**
     * Удалить истекшие и использованные коды
     */
    @Modifying
    @Query("DELETE FROM VerificationCode v WHERE v.expiryDate < :now OR v.isUsed = true")
    void deleteExpiredOrUsedCodes(Instant now);

    /**
     * Проверить существование активного кода для пользователя и типа
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM VerificationCode v WHERE v.user = :user AND v.type = :type AND v.isUsed = false AND v.expiryDate > :now")
    boolean existsActiveCodeByUserAndType(User user, VerificationType type, Instant now);
}
