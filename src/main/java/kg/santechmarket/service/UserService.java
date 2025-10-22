package kg.santechmarket.service;

import kg.santechmarket.entity.User;
import kg.santechmarket.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс сервиса для работы с пользователями
 * <p>
 * Преимущества интерфейса:
 * - Четко определенный контракт API
 * - Возможность создания моков для тестирования
 * - Соблюдение принципа DIP (Dependency Inversion Principle)
 * - Возможность множественных реализаций в будущем
 */
public interface UserService extends UserDetailsService {

    /**
     * Найти пользователя по ID
     */
    Optional<User> findById(Long id);

    /**
     * Найти пользователя по логину
     */
    Optional<User> findByUsername(String username);

    /**
     * Найти пользователя по email
     */
    Optional<User> findByEmail(String email);

    /**
     * Создать нового пользователя
     */
    User createUser(User user);

    /**
     * Обновить пользователя (частичное обновление)
     * Обновляются только те поля, которые переданы (не null)
     */
    User updateUser(Long id, kg.santechmarket.dto.UserDto.UpdateUserRequest userUpdate);

    /**
     * Деактивировать пользователя (мягкое удаление)
     */
    void deactivateUser(Long id);

    /**
     * Активировать пользователя
     */
    void activateUser(Long id);

    /**
     * Получить всех активных пользователей
     */
    List<User> findAllActiveUsers();

    /**
     * Получить пользователей по роли
     */
    List<User> findUsersByRole(UserRole role);

    /**
     * Поиск пользователей с пагинацией
     */
    Page<User> searchUsers(String searchTerm, Boolean isActive, Pageable pageable);

    /**
     * Получить статистику пользователей
     */
    long getUserCount();

    long getActiveUserCount();

    long getUserCountByRole(UserRole role);

    /**
     * Проверить корректность пароля
     */
    boolean checkPassword(User user, String rawPassword);

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
     * Одобрить пользователя (PENDING -> APPROVED)
     */
    User approveUser(Long id);

    /**
     * Отклонить пользователя (PENDING -> REJECTED)
     */
    User rejectUser(Long id, String reason);

    /**
     * Получить пользователей на модерации (PENDING)
     */
    Page<User> getPendingUsers(Pageable pageable);
}