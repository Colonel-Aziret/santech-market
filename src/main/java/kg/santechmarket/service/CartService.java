package kg.santechmarket.service;

import kg.santechmarket.dto.CartResponseDTO;
import kg.santechmarket.entity.Cart;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Интерфейс сервиса для работы с корзиной пользователя
 */
public interface CartService {

    /**
     * Получить корзину пользователя (создать, если не существует)
     */
    Cart getOrCreateUserCart(Long userId);

    /**
     * Получить корзину пользователя с товарами
     */
    Optional<Cart> getUserCart(Long userId);

    /**
     * Добавить товар в корзину
     */
    Cart addItemToCart(Long userId, Long productId, Integer quantity);

    /**
     * Обновить количество товара в корзине
     */
    Cart updateItemQuantity(Long userId, Long productId, Integer quantity);

    /**
     * Удалить товар из корзины
     */
    Cart removeItemFromCart(Long userId, Long productId);

    /**
     * Очистить корзину пользователя
     */
    void clearUserCart(Long userId);

    /**
     * Получить количество товаров в корзине пользователя
     */
    int getCartItemCount(Long userId);

    /**
     * Получить общую стоимость корзины пользователя
     */
    BigDecimal getCartTotal(Long userId);

    /**
     * Проверить, пуста ли корзина
     */
    boolean isCartEmpty(Long userId);

    /**
     * Синхронизировать цены в корзине с актуальными ценами товаров
     */
    Cart syncCartPrices(Long userId);

    /**
     * Увеличить количество товара на 1
     */
    Cart incrementItemQuantity(Long userId, Long productId);

    /**
     * Уменьшить количество товара на 1
     */
    Cart decrementItemQuantity(Long userId, Long productId);

    /**
     * Получить количество уникальных товаров в корзине
     */
    int getUniqueItemsCount(Long userId);

    /**
     * Валидация корзины перед оформлением заказа
     */
    void validateCartForCheckout(Long userId);

    /**
     * Конвертировать Cart в CartResponseDTO
     */
    CartResponseDTO toCartResponseDTO(Cart cart);
}