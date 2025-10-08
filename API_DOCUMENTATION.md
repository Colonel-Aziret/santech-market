# API Документация для фронтенд-разработчика

## 📋 Оглавление
1. [Регистрация с модерацией](#регистрация-с-модерацией)
2. [JWT Аутентификация](#jwt-аутентификация)
3. [Формат ошибок](#формат-ошибок)
4. [Refresh Token механизм](#refresh-token-механизм)
5. [Разделение прав доступа](#разделение-прав-доступа)
6. [Примеры использования](#примеры-использования)

---

## 📝 Регистрация с модерацией

### Как работает система регистрации

1. **Пользователь регистрируется** через `/auth/register`
2. **Аккаунт создаётся со статусом `PENDING`** (на модерации)
3. **Менеджер одобряет/отклоняет** через админ-панель
4. **Пользователь получает доступ** только после одобрения

### Статусы пользователя

| Статус | Описание | Может войти? | Может покупать? |
|--------|----------|--------------|-----------------|
| `PENDING` | На модерации | ❌ Нет | ❌ Нет |
| `APPROVED` | Одобрен | ✅ Да | ✅ Да |
| `REJECTED` | Отклонён | ❌ Нет | ❌ Нет |
| `BLOCKED` | Заблокирован | ❌ Нет | ❌ Нет |

### 1. Регистрация нового пользователя

**Endpoint:** `POST /api/v1/auth/register`

**Request:**
```json
{
  "username": "user123",
  "password": "password123",
  "fullName": "Иван Иванов",
  "phoneNumber": "+996700123456",
  "email": "ivan@example.com"
}
```

**Response (201 Created):**
```json
{
  "id": 15,
  "username": "user123",
  "fullName": "Иван Иванов",
  "phoneNumber": "+996700123456",
  "email": "ivan@example.com",
  "status": "PENDING",
  "message": "Регистрация успешна! Ваша заявка на рассмотрении. Менеджер свяжется с вами в течение 1-2 рабочих дней."
}
```

**Error Response (400 Bad Request) - пользователь уже существует:**
```json
{
  "message": "Пользователь с таким логином уже существует",
  "code": "USER_ALREADY_EXISTS",
  "timestamp": "2025-10-08T19:30:00"
}
```

### 2. Попытка входа с PENDING статусом

**Endpoint:** `POST /api/v1/auth/login`

**Request:**
```json
{
  "username": "user123",
  "password": "password123"
}
```

**Response (401 Unauthorized):**
```json
{
  "message": "Неверный логин или пароль",
  "code": "AUTH_INVALID_CREDENTIALS",
  "timestamp": "2025-10-08T19:30:00"
}
```

> ⚠️ **Важно:** Пользователь с статусом PENDING не может войти в систему до одобрения.

### 3. Одобрение пользователя (для менеджера)

**Endpoint:** `PATCH /api/v1/users/{id}/approve`

**Headers:**
```
Authorization: Bearer <manager_access_token>
```

**Response (200 OK):**
```json
{
  "id": 15,
  "username": "user123",
  "fullName": "Иван Иванов",
  "status": "APPROVED",
  "isActive": true,
  ...
}
```

### 4. Отклонение пользователя (для менеджера)

**Endpoint:** `PATCH /api/v1/users/{id}/reject?reason=Недостаточно информации`

**Headers:**
```
Authorization: Bearer <manager_access_token>
```

**Response (200 OK):**
```json
{
  "id": 15,
  "username": "user123",
  "status": "REJECTED",
  "isActive": false,
  ...
}
```

### 5. Получить пользователей на модерации

**Endpoint:** `GET /api/v1/users/pending?page=0&size=20`

**Headers:**
```
Authorization: Bearer <manager_access_token>
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 15,
      "username": "user123",
      "fullName": "Иван Иванов",
      "phoneNumber": "+996700123456",
      "email": "ivan@example.com",
      "status": "PENDING",
      "createdAt": "2025-10-08T19:00:00"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

---

## 🔐 JWT Аутентификация

### Конфигурация токенов

| Параметр | Значение |
|----------|----------|
| **Access Token время жизни** | 24 часа (86400000 мс) |
| **Refresh Token время жизни** | 7 дней (604800000 мс) |
| **Тип токена** | Bearer |
| **Формат заголовка** | `Authorization: Bearer <access_token>` |

### Структура Access Token (JWT)

Токен содержит следующие claims:
```json
{
  "sub": "username",           // логин пользователя
  "userId": 1,                 // ID пользователя
  "role": "CLIENT",            // роль (CLIENT, ADMIN, MANAGER)
  "fullName": "Имя Фамилия",   // полное имя
  "iat": 1234567890,           // время создания
  "exp": 1234654290            // время истечения
}
```

---

## ❌ Формат ошибок

### Единый формат для всех ошибок

**Все ошибки возвращаются в едином формате:**

```json
{
  "message": "Сообщение об ошибке для пользователя",
  "code": "ERROR_CODE_FOR_BACKEND",
  "timestamp": "2025-10-08T19:30:00"
}
```

### Коды ошибок

#### Аутентификация и авторизация
| Код | Описание | HTTP Status |
|-----|----------|-------------|
| `AUTH_INVALID_CREDENTIALS` | Неверный логин или пароль | 401 |
| `AUTH_TOKEN_EXPIRED` | Access токен истёк | 401 |
| `AUTH_TOKEN_INVALID` | Невалидный access токен | 401 |
| `AUTH_REFRESH_TOKEN_EXPIRED` | Refresh токен истёк | 401 |
| `AUTH_REFRESH_TOKEN_INVALID` | Невалидный refresh токен | 401 |
| `AUTH_UNAUTHORIZED` | Не авторизован | 401 |
| `AUTH_FORBIDDEN` | Доступ запрещён | 403 |

#### Пользователи
| Код | Описание | HTTP Status |
|-----|----------|-------------|
| `USER_NOT_FOUND` | Пользователь не найден | 404 |
| `USER_ALREADY_EXISTS` | Пользователь уже существует | 400 |
| `USER_INACTIVE` | Пользователь неактивен | 400 |
| `USER_INVALID_PASSWORD` | Неверный пароль | 400 |

#### Товары
| Код | Описание | HTTP Status |
|-----|----------|-------------|
| `PRODUCT_NOT_FOUND` | Товар не найден | 404 |
| `PRODUCT_OUT_OF_STOCK` | Товар отсутствует на складе | 400 |
| `PRODUCT_INACTIVE` | Товар неактивен | 400 |

#### Заказы
| Код | Описание | HTTP Status |
|-----|----------|-------------|
| `ORDER_NOT_FOUND` | Заказ не найден | 404 |
| `ORDER_INVALID_STATUS` | Невалидный статус заказа | 400 |
| `ORDER_CANNOT_CANCEL` | Невозможно отменить заказ | 400 |
| `ORDER_ACCESS_DENIED` | Доступ к заказу запрещён | 403 |

#### Файлы
| Код | Описание | HTTP Status |
|-----|----------|-------------|
| `FILE_UPLOAD_FAILED` | Ошибка загрузки файла | 500 |
| `FILE_DELETE_FAILED` | Ошибка удаления файла | 500 |
| `FILE_INVALID_TYPE` | Неверный тип файла | 400 |
| `FILE_TOO_LARGE` | Файл слишком большой (>5MB) | 400 |

#### Валидация
| Код | Описание | HTTP Status |
|-----|----------|-------------|
| `VALIDATION_ERROR` | Ошибка валидации | 400 |
| `INVALID_REQUEST` | Некорректный запрос | 400 |

#### Сервер
| Код | Описание | HTTP Status |
|-----|----------|-------------|
| `INTERNAL_SERVER_ERROR` | Внутренняя ошибка сервера | 500 |

---

## 🔄 Refresh Token механизм

### 1. Вход в систему (Login)

**Endpoint:** `POST /api/v1/auth/login`

**Request:**
```json
{
  "username": "user123",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "username": "user123",
    "fullName": "Иван Иванов",
    "email": "ivan@example.com",
    "phoneNumber": "+996700123456",
    "role": "CLIENT",
    "isActive": true
  }
}
```

### 2. Обновление токена (Refresh)

**Endpoint:** `POST /api/v1/auth/refresh`

**Request:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "660f9511-f30c-52e5-b827-557766551111",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

**Error Response (401 Unauthorized):**
```json
{
  "message": "Refresh токен истёк. Пожалуйста, выполните вход заново",
  "code": "AUTH_REFRESH_TOKEN_INVALID",
  "timestamp": "2025-10-08T19:30:00"
}
```

### 3. Выход из системы (Logout)

**Endpoint:** `POST /api/v1/auth/logout`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "message": "Вы успешно вышли из системы"
}
```

---

## 🔒 Разделение прав доступа

### Публичные эндпоинты (без токена)

- `POST /api/v1/auth/register` - регистрация
- `POST /api/v1/auth/login` - вход
- `POST /api/v1/auth/refresh` - обновление токена
- `GET /api/v1/categories` - список категорий
- `GET /api/v1/products/**` - каталог товаров
- `GET /api/v1/promo-banners` - промо-баннеры

### Для авторизованных пользователей (любая роль)

- `GET /api/v1/auth/profile` - получить профиль
- `PUT /api/v1/auth/profile` - обновить профиль
- `POST /api/v1/auth/change-password` - сменить пароль
- `POST /api/v1/auth/logout` - выход
- `GET /api/v1/cart/**` - корзина
- `POST /api/v1/orders` - создать заказ
- `GET /api/v1/orders/my` - мои заказы

### Только для ADMIN или MANAGER

- `POST /api/v1/users` - создать пользователя
- `PUT /api/v1/users/{id}` - обновить пользователя
- `GET /api/v1/users/pending` - получить пользователей на модерации
- `PATCH /api/v1/users/{id}/approve` - одобрить пользователя
- `PATCH /api/v1/users/{id}/reject` - отклонить пользователя
- `POST /api/v1/products` - создать товар
- `PUT /api/v1/products/{id}` - обновить товар
- `PATCH /api/v1/orders/{id}/status` - изменить статус заказа
- `GET /api/v1/orders/stats` - статистика заказов

### Только для ADMIN

- `PATCH /api/v1/users/{id}/deactivate` - деактивировать пользователя
- `PATCH /api/v1/users/{id}/activate` - активировать пользователя

---

## 💡 Примеры использования

### React Native пример

```javascript
import AsyncStorage from '@react-native-async-storage/async-storage';

const API_URL = 'http://localhost:8080/api/v1';

// 1. Логин
async function login(username, password) {
  try {
    const response = await fetch(`${API_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    });

    const data = await response.json();

    if (response.ok) {
      // Сохраняем токены
      await AsyncStorage.setItem('accessToken', data.accessToken);
      await AsyncStorage.setItem('refreshToken', data.refreshToken);
      await AsyncStorage.setItem('user', JSON.stringify(data.user));
      return { success: true, data };
    } else {
      // Обработка ошибки
      return {
        success: false,
        error: {
          message: data.message,
          code: data.code
        }
      };
    }
  } catch (error) {
    return { success: false, error: { message: 'Ошибка сети', code: 'NETWORK_ERROR' } };
  }
}

// 2. API запрос с автоматическим обновлением токена
async function apiRequest(endpoint, options = {}) {
  let accessToken = await AsyncStorage.getItem('accessToken');

  // Первая попытка с текущим токеном
  let response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${accessToken}`,
    },
  });

  // Если токен истёк - обновляем
  if (response.status === 401) {
    const refreshToken = await AsyncStorage.getItem('refreshToken');

    const refreshResponse = await fetch(`${API_URL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });

    if (refreshResponse.ok) {
      const data = await refreshResponse.json();

      // Сохраняем новые токены
      await AsyncStorage.setItem('accessToken', data.accessToken);
      await AsyncStorage.setItem('refreshToken', data.refreshToken);

      // Повторяем запрос с новым токеном
      response = await fetch(`${API_URL}${endpoint}`, {
        ...options,
        headers: {
          ...options.headers,
          'Authorization': `Bearer ${data.accessToken}`,
        },
      });
    } else {
      // Refresh token тоже истёк - редирект на логин
      await logout();
      // Navigate to login screen
      return null;
    }
  }

  const data = await response.json();

  if (response.ok) {
    return { success: true, data };
  } else {
    return {
      success: false,
      error: {
        message: data.message,
        code: data.code
      }
    };
  }
}

// 3. Выход
async function logout() {
  const accessToken = await AsyncStorage.getItem('accessToken');

  await fetch(`${API_URL}/auth/logout`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
    },
  });

  await AsyncStorage.multiRemove(['accessToken', 'refreshToken', 'user']);
}

// 4. Пример использования
async function getMyOrders() {
  const result = await apiRequest('/orders/my');

  if (result.success) {
    console.log('Заказы:', result.data);
  } else {
    console.error('Ошибка:', result.error.message, result.error.code);
    // Обработка по коду
    if (result.error.code === 'ORDER_NOT_FOUND') {
      // Показать сообщение "Заказы не найдены"
    }
  }
}
```

### Обработка ошибок по кодам

```javascript
function handleError(error) {
  switch(error.code) {
    case 'AUTH_INVALID_CREDENTIALS':
      return 'Неверный логин или пароль';

    case 'AUTH_REFRESH_TOKEN_EXPIRED':
    case 'AUTH_REFRESH_TOKEN_INVALID':
      // Перенаправить на экран логина
      navigateToLogin();
      return 'Сессия истекла. Войдите заново';

    case 'VALIDATION_ERROR':
      return `Ошибка валидации: ${error.message}`;

    case 'FILE_TOO_LARGE':
      return 'Файл слишком большой. Максимум 5MB';

    default:
      return error.message || 'Произошла ошибка';
  }
}
```

---

## 📝 Важные замечания

### 1. Безопасность токенов
- **Access token** хранить в памяти приложения (не в AsyncStorage если возможно)
- **Refresh token** можно хранить в защищённом хранилище (AsyncStorage с шифрованием)
- При выходе **ВСЕГДА** удалять оба токена

### 2. Логика обновления токена
- При получении **401** сначала пробуем обновить токен через `/auth/refresh`
- Если refresh тоже возвращает 401 - редирект на логин
- После успешного обновления - **повторить** оригинальный запрос

### 3. Роли и доступ
- **CLIENT** - обычный пользователь (может делать заказы, управлять корзиной)
- **MANAGER** - менеджер (может управлять заказами, товарами, пользователями)
- **ADMIN** - администратор (полный доступ ко всем функциям)

### 4. Формат даты/времени
- Все даты в формате ISO 8601: `2025-10-08T19:30:00`
- Временная зона: UTC

---

## 🔗 Swagger документация

Полная документация доступна по адресу:
```
http://localhost:8080/api/v1/swagger-ui.html
```

Там можно найти:
- Все эндпоинты с примерами
- Схемы запросов и ответов
- Возможность тестировать API прямо в браузере
