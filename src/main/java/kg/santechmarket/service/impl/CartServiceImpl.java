package kg.santechmarket.service.impl;

import kg.santechmarket.dto.CartItemDTO;
import kg.santechmarket.dto.CartResponseDTO;
import kg.santechmarket.entity.Cart;
import kg.santechmarket.entity.CartItem;
import kg.santechmarket.entity.Product;
import kg.santechmarket.entity.User;
import kg.santechmarket.repository.CartItemRepository;
import kg.santechmarket.repository.CartRepository;
import kg.santechmarket.repository.ProductRepository;
import kg.santechmarket.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для работы с корзиной пользователя
 * <p>
 * Основные функции:
 * - Создание корзины при первом добавлении товара
 * - Добавление/удаление/обновление товаров в корзине
 * - Автоматический пересчет общей суммы и количества
 * - Очистка корзины после оформления заказа
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * Получить корзину пользователя (создать, если не существует)
     */
    @Transactional
    public Cart getOrCreateUserCart(Long userId) {
        log.debug("Получение корзины для пользователя: {}", userId);

        Optional<Cart> existingCart = cartRepository.findByUserIdWithItems(userId);
        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Создаем новую корзину
        Cart newCart = new Cart();
        User user = new User();
        user.setId(userId);
        newCart.setUser(user);

        Cart savedCart = cartRepository.save(newCart);
        log.info("Создана новая корзина для пользователя: {}", userId);

        return savedCart;
    }

    /**
     * Получить корзину пользователя с товарами
     */
    public Optional<Cart> getUserCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId);
    }

    /**
     * Добавить товар в корзину
     */
    @Transactional
    public Cart addItemToCart(Long userId, Long productId, Integer quantity) {
        log.info("Добавление товара {} в корзину пользователя {} в количестве {}",
                productId, userId, quantity);

        // Валидация
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }

        // Проверяем существование товара
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден: " + productId));

        if (!product.getIsActive()) {
            throw new IllegalArgumentException("Товар недоступен для заказа: " + product.getName());
        }

        // Получаем или создаем корзину
        Cart cart = getOrCreateUserCart(userId);

        // Используем метод Entity для добавления товара
        cart.addItem(product, quantity);

        Cart savedCart = cartRepository.save(cart);
        log.info("Товар {} добавлен в корзину пользователя {}", product.getName(), userId);

        return savedCart;
    }

    /**
     * Обновить количество товара в корзине
     */
    @Transactional
    public Cart updateItemQuantity(Long userId, Long productId, Integer quantity) {
        log.info("Обновление количества товара {} в корзине пользователя {} на {}",
                productId, userId, quantity);

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new IllegalArgumentException("Корзина не найдена"));

        // Используем метод Entity для обновления
        cart.updateItemQuantity(productId, quantity);

        Cart savedCart = cartRepository.save(cart);
        log.info("Количество товара обновлено в корзине пользователя {}", userId);

        return savedCart;
    }

    /**
     * Удалить товар из корзины
     */
    @Transactional
    public Cart removeItemFromCart(Long userId, Long productId) {
        log.info("Удаление товара {} из корзины пользователя {}", productId, userId);

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new IllegalArgumentException("Корзина не найдена"));

        // Используем метод Entity для удаления
        cart.removeItem(productId);

        Cart savedCart = cartRepository.save(cart);
        log.info("Товар удален из корзины пользователя {}", userId);

        return savedCart;
    }

    /**
     * Очистить корзину пользователя
     */
    @Transactional
    public void clearUserCart(Long userId) {
        log.info("Очистка корзины пользователя: {}", userId);

        Optional<Cart> cartOpt = cartRepository.findByUserIdWithItems(userId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cart.clear();
            cartRepository.save(cart);
            log.info("Корзина пользователя {} очищена", userId);
        }
    }

    /**
     * Получить количество товаров в корзине пользователя
     */
    public int getCartItemCount(Long userId) {
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        return cart.map(Cart::getTotalItems).orElse(0);
    }

    /**
     * Получить общую стоимость корзины пользователя
     */
    public BigDecimal getCartTotal(Long userId) {
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        return cart.map(Cart::getTotalAmount).orElse(BigDecimal.ZERO);
    }

    /**
     * Проверить, пуста ли корзина
     */
    public boolean isCartEmpty(Long userId) {
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        return cart.map(c -> c.getItems().isEmpty()).orElse(true);
    }

    /**
     * Синхронизировать цены в корзине с актуальными ценами товаров
     * Полезно для длительно хранящихся корзин
     */
    @Transactional
    public Cart syncCartPrices(Long userId) {
        log.info("Синхронизация цен в корзине пользователя: {}", userId);

        Optional<Cart> cartOpt = cartRepository.findByUserIdWithItems(userId);
        if (cartOpt.isEmpty()) {
            return null;
        }

        Cart cart = cartOpt.get();
        boolean updated = false;

        for (CartItem item : cart.getItems()) {
            Product currentProduct = productRepository.findById(item.getProduct().getId())
                    .orElse(null);

            if (currentProduct != null && currentProduct.getIsActive()) {
                // Обновляем цену, если она изменилась
                if (!item.getPrice().equals(currentProduct.getPrice())) {
                    log.info("Обновление цены товара {} с {} на {}",
                            currentProduct.getName(), item.getPrice(), currentProduct.getPrice());
                    item.setPrice(currentProduct.getPrice());
                    updated = true;
                }
            } else {
                // Товар больше не доступен - удаляем из корзины
                log.warn("Товар {} больше не доступен, удаляем из корзины",
                        item.getProduct().getName());
                cart.removeItem(item.getProduct().getId());
                updated = true;
            }
        }

        if (updated) {
            Cart savedCart = cartRepository.save(cart);
            log.info("Цены в корзине пользователя {} синхронизированы", userId);
            return savedCart;
        }

        return cart;
    }

    /**
     * Валидация корзины перед оформлением заказа
     */
    public void validateCartForCheckout(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new IllegalArgumentException("Корзина не найдена"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста");
        }

        // Проверяем доступность всех товаров
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Товар не найден: " + item.getProduct().getName()));

            if (!product.getIsActive()) {
                throw new IllegalArgumentException(
                        "Товар больше не доступен: " + product.getName());
            }

            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Некорректное количество товара: " + product.getName());
            }
        }

        log.info("Корзина пользователя {} прошла валидацию для оформления заказа", userId);
    }

    /**
     * Увеличить количество товара на 1
     */
    @Transactional
    public Cart incrementItemQuantity(Long userId, Long productId) {
        log.info("Увеличение количества товара {} в корзине пользователя {}", productId, userId);

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new IllegalArgumentException("Корзина не найдена"));

        // Находим CartItem и явно сохраняем изменения
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден в корзине"));

        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartItemRepository.save(cartItem); // Явно сохраняем CartItem

        cart.recalculateTotals(); // Пересчитываем итоги корзины
        Cart savedCart = cartRepository.save(cart);

        log.info("Количество товара {} увеличено в корзине пользователя {}", productId, userId);

        return savedCart;
    }

    /**
     * Уменьшить количество товара на 1
     */
    @Transactional
    public Cart decrementItemQuantity(Long userId, Long productId) {
        log.info("Уменьшение количества товара {} в корзине пользователя {}", productId, userId);

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new IllegalArgumentException("Корзина не найдена"));

        // Находим CartItem
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден в корзине"));

        // Если количество станет 0, удаляем товар
        if (cartItem.getQuantity() <= 1) {
            cart.removeItem(productId);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            cartItemRepository.save(cartItem); // Явно сохраняем CartItem
            cart.recalculateTotals(); // Пересчитываем итоги корзины
        }

        Cart savedCart = cartRepository.save(cart);
        log.info("Количество товара {} уменьшено в корзине пользователя {}", productId, userId);

        return savedCart;
    }

    /**
     * Получить количество уникальных товаров в корзине
     */
    public int getUniqueItemsCount(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .map(Cart::getUniqueItemsCount)
                .orElse(0);
    }

    /**
     * Конвертировать Cart в CartResponseDTO
     */
    public CartResponseDTO toCartResponseDTO(Cart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(this::toCartItemDTO)
                .collect(Collectors.toList());

        return CartResponseDTO.builder()
                .id(cart.getId())
                .items(itemDTOs)
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .uniqueItemsCount(cart.getUniqueItemsCount())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    /**
     * Конвертировать CartItem в CartItemDTO
     */
    private CartItemDTO toCartItemDTO(CartItem item) {
        Product product = item.getProduct();

        return CartItemDTO.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(product.getImageUrl())
                .productBrand(product.getBrand())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}