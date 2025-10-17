package kg.santechmarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.santechmarket.dto.CartResponseDTO;
import kg.santechmarket.entity.Cart;
import kg.santechmarket.entity.User;
import kg.santechmarket.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Корзина", description = "API для управления корзиной покупок: добавление/удаление товаров, изменение количества")
@SecurityRequirement(name = "JWT")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(
            summary = "Получить корзину пользователя",
            description = "Возвращает текущую корзину авторизованного пользователя со всеми товарами, количеством и общей стоимостью"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Корзина успешно получена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public ResponseEntity<CartResponseDTO> getUserCart(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Cart cart = cartService.getOrCreateUserCart(user.getId());
        CartResponseDTO response = cartService.toCartResponseDTO(cart);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    @Operation(
            summary = "Добавить товар в корзину",
            description = """
                    Добавляет указанное количество товара в корзину пользователя.
                    Если товар уже есть в корзине, количество будет увеличено на указанное значение.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар успешно добавлен в корзину"),
            @ApiResponse(responseCode = "400", description = "Неверные параметры или товар недоступен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "404", description = "Товар не найден")
    })
    public ResponseEntity<Boolean> addItemToCart(
            @Parameter(description = "ID товара для добавления", example = "5", required = true)
            @RequestParam Long productId,

            @Parameter(description = "Количество товара", example = "2", required = true)
            @RequestParam Integer quantity,

            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        cartService.addItemToCart(user.getId(), productId, quantity);
        return ResponseEntity.ok(true);
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Обновить количество товара", description = "Обновляет количество указанного товара в корзине")
    public ResponseEntity<Boolean> updateItemQuantity(
            @Parameter(description = "ID товара") @PathVariable Long productId,
            @Parameter(description = "Новое количество") @RequestParam Integer quantity,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        cartService.updateItemQuantity(user.getId(), productId, quantity);
        return ResponseEntity.ok(true);
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Удалить товар из корзины", description = "Удаляет товар из корзины пользователя")
    public ResponseEntity<Boolean> removeItemFromCart(
            @Parameter(description = "ID товара") @PathVariable Long productId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        cartService.removeItemFromCart(user.getId(), productId);
        return ResponseEntity.ok(true);
    }

    @DeleteMapping
    @Operation(summary = "Очистить корзину", description = "Удаляет все товары из корзины пользователя")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        cartService.clearUserCart(user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    @Operation(summary = "Получить количество товаров в корзине", description = "Возвращает общее количество товаров в корзине")
    public ResponseEntity<Integer> getCartItemCount(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        int count = cartService.getCartItemCount(user.getId());
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total")
    @Operation(summary = "Получить общую стоимость корзины", description = "Возвращает общую стоимость всех товаров в корзине")
    public ResponseEntity<BigDecimal> getCartTotal(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        BigDecimal total = cartService.getCartTotal(user.getId());
        return ResponseEntity.ok(total);
    }

    @GetMapping("/empty")
    @Operation(summary = "Проверить, пуста ли корзина", description = "Возвращает true, если корзина пуста")
    public ResponseEntity<Boolean> isCartEmpty(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        boolean isEmpty = cartService.isCartEmpty(user.getId());
        return ResponseEntity.ok(isEmpty);
    }

    @PatchMapping("/items/{productId}/increment")
    @Operation(
            summary = "Увеличить количество товара на 1",
            description = "Увеличивает количество указанного товара в корзине на 1. Удобно для кнопки '+' в интерфейсе."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Количество успешно увеличено"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "404", description = "Товар не найден в корзине")
    })
    public ResponseEntity<Boolean> incrementItemQuantity(
            @Parameter(description = "ID товара в корзине", example = "5", required = true)
            @PathVariable Long productId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        cartService.incrementItemQuantity(user.getId(), productId);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/items/{productId}/decrement")
    @Operation(
            summary = "Уменьшить количество товара на 1",
            description = "Уменьшает количество указанного товара в корзине на 1. Если количество станет 0, товар будет удален из корзины. Удобно для кнопки '-' в интерфейсе."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Количество успешно уменьшено"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "404", description = "Товар не найден в корзине")
    })
    public ResponseEntity<Boolean> decrementItemQuantity(
            @Parameter(description = "ID товара в корзине", example = "5", required = true)
            @PathVariable Long productId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        cartService.decrementItemQuantity(user.getId(), productId);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/unique-count")
    @Operation(summary = "Получить количество уникальных товаров", description = "Возвращает количество уникальных товаров в корзине")
    public ResponseEntity<Integer> getUniqueItemsCount(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        int count = cartService.getUniqueItemsCount(user.getId());
        return ResponseEntity.ok(count);
    }

    @PostMapping("/sync-prices")
    @Operation(summary = "Синхронизировать цены", description = "Синхронизирует цены в корзине с актуальными ценами товаров")
    public ResponseEntity<Boolean> syncCartPrices(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        cartService.syncCartPrices(user.getId());
        return ResponseEntity.ok(true);
    }

    @PostMapping("/validate")
    @Operation(summary = "Валидировать корзину", description = "Проверяет корзину перед оформлением заказа")
    public ResponseEntity<Void> validateCart(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        cartService.validateCartForCheckout(user.getId());
        return ResponseEntity.ok().build();
    }
}