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
    @Operation(summary = "Обновить пользователя", description = "Обновляет информацию о пользователе")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or #id == authentication.principal.id")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID пользователя") @PathVariable Long id,
            @Valid @RequestBody User userUpdate) {
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
}