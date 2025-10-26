package kg.santechmarket.service.impl;

import kg.santechmarket.entity.User;
import kg.santechmarket.enums.UserRole;
import kg.santechmarket.enums.UserStatus;
import kg.santechmarket.repository.UserRepository;
import kg.santechmarket.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для работы с пользователями
 * Реализует UserDetailsService для интеграции с Spring Security
 * <p>
 * Принципы:
 * - Транзакционность для модифицирующих операций
 * - Логирование важных событий
 * - Валидация бизнес-правил
 * - Хеширование паролей
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Загрузка пользователя по логину для Spring Security
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Попытка загрузки пользователя: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: {}", username);
                    return new UsernameNotFoundException("Пользователь не найден: " + username);
                });

        if (!user.getIsActive()) {
            log.warn("Попытка входа заблокированного пользователя: {}", username);
            throw new UsernameNotFoundException("Пользователь заблокирован: " + username);
        }

        log.info("Пользователь успешно загружен: {} с ролью {}", username, user.getRole());
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        log.info("Создание нового пользователя: {}", user.getUsername());

        // Проверяем уникальность
        validateUserUniqueness(user);

        // Хешируем пароль
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // По умолчанию пользователь активен
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }

        // По умолчанию роль CLIENT
        if (user.getRole() == null) {
            user.setRole(UserRole.CLIENT);
        }

        User savedUser = userRepository.save(user);
        log.info("Пользователь создан: {} с ID {}", savedUser.getUsername(), savedUser.getId());

        return savedUser;
    }

    @Override
    @Transactional
    public User updateUser(Long id, kg.santechmarket.dto.UserDto.UpdateUserRequest userUpdate) {
        log.info("Обновление пользователя с ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        // Обновляем только те поля, которые переданы (не null)
        boolean needsUniquenessCheck = false;

        if (userUpdate.username() != null && !userUpdate.username().equals(existingUser.getUsername())) {
            existingUser.setUsername(userUpdate.username());
            needsUniquenessCheck = true;
        }

        if (userUpdate.fullName() != null) {
            existingUser.setFullName(userUpdate.fullName());
        }

        if (userUpdate.email() != null && !userUpdate.email().equals(existingUser.getEmail())) {
            existingUser.setEmail(userUpdate.email());
            needsUniquenessCheck = true;
        }

        if (userUpdate.phoneNumber() != null && !userUpdate.phoneNumber().equals(existingUser.getPhoneNumber())) {
            existingUser.setPhoneNumber(userUpdate.phoneNumber());
            needsUniquenessCheck = true;
        }

        if (userUpdate.role() != null) {
            existingUser.setRole(userUpdate.role());
        }

        if (userUpdate.isActive() != null) {
            existingUser.setIsActive(userUpdate.isActive());
        }

        // Проверяем уникальность только если критичные поля изменились
        if (needsUniquenessCheck) {
            if (userUpdate.username() != null && userRepository.existsByUsernameAndIdNot(userUpdate.username(), id)) {
                throw new IllegalArgumentException("Пользователь с таким логином уже существует");
            }
            if (userUpdate.email() != null && userRepository.existsByEmailAndIdNot(userUpdate.email(), id)) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }
            if (userUpdate.phoneNumber() != null && userRepository.existsByPhoneNumberAndIdNot(userUpdate.phoneNumber(), id)) {
                throw new IllegalArgumentException("Пользователь с таким номером телефона уже существует");
            }
        }

        // Обновляем пароль только если он передан
        if (userUpdate.password() != null && !userUpdate.password().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userUpdate.password()));
            log.info("Пароль обновлен для пользователя: {}", existingUser.getUsername());
        }

        User savedUser = userRepository.save(existingUser);
        log.info("Пользователь обновлен: {}", savedUser.getUsername());

        return savedUser;
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        log.info("Деактивация пользователя с ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("Пользователь деактивирован: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        log.info("Активация пользователя с ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        user.setIsActive(true);
        userRepository.save(user);

        log.info("Пользователь активирован: {}", user.getUsername());
    }

    @Override
    public List<User> findAllActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    @Override
    public List<User> findUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    public Page<User> searchUsers(String searchTerm, Boolean isActive, Pageable pageable) {
        return userRepository.findBySearchTerm(searchTerm, isActive, pageable);
    }

    @Override
    public long getUserCount() {
        return userRepository.count();
    }

    @Override
    public long getActiveUserCount() {
        return userRepository.countByIsActiveTrue();
    }

    @Override
    public long getUserCountByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    @Override
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).isPresent();
    }

    @Override
    @Transactional
    public User approveUser(Long id) {
        log.info("Одобрение пользователя с ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new IllegalStateException("Одобрить можно только пользователей со статусом PENDING");
        }

        user.setStatus(UserStatus.APPROVED);
        user.setIsActive(true);
        User savedUser = userRepository.save(user);

        log.info("Пользователь {} одобрен и активирован", user.getUsername());
        return savedUser;
    }

    @Override
    @Transactional
    public User rejectUser(Long id, String reason) {
        log.info("Отклонение пользователя с ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new IllegalStateException("Отклонить можно только пользователей со статусом PENDING");
        }

        user.setStatus(UserStatus.REJECTED);
        user.setIsActive(false);
        User savedUser = userRepository.save(user);

        log.info("Пользователь {} отклонён. Причина: {}", user.getUsername(), reason);
        return savedUser;
    }

    @Override
    public Page<User> getPendingUsers(Pageable pageable) {
        return userRepository.findByStatus(UserStatus.PENDING, pageable);
    }

    /**
     * Валидация уникальности пользователя
     */
    private void validateUserUniqueness(User user) {
        validateUserUniqueness(user, null);
    }

    /**
     * Валидация уникальности пользователя (исключая определенный ID)
     */
    private void validateUserUniqueness(User user, Long excludeId) {
        // Проверяем логин
        Optional<User> existingByUsername = userRepository.findByUsername(user.getUsername());
        if (existingByUsername.isPresent() && !existingByUsername.get().getId().equals(excludeId)) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует: " + user.getUsername());
        }

        // Проверяем email (если указан)
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            Optional<User> existingByEmail = userRepository.findByEmail(user.getEmail());
            if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(excludeId)) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует: " + user.getEmail());
            }
        }

        // Проверяем телефон (если указан)
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            Optional<User> existingByPhone = userRepository.findByPhoneNumber(user.getPhoneNumber());
            if (existingByPhone.isPresent() && !existingByPhone.get().getId().equals(excludeId)) {
                throw new IllegalArgumentException("Пользователь с таким номером телефона уже существует: " + user.getPhoneNumber());
            }
        }
    }
}