package kg.santechmarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.santechmarket.entity.User;
import kg.santechmarket.enums.UserRole;
import kg.santechmarket.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API для управления пользователями")
@SecurityRequirement(name = "JWT")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Получить текущего пользователя", description = "Возвращает информацию о текущем авторизованном пользователе")
    @ApiResponse(responseCode = "200", description = "Успешно получена информация о пользователе")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID", description = "Возвращает информацию о пользователе по его идентификатору")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<User> getUserById(@Parameter(description = "ID пользователя") @PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Получить список пользователей", description = "Возвращает постраничный список всех активных пользователей")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Page<User>> getUsers(
            @Parameter(description = "Поисковый запрос") @RequestParam(required = false) String search,
            @Parameter(description = "Фильтр по активности") @RequestParam(required = false) Boolean isActive,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<User> users = userService.searchUsers(search, isActive, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Получить пользователей по роли", description = "Возвращает список пользователей с указаннойролью")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<User>> getUsersByRole(@Parameter(description = "Роль пользователя") @PathVariable UserRole role) {
        List<User> users = userService.findUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя в системе")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя", description = "Обновляет информацию о пользователе. Все поля опциональны - обновляются только переданные поля.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or #id == authentication.principal.id")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID пользователя") @PathVariable Long id,
            @Valid @RequestBody kg.santechmarket.dto.UserDto.UpdateUserRequest userUpdate) {
        User updatedUser = userService.updateUser(id, userUpdate);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Деактивировать пользователя", description = "Деактивирует пользователя (мягкое удаление)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@Parameter(description = "ID пользователя") @PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Активировать пользователя", description = "Активирует ранее деактивированного пользователя")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateUser(@Parameter(description = "ID пользователя") @PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/count")
    @Operation(summary = "Получить статистику пользователей", description = "Возвращает общее количество пользователей")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Long> getUserCount() {
        long count = userService.getUserCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/active-count")
    @Operation(summary = "Получить количество активных пользователей", description = "Возвращает количество активных пользователей")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Long> getActiveUserCount() {
        long count = userService.getActiveUserCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/count-by-role/{role}")
    @Operation(summary = "Получить количество пользователей по роли", description = "Возвращает количество пользователей с указанной ролью")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Long> getUserCountByRole(@Parameter(description = "Роль пользователя") @PathVariable UserRole role) {
        long count = userService.getUserCountByRole(role);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/pending")
    @Operation(summary = "Получить пользователей на модерации", description = "Возвращает список пользователей со статусом PENDING")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Page<User>> getPendingUsers(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<User> users = userService.getPendingUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Одобрить пользователя", description = "Одобряет регистрацию пользователя (PENDING -> APPROVED)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<User> approveUser(@Parameter(description = "ID пользователя") @PathVariable Long id) {
        User user = userService.approveUser(id);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Отклонить пользователя", description = "Отклоняет регистрацию пользователя (PENDING -> REJECTED)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<User> rejectUser(
            @Parameter(description = "ID пользователя") @PathVariable Long id,
            @Parameter(description = "Причина отклонения") @RequestParam(required = false) String reason) {
        User user = userService.rejectUser(id, reason);
        return ResponseEntity.ok(user);
    }

    /**
     * Инициировать смену email (шаг 1)
     */
    @PostMapping("/me/email/request-change")
    @Operation(
            summary = "Запросить смену email",
            description = "Отправляет 6-значный код подтверждения на новый email адрес. Код действителен 15 минут."
    )
    @ApiResponse(responseCode = "200", description = "Код успешно отправлен на новый email")
    @ApiResponse(responseCode = "400", description = "Некорректные данные или email уже используется")
    public ResponseEntity<?> requestEmailChange(
            Authentication authentication,
            @Valid @RequestBody kg.santechmarket.dto.UserDto.ChangeEmailRequest request) {
        User user = (User) authentication.getPrincipal();

        try {
            userService.initiateEmailChange(user, request.newEmail());
            return ResponseEntity.ok(new MessageResponse(
                    "Код подтверждения отправлен на новый email адрес: " + maskEmail(request.newEmail())
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Подтвердить смену email (шаг 2)
     */
    @PostMapping("/me/email/confirm-change")
    @Operation(
            summary = "Подтвердить смену email",
            description = "Подтверждает смену email адреса с помощью кода из письма"
    )
    @ApiResponse(responseCode = "200", description = "Email успешно изменен")
    @ApiResponse(responseCode = "400", description = "Неверный или истекший код")
    public ResponseEntity<?> confirmEmailChange(
            Authentication authentication,
            @Valid @RequestBody kg.santechmarket.dto.UserDto.ConfirmEmailChangeRequest request) {
        User user = (User) authentication.getPrincipal();

        try {
            User updatedUser = userService.confirmEmailChange(user, request.newEmail(), request.code());
            return ResponseEntity.ok(new MessageResponse("Email успешно изменен на: " + updatedUser.getEmail()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Инициировать смену телефона (шаг 1)
     */
    @PostMapping("/me/phone/request-change")
    @Operation(
            summary = "Запросить смену телефона",
            description = "Отправляет 6-значный код подтверждения на текущий email. Код действителен 15 минут."
    )
    @ApiResponse(responseCode = "200", description = "Код успешно отправлен на email")
    @ApiResponse(responseCode = "400", description = "Некорректные данные или телефон уже используется")
    public ResponseEntity<?> requestPhoneChange(
            Authentication authentication,
            @Valid @RequestBody kg.santechmarket.dto.UserDto.ChangePhoneRequest request) {
        User user = (User) authentication.getPrincipal();

        try {
            userService.initiatePhoneChange(user, request.newPhoneNumber());
            return ResponseEntity.ok(new MessageResponse(
                    "Код подтверждения отправлен на ваш email: " + maskEmail(user.getEmail())
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Подтвердить смену телефона (шаг 2)
     */
    @PostMapping("/me/phone/confirm-change")
    @Operation(
            summary = "Подтвердить смену телефона",
            description = "Подтверждает смену номера телефона с помощью кода из email"
    )
    @ApiResponse(responseCode = "200", description = "Телефон успешно изменен")
    @ApiResponse(responseCode = "400", description = "Неверный или истекший код")
    public ResponseEntity<?> confirmPhoneChange(
            Authentication authentication,
            @Valid @RequestBody kg.santechmarket.dto.UserDto.ConfirmPhoneChangeRequest request) {
        User user = (User) authentication.getPrincipal();

        try {
            User updatedUser = userService.confirmPhoneChange(user, request.newPhoneNumber(), request.code());
            return ResponseEntity.ok(new MessageResponse("Телефон успешно изменен на: " + updatedUser.getPhoneNumber()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Вспомогательный метод для маскировки email
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        if (localPart.length() <= 2) {
            return "**@" + domain;
        }
        return localPart.substring(0, 2) + "***@" + domain;
    }

    /**
     * Простой record для ответа с сообщением
     */
    private record MessageResponse(String message) {
    }
}