package kg.santechmarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.santechmarket.dto.OrderResponseDTO;
import kg.santechmarket.entity.Order;
import kg.santechmarket.entity.User;
import kg.santechmarket.enums.OrderStatus;
import kg.santechmarket.service.OrderService;
import kg.santechmarket.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "API для управления заказами")
@SecurityRequirement(name = "JWT")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Создать заказ", description = "Создает новый заказ из корзины пользователя")
    @ApiResponse(responseCode = "200", description = "Заказ успешно создан")
    public ResponseEntity<Boolean> createOrder(
            @Parameter(description = "Комментарий клиента") @RequestParam(required = false) String customerComment,
            @Parameter(description = "Контактная информация") @RequestParam(required = false) String contactInfo,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        orderService.createOrderFromCart(user.getId(), customerComment, contactInfo);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/my")
    @Operation(summary = "Получить мои заказы", description = "Возвращает заказы текущего пользователя")
    public ResponseEntity<Page<OrderResponseDTO>> getMyOrders(
            @PageableDefault(size = 10) Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Page<Order> orders = orderService.getUserOrders(user.getId(), pageable);
        Page<OrderResponseDTO> orderDTOs = orders.map(orderService::toOrderResponseDTO);
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/my/with-items")
    @Operation(summary = "Получить мои заказы с товарами", description = "Возвращает заказы текущего пользователя с деталями товаров")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrdersWithItems(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Order> orders = orderService.getUserOrdersWithItems(user.getId());
        List<OrderResponseDTO> orderDTOs = orders.stream()
                .map(orderService::toOrderResponseDTO)
                .toList();
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить заказ по ID", description = "Возвращает детали заказа по идентификатору")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @orderService.isOrderOwner(#id, authentication.principal.id)")
    public ResponseEntity<OrderResponseDTO> getOrderById(@Parameter(description = "ID заказа") @PathVariable Long id) {
        return orderService.findByIdWithItems(id)
                .map(orderService::toOrderResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Получить заказ по номеру", description = "Возвращает детали заказа по его номеру")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @orderService.isOrderOwnerByNumber(#orderNumber, authentication.principal.id)")
    public ResponseEntity<OrderResponseDTO> getOrderByNumber(@Parameter(description = "Номер заказа") @PathVariable String orderNumber) {
        return orderService.findByOrderNumber(orderNumber)
                .map(orderService::toOrderResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Получить заказы по статусу", description = "Возвращает заказы с указанным статусом")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersByStatus(
            @Parameter(description = "Статус заказа") @PathVariable OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Order> orders = orderService.getOrdersByStatus(status, pageable);
        Page<OrderResponseDTO> orderDTOs = orders.map(orderService::toOrderResponseDTO);
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск заказов", description = "Поиск заказов по различным критериям")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Page<OrderResponseDTO>> searchOrders(
            @Parameter(description = "Поисковый запрос") @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Order> orders = orderService.searchOrders(query, pageable);
        Page<OrderResponseDTO> orderDTOs = orders.map(orderService::toOrderResponseDTO);
        return ResponseEntity.ok(orderDTOs);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Обновить статус заказа", description = "Обновляет статус заказа")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Boolean> updateOrderStatus(
            @Parameter(description = "ID заказа") @PathVariable Long id,
            @Parameter(description = "Новый статус") @RequestParam OrderStatus status,
            @Parameter(description = "Комментарий менеджера") @RequestParam(required = false) String managerComment) {
        orderService.updateOrderStatus(id, status, managerComment);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Подтвердить заказ", description = "Подтверждает заказ (PENDING -> CONFIRMED)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Boolean> confirmOrder(
            @Parameter(description = "ID заказа") @PathVariable Long id,
            @Parameter(description = "Комментарий менеджера") @RequestParam(required = false) String managerComment) {
        orderService.confirmOrder(id, managerComment);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/{id}/process")
    @Operation(summary = "Начать обработку заказа", description = "Переводит заказ в обработку (CONFIRMED -> PROCESSING)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Boolean> startProcessing(
            @Parameter(description = "ID заказа") @PathVariable Long id,
            @Parameter(description = "Комментарий менеджера") @RequestParam(required = false) String managerComment) {
        orderService.startProcessingOrder(id, managerComment);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/{id}/ready")
    @Operation(summary = "Подготовить к выдаче", description = "Отмечает заказ как готовый к выдаче (PROCESSING -> READY)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Boolean> markReady(
            @Parameter(description = "ID заказа") @PathVariable Long id,
            @Parameter(description = "Комментарий менеджера") @RequestParam(required = false) String managerComment) {
        orderService.markOrderReady(id, managerComment);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Завершить заказ", description = "Завершает заказ (READY -> COMPLETED)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Boolean> completeOrder(
            @Parameter(description = "ID заказа") @PathVariable Long id,
            @Parameter(description = "Комментарий менеджера") @RequestParam(required = false) String managerComment) {
        orderService.completeOrder(id, managerComment);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Отменить заказ", description = "Отменяет заказ")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @orderService.isOrderOwner(#id, authentication.principal.id)")
    public ResponseEntity<Boolean> cancelOrder(
            @Parameter(description = "ID заказа") @PathVariable Long id,
            @Parameter(description = "Причина отмены") @RequestParam String cancelReason) {
        orderService.cancelOrder(id, cancelReason);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Получить заказы за период", description = "Возвращает заказы за указанный период времени")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByDateRange(
            @Parameter(description = "Начальная дата") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Конечная дата") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Order> orders = orderService.getOrdersByDateRange(startDate, endDate);
        List<OrderResponseDTO> orderDTOs = orders.stream()
                .map(orderService::toOrderResponseDTO)
                .toList();
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/overdue")
    @Operation(summary = "Получить просроченные заказы", description = "Возвращает заказы, которые находятся в статусе PENDING дольше указанного времени")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<OrderResponseDTO>> getOverdueOrders(
            @Parameter(description = "Пороговое количество часов") @RequestParam(defaultValue = "24") int hoursThreshold) {
        List<Order> orders = orderService.getOverdueOrders(hoursThreshold);
        List<OrderResponseDTO> orderDTOs = orders.stream()
                .map(orderService::toOrderResponseDTO)
                .toList();
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/stats")
    @Operation(summary = "Получить статистику заказов", description = "Возвращает общую статистику по заказам")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<OrderServiceImpl.OrderStatistics> getOrderStatistics() {
        OrderServiceImpl.OrderStatistics stats = orderService.getOrderStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/my/count")
    @Operation(summary = "Получить количество моих заказов", description = "Возвращает количество заказов текущего пользователя")
    public ResponseEntity<Long> getMyOrderCount(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        long count = orderService.getUserOrderCount(user.getId());
        return ResponseEntity.ok(count);
    }
}