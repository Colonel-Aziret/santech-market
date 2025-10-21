# Гайд по пагинации для фронтенд-разработчика

## Как работает пагинация в API

Все эндпоинты с пагинацией теперь используют **query параметры** вместо тела запроса.

### Формат запроса

```
GET /api/v1/notifications?page=0&size=10&sort=createdAt,desc
```

### Параметры

- `page` - номер страницы (начинается с 0)
- `size` - количество элементов на странице
- `sort` - сортировка в формате `поле,направление` (например `createdAt,desc` или `name,asc`)

### Примеры

#### 1. Получить уведомления (первая страница, 20 элементов)
```
GET /api/v1/notifications?page=0&size=20
```

#### 2. Получить товары (вторая страница, 10 элементов, сортировка по цене)
```
GET /api/v1/products?page=1&size=10&sort=price,asc
```

#### 3. Получить заказы (с сортировкой по дате создания)
```
GET /api/v1/orders/my?page=0&size=10&sort=createdAt,desc
```

#### 4. Поиск товаров с пагинацией
```
GET /api/v1/products/search?query=труба&page=0&size=20&sort=name,asc
```

### Формат ответа

Ответ всегда приходит в формате Page<T>:

```json
{
  "content": [...],           // Массив элементов на текущей странице
  "pageable": {
    "pageNumber": 0,          // Текущая страница
    "pageSize": 20,           // Размер страницы
    "sort": {...}             // Информация о сортировке
  },
  "totalElements": 100,       // Общее количество элементов
  "totalPages": 5,            // Общее количество страниц
  "last": false,              // Это последняя страница?
  "first": true,              // Это первая страница?
  "numberOfElements": 20,     // Количество элементов на текущей странице
  "size": 20,                 // Размер страницы
  "number": 0,                // Номер текущей страницы
  "empty": false              // Страница пустая?
}
```

### Значения по умолчанию

Если параметры не указаны, используются значения по умолчанию:

| Эндпоинт | size | sort |
|----------|------|------|
| `/notifications` | 20 | createdAt |
| `/products` | 20 | - |
| `/orders/my` | 10 | - |
| `/users/search` | 20 | - |

### Эндпоинты с пагинацией

1. **Notifications** (`/api/v1/notifications`)
2. **Products** (`/api/v1/products`, `/api/v1/products/category/{id}`, `/api/v1/products/search`, и др.)
3. **Orders** (`/api/v1/orders/my`, `/api/v1/orders/status/{status}`)
4. **Users** (`/api/v1/users/search`, `/api/v1/users/pending`)
5. **Promo Banners** (`/api/v1/promo-banners`)

### Swagger UI

В Swagger UI теперь параметры отображаются правильно как query параметры, а не как тело запроса.

## Миграция с старого формата

### Было (НЕПРАВИЛЬНО):
```javascript
// POST /api/v1/notifications
{
  "page": 0,
  "size": 10,
  "sort": ["createdAt,desc"]
}
```

### Стало (ПРАВИЛЬНО):
```javascript
// GET /api/v1/notifications?page=0&size=10&sort=createdAt,desc
```

### Пример кода на JavaScript/TypeScript

```typescript
// Функция для получения уведомлений с пагинацией
async function getNotifications(page = 0, size = 20, sort = 'createdAt,desc') {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
    sort: sort
  });

  const response = await fetch(`/api/v1/notifications?${params}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  return response.json();
}

// Использование
const notifications = await getNotifications(0, 20, 'createdAt,desc');
console.log('Total pages:', notifications.totalPages);
console.log('Total elements:', notifications.totalElements);
console.log('Current page:', notifications.content);
```

## Вопросы?

Если что-то непонятно, смотри Swagger UI по адресу: http://localhost:8080/api/v1/swagger-ui.html
