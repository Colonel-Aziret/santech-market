package kg.santechmarket.repository;

import kg.santechmarket.entity.User;
import kg.santechmarket.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями
 * Использует Spring Data JPA для автоматической генерации запросов
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Найти пользователя по логину (для Spring Security)
     */
    Optional<User> findByUsername(String username);

    /**
     * Найти пользователя по email
     */
    Optional<User> findByEmail(String email);

    /**
     * Найти пользователя по номеру телефона
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Проверить существование пользователя по логину
     */
    boolean existsByUsername(String username);

    /**
     * Проверить существование пользователя по email
     */
    boolean existsByEmail(String email);

    /**
     * Проверить существование пользователя по номеру телефона
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Найти всех активных пользователей
     */
    List<User> findByIsActiveTrue();

    /**
     * Найти пользователей по роли
     */
    List<User> findByRole(UserRole role);

    /**
     * Найти активных пользователей по роли
     */
    List<User> findByRoleAndIsActiveTrue(UserRole role);

    /**
     * Поиск пользователей по части имени или логина
     */
    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive)")
    Page<User> findBySearchTerm(@Param("searchTerm") String searchTerm,
                                @Param("isActive") Boolean isActive,
                                Pageable pageable);

    /**
     * Получить количество пользователей по роли
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);

    /**
     * Получить количество активных пользователей
     */
    long countByIsActiveTrue();
}