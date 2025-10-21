package kg.santechmarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.santechmarket.entity.Notification;
import kg.santechmarket.entity.User;
import kg.santechmarket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "API для управления уведомлениями")
@SecurityRequirement(name = "JWT")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(
            summary = "Получить мои уведомления",
            description = "Возвращает список уведомлений текущего пользователя. " +
                    "Параметры пагинации передаются как query параметры: ?page=0&size=10&sort=createdAt,desc"
    )
    @ApiResponse(responseCode = "200", description = "Успешно получен список уведомлений")
    public ResponseEntity<Page<Notification>> getMyNotifications(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Page<Notification> notifications = notificationService.getUserNotifications(user.getId(), pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    @Operation(summary = "Получить непрочитанные уведомления", description = "Возвращает список непрочитанных уведомлений")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Notification> notifications = notificationService.getUnreadNotifications(user.getId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Получить количество непрочитанных", description = "Возвращает количество непрочитанных уведомлений")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        long count = notificationService.getUnreadNotificationCount(user.getId());
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Отметить как прочитанное", description = "Отмечает уведомление как прочитанное")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "ID уведомления") @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/mark-all-read")
    @Operation(summary = "Отметить все как прочитанные", description = "Отмечает все уведомления пользователя как прочитанные")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить уведомление", description = "Удаляет уведомление пользователя")
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "ID уведомления") @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        notificationService.deleteNotification(id, user.getId());
        return ResponseEntity.ok().build();
    }
}
