package kg.santechmarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.santechmarket.config.JwtService;
import kg.santechmarket.dto.AuthDto;
import kg.santechmarket.entity.User;
import kg.santechmarket.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для авторизации и работы с профилем пользователя
 * <p>
 * Endpoints:
 * - POST /auth/login - вход в систему
 * - GET /auth/profile - получение профиля
 * - PUT /auth/profile - обновление профиля
 * - POST /auth/change-password - смена пароля
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Авторизация", description = "API для авторизации и управления профилем")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    /**
     * Вход в систему
     */
    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя и получение JWT токена")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto.LoginRequest loginRequest) {
        log.info("Попытка входа пользователя: {}", loginRequest.username());

        try {
            // Аутентификация пользователя
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            // Получение пользователя
            User user = (User) authentication.getPrincipal();

            // Генерация JWT токена
            String token = jwtService.generateToken(user);

            // Создание ответа
            AuthDto.LoginResponse response = new AuthDto.LoginResponse(
                    token,
                    "Bearer",
                    jwtService.getExpirationMs() / 1000, // в секундах
                    mapToUserInfo(user)
            );

            log.info("Успешный вход пользователя: {} с ролью {}", user.getUsername(), user.getRole());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            log.warn("Неудачная попытка входа для пользователя: {}", loginRequest.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Неверный логин или пароль"));
        } catch (Exception e) {
            log.error("Ошибка при входе пользователя {}: {}", loginRequest.username(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера"));
        }
    }

    /**
     * Получение профиля текущего пользователя
     */
    @GetMapping("/profile")
    @Operation(summary = "Получить профиль", description = "Получение информации о текущем пользователе")
    public ResponseEntity<AuthDto.UserInfo> getProfile(@AuthenticationPrincipal User currentUser) {
        log.debug("Запрос профиля пользователя: {}", currentUser.getUsername());

        AuthDto.UserInfo userInfo = mapToUserInfo(currentUser);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Обновление профиля пользователя
     */
    @PutMapping("/profile")
    @Operation(summary = "Обновить профиль", description = "Обновление информации профиля пользователя")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal User currentUser,
                                           @Valid @RequestBody AuthDto.UpdateProfileRequest request) {
        log.info("Обновление профиля пользователя: {}", currentUser.getUsername());

        try {
            // Создаем объект с обновленными данными
            User updatedUser = new User();
            updatedUser.setUsername(currentUser.getUsername()); // логин не меняется
            updatedUser.setFullName(request.fullName());
            updatedUser.setEmail(request.email());
            updatedUser.setPhoneNumber(request.phoneNumber());
            updatedUser.setRole(currentUser.getRole());
            updatedUser.setIsActive(currentUser.getIsActive());

            // Обновляем пользователя
            User savedUser = userService.updateUser(currentUser.getId(), updatedUser);

            AuthDto.UserInfo userInfo = mapToUserInfo(savedUser);

            log.info("Профиль пользователя {} успешно обновлен", currentUser.getUsername());
            return ResponseEntity.ok(userInfo);

        } catch (IllegalArgumentException e) {
            log.warn("Ошибка валидации при обновлении профиля {}: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Ошибка при обновлении профиля {}: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Не удалось обновить профиль"));
        }
    }

    /**
     * Смена пароля
     */
    @PostMapping("/change-password")
    @Operation(summary = "Сменить пароль", description = "Изменение пароля текущего пользователя")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal User currentUser,
                                            @Valid @RequestBody AuthDto.ChangePasswordRequest request) {
        log.info("Запрос смены пароля для пользователя: {}", currentUser.getUsername());

        try {
            // Проверяем текущий пароль
            if (!userService.checkPassword(currentUser, request.currentPassword())) {
                log.warn("Неверный текущий пароль при смене для пользователя: {}", currentUser.getUsername());
                return ResponseEntity.badRequest().body(new ErrorResponse("Неверный текущий пароль"));
            }

            // Создаем объект с новым паролем
            User updatedUser = new User();
            updatedUser.setUsername(currentUser.getUsername());
            updatedUser.setPassword(request.newPassword()); // будет хеширован в сервисе
            updatedUser.setFullName(currentUser.getFullName());
            updatedUser.setEmail(currentUser.getEmail());
            updatedUser.setPhoneNumber(currentUser.getPhoneNumber());
            updatedUser.setRole(currentUser.getRole());
            updatedUser.setIsActive(currentUser.getIsActive());

            // Обновляем пользователя
            userService.updateUser(currentUser.getId(), updatedUser);

            log.info("Пароль пользователя {} успешно изменен", currentUser.getUsername());
            return ResponseEntity.ok(new SuccessResponse("Пароль успешно изменен"));

        } catch (Exception e) {
            log.error("Ошибка при смене пароля для {}: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Не удалось изменить пароль"));
        }
    }

    /**
     * Маппинг User в UserInfo DTO
     */
    private AuthDto.UserInfo mapToUserInfo(User user) {
        return new AuthDto.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.getIsActive()
        );
    }

    /**
     * DTO для ошибок
     */
    public record ErrorResponse(String message) {
    }

    /**
     * DTO для успешных операций
     */
    public record SuccessResponse(String message) {
    }
}