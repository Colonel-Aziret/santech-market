package kg.santechmarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.santechmarket.config.JwtService;
import kg.santechmarket.dto.AuthDto;
import kg.santechmarket.dto.ErrorResponse;
import kg.santechmarket.entity.RefreshToken;
import kg.santechmarket.entity.User;
import kg.santechmarket.enums.ErrorCode;
import kg.santechmarket.enums.UserRole;
import kg.santechmarket.enums.UserStatus;
import kg.santechmarket.service.RefreshTokenService;
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
    private final RefreshTokenService refreshTokenService;
    private final kg.santechmarket.service.PasswordResetService passwordResetService;

    /**
     * Регистрация нового пользователя
     */
    @PostMapping("/register")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Регистрация нового пользователя со статусом PENDING (ожидает одобрения менеджера). После регистрации пользователь не может войти в систему до одобрения менеджером."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно зарегистрирован",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthDto.RegisterResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации (логин/телефон/email уже существует или неверный формат)",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> register(@Valid @RequestBody AuthDto.RegisterRequest request) {
        log.info("Регистрация нового пользователя: {}", request.username());

        try {
            // Проверяем, существует ли пользователь
            if (userService.existsByUsername(request.username())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Пользователь с таким логином уже существует", ErrorCode.USER_ALREADY_EXISTS.getCode()));
            }

            if (userService.existsByPhoneNumber(request.phoneNumber())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Пользователь с таким номером телефона уже существует", ErrorCode.USER_ALREADY_EXISTS.getCode()));
            }

            if (request.email() != null && !request.email().isEmpty() && userService.existsByEmail(request.email())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Пользователь с таким email уже существует", ErrorCode.USER_ALREADY_EXISTS.getCode()));
            }

            // Создаём пользователя
            User newUser = new User();
            newUser.setUsername(request.username());
            newUser.setPassword(request.password()); // будет хеширован в сервисе
            newUser.setFullName(request.fullName());
            newUser.setPhoneNumber(request.phoneNumber());
            newUser.setEmail(request.email());
            newUser.setRole(UserRole.CLIENT);
            newUser.setStatus(UserStatus.PENDING);
            newUser.setIsActive(false); // неактивен до одобрения

            User savedUser = userService.createUser(newUser);

            AuthDto.RegisterResponse response = new AuthDto.RegisterResponse(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getFullName(),
                    savedUser.getPhoneNumber(),
                    savedUser.getEmail(),
                    savedUser.getStatus().name(),
                    "Регистрация успешна! Ваша заявка на рассмотрении. Менеджер свяжется с вами в течение 1-2 рабочих дней."
            );

            log.info("Пользователь {} успешно зарегистрирован со статусом PENDING", savedUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Ошибка валидации при регистрации {}: {}", request.username(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage(), ErrorCode.VALIDATION_ERROR.getCode()));
        } catch (Exception e) {
            log.error("Ошибка при регистрации пользователя {}: {}", request.username(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Не удалось зарегистрироваться", ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
        }
    }

    /**
     * Вход в систему
     */
    @PostMapping("/login")
    @Operation(
            summary = "Вход в систему",
            description = "Аутентификация пользователя по логину и паролю. При успешной авторизации возвращает access токен (срок действия 24 часа) и refresh токен для продления сессии."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Успешная авторизация",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthDto.LoginResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Неверный логин или пароль",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            )
    })
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

            // Генерация access токена
            String accessToken = jwtService.generateToken(user);

            // Генерация refresh токена
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            // Создание ответа
            AuthDto.LoginResponse response = new AuthDto.LoginResponse(
                    accessToken,
                    refreshToken.getToken(),
                    "Bearer",
                    jwtService.getExpirationMs() / 1000, // в секундах
                    mapToUserInfo(user)
            );

            log.info("Успешный вход пользователя: {} с ролью {}", user.getUsername(), user.getRole());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            log.warn("Неудачная попытка входа для пользователя: {}", loginRequest.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Неверный логин или пароль", ErrorCode.AUTH_INVALID_CREDENTIALS.getCode()));
        } catch (Exception e) {
            log.error("Ошибка при входе пользователя {}: {}", loginRequest.username(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
        }
    }

    /**
     * Получение профиля текущего пользователя
     */
    @GetMapping("/profile")
    @Operation(
            summary = "Получить профиль текущего пользователя",
            description = "Получение полной информации о профиле авторизованного пользователя"
    )
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "JWT")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Профиль успешно получен",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthDto.UserInfo.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован (отсутствует или недействителен токен)",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<AuthDto.UserInfo> getProfile(@AuthenticationPrincipal User currentUser) {
        log.debug("Запрос профиля пользователя: {}", currentUser.getUsername());

        AuthDto.UserInfo userInfo = mapToUserInfo(currentUser);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Обновление профиля пользователя
     */
    @PutMapping("/profile")
    @Operation(
            summary = "Обновить профиль",
            description = "Обновление информации профиля текущего пользователя (ФИО, email, телефон)"
    )
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "JWT")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Профиль успешно обновлен",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthDto.UserInfo.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации данных",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal User currentUser,
                                           @Valid @RequestBody AuthDto.UpdateProfileRequest request) {
        log.info("Обновление профиля пользователя: {}", currentUser.getUsername());

        try {
            // Создаем DTO с обновленными данными
            kg.santechmarket.dto.UserDto.UpdateUserRequest updateRequest =
                    new kg.santechmarket.dto.UserDto.UpdateUserRequest(
                            request.username(), // username может быть изменен
                            request.fullName(),
                            request.email(),
                            request.phoneNumber(),
                            null, // password не меняется
                            null, // role не меняется
                            null  // isActive не меняется
                    );

            // Обновляем пользователя
            User savedUser = userService.updateUser(currentUser.getId(), updateRequest);

            AuthDto.UserInfo userInfo = mapToUserInfo(savedUser);

            log.info("Профиль пользователя {} успешно обновлен", savedUser.getUsername());
            return ResponseEntity.ok(userInfo);

        } catch (IllegalArgumentException e) {
            log.warn("Ошибка валидации при обновлении профиля {}: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), ErrorCode.VALIDATION_ERROR.getCode()));
        } catch (Exception e) {
            log.error("Ошибка при обновлении профиля {}: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Не удалось обновить профиль", ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
        }
    }

    /**
     * Смена пароля
     */
    @PostMapping("/change-password")
    @Operation(
            summary = "Сменить пароль",
            description = "Изменение пароля текущего пользователя. Требуется указать текущий пароль для подтверждения."
    )
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "JWT")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Пароль успешно изменен",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = SuccessResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Неверный текущий пароль",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal User currentUser,
                                            @Valid @RequestBody AuthDto.ChangePasswordRequest request) {
        log.info("Запрос смены пароля для пользователя: {}", currentUser.getUsername());

        try {
            // Проверяем текущий пароль
            if (!userService.checkPassword(currentUser, request.currentPassword())) {
                log.warn("Неверный текущий пароль при смене для пользователя: {}", currentUser.getUsername());
                return ResponseEntity.badRequest().body(new ErrorResponse("Неверный текущий пароль", ErrorCode.USER_INVALID_PASSWORD.getCode()));
            }

            // Создаем DTO с новым паролем
            kg.santechmarket.dto.UserDto.UpdateUserRequest updateRequest =
                    new kg.santechmarket.dto.UserDto.UpdateUserRequest(
                            null, // username не меняется
                            null, // fullName не меняется
                            null, // email не меняется
                            null, // phoneNumber не меняется
                            request.newPassword(), // меняем только пароль
                            null, // role не меняется
                            null  // isActive не меняется
                    );

            // Обновляем пользователя
            userService.updateUser(currentUser.getId(), updateRequest);

            log.info("Пароль пользователя {} успешно изменен", currentUser.getUsername());
            return ResponseEntity.ok(new SuccessResponse("Пароль успешно изменен"));

        } catch (Exception e) {
            log.error("Ошибка при смене пароля для {}: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Не удалось изменить пароль", ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
        }
    }

    /**
     * Обновление access токена с помощью refresh токена
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Обновить токен",
            description = "Обновление access токена с помощью refresh токена. Возвращает новую пару токенов (access и refresh)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Токен успешно обновлен",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthDto.RefreshTokenResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh токен не найден или истек",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> refreshToken(@Valid @RequestBody AuthDto.RefreshTokenRequest request) {
        String refreshTokenValue = request.refreshToken();
        log.info("Запрос обновления токена");

        try {
            // Ищем refresh токен
            RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                    .orElseThrow(() -> new IllegalArgumentException("Refresh токен не найден"));

            // Проверяем, не истёк ли токен
            refreshToken = refreshTokenService.verifyExpiration(refreshToken);

            // Получаем пользователя
            User user = refreshToken.getUser();

            // Генерируем новый access токен
            String newAccessToken = jwtService.generateToken(user);

            // Создаём новый refresh токен
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

            // Создание ответа
            AuthDto.RefreshTokenResponse response = new AuthDto.RefreshTokenResponse(
                    newAccessToken,
                    newRefreshToken.getToken(),
                    "Bearer",
                    jwtService.getExpirationMs() / 1000 // в секундах
            );

            log.info("Токен успешно обновлён для пользователя: {}", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Ошибка при обновлении токена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage(), ErrorCode.AUTH_REFRESH_TOKEN_INVALID.getCode()));
        } catch (Exception e) {
            log.error("Ошибка при обновлении токена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Не удалось обновить токен", ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
        }
    }

    /**
     * Выход из системы (удаление refresh токена)
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Выход из системы",
            description = "Удаление refresh токена пользователя. После выхода для повторной авторизации потребуется выполнить вход заново."
    )
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "JWT")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Выход выполнен успешно",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = SuccessResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> logout(@AuthenticationPrincipal User currentUser) {
        log.info("Выход из системы для пользователя: {}", currentUser.getUsername());

        try {
            refreshTokenService.deleteByUser(currentUser);
            log.info("Пользователь {} вышел из системы", currentUser.getUsername());
            return ResponseEntity.ok(new SuccessResponse("Вы успешно вышли из системы"));
        } catch (Exception e) {
            log.error("Ошибка при выходе из системы для {}: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Не удалось выйти из системы", ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
        }
    }

    /**
     * Запрос на сброс пароля (забыли пароль)
     */
    @PostMapping("/forgot-password")
    @Operation(
            summary = "Забыли пароль",
            description = "Отправка кода для сброса пароля на номер телефона пользователя. Код действителен 15 минут."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Код успешно отправлен",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthDto.ForgotPasswordResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Пользователь с таким номером не найден",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody AuthDto.ForgotPasswordRequest request) {
        log.info("Запрос на сброс пароля для номера: {}", request.phoneNumber());

        try {
            // Поиск пользователя по номеру телефона
            User user = userService.findByPhoneNumber(request.phoneNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь с таким номером телефона не найден"));

            // Создаем токен сброса пароля
            kg.santechmarket.entity.PasswordResetToken resetToken = passwordResetService.createPasswordResetToken(user);

            // Маскируем номер телефона для ответа
            String maskedPhone = passwordResetService.maskPhoneNumber(request.phoneNumber());

            AuthDto.ForgotPasswordResponse response = new AuthDto.ForgotPasswordResponse(
                    "Код для сброса пароля отправлен. Проверьте ваши уведомления.",
                    maskedPhone
            );

            log.info("Код сброса пароля создан для пользователя: {}", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Ошибка при запросе сброса пароля: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage(), ErrorCode.USER_NOT_FOUND.getCode()));
        } catch (Exception e) {
            log.error("Ошибка при запросе сброса пароля: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Не удалось отправить код сброса пароля", ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
        }
    }

    /**
     * Подтверждение сброса пароля с кодом
     */
    @PostMapping("/reset-password")
    @Operation(
            summary = "Сброс пароля",
            description = "Сброс пароля с использованием кода подтверждения из SMS/уведомления"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Пароль успешно изменен",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthDto.ResetPasswordResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Неверный или истекший код",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<?> resetPassword(@Valid @RequestBody AuthDto.ResetPasswordRequest request) {
        log.info("Запрос на сброс пароля с кодом для номера: {}", request.phoneNumber());

        try {
            // Поиск пользователя по номеру телефона
            User user = userService.findByPhoneNumber(request.phoneNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь с таким номером телефона не найден"));

            // Поиск и проверка токена
            kg.santechmarket.entity.PasswordResetToken resetToken = passwordResetService.findActiveToken(request.code())
                    .orElseThrow(() -> new IllegalArgumentException("Неверный или истекший код подтверждения"));

            // Проверка, что токен принадлежит этому пользователю
            if (!resetToken.getUser().getId().equals(user.getId())) {
                log.warn("Попытка использования чужого токена");
                throw new IllegalArgumentException("Неверный код подтверждения");
            }

            // Обновляем пароль пользователя
            kg.santechmarket.dto.UserDto.UpdateUserRequest updateRequest =
                    new kg.santechmarket.dto.UserDto.UpdateUserRequest(
                            null, null, null, null,
                            request.newPassword(),
                            null, null
                    );
            userService.updateUser(user.getId(), updateRequest);

            // Отмечаем токен как использованный
            passwordResetService.markTokenAsUsed(resetToken);

            AuthDto.ResetPasswordResponse response = new AuthDto.ResetPasswordResponse(
                    "Пароль успешно изменен. Теперь вы можете войти с новым паролем."
            );

            log.info("Пароль успешно сброшен для пользователя: {}", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Ошибка при сбросе пароля: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), ErrorCode.VALIDATION_ERROR.getCode()));
        } catch (Exception e) {
            log.error("Ошибка при сбросе пароля: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Не удалось сбросить пароль", ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
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
     * DTO для успешных операций
     */
    @io.swagger.v3.oas.annotations.media.Schema(description = "Успешный ответ операции")
    public record SuccessResponse(
            @io.swagger.v3.oas.annotations.media.Schema(description = "Сообщение об успешной операции", example = "Операция выполнена успешно")
            String message
    ) {
    }
}