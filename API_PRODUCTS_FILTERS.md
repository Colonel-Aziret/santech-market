# API - Фильтрация товаров

Документация для фронтенд-разработчиков по фильтрации товаров в SanTech Market API.

**Base URL:** `http://localhost:8080/api/v1`

---

## 1. Основная фильтрация

### Endpoint
```
GET /products/filter
```

### Query параметры (все опциональны)

| Параметр | Тип | Описание | Пример |
|----------|-----|----------|--------|
| `categoryId` | Long | ID категории товара | `5` |
| `brand` | String | Бренд/производитель | `PRO AQUA`, `Lammin` |
| `minPrice` | Decimal | Минимальная цена в сомах | `100`, `500.50` |
| `maxPrice` | Decimal | Максимальная цена в сомах | `5000`, `10000.99` |
| `search` | String | Текстовый поиск по названию и описанию | `труба`, `полипропилен` |
| `page` | Integer | Номер страницы (начинается с 0) | `0`, `1`, `2` |
| `size` | Integer | Количество элементов на странице | `20` (по умолчанию) |
| `sort` | String | Сортировка (поле,направление) | `price,asc`, `name,desc` |

### Примеры запросов

#### Пример 1: Фильтр по категории и цене
```http
GET /products/filter?categoryId=5&minPrice=100&maxPrice=5000
```

#### Пример 2: Поиск товаров бренда с сортировкой
```http
GET /products/filter?brand=PRO AQUA&maxPrice=3000&sort=price,asc
```

#### Пример 3: Комбинация всех фильтров
```http
GET /products/filter?categoryId=5&brand=Lammin&minPrice=100&maxPrice=5000&search=труба&page=1&size=20&sort=price,asc
```

#### Пример 4: Только поиск
```http
GET /products/filter?search=полипропиленовая труба
```

### Формат ответа
```json
{
  "content": [
    {
      "id": 1,
      "name": "Труба PPR PN25 арм. стеклов. 20x3,4 (100м)",
      "description": "Полипропиленовая труба с армированием стекловолокном",
      "price": 1250.00,
      "oldPrice": 1500.00,
      "brand": "PRO AQUA",
      "sku": "TR-PPR-PN25-20",
      "imageUrl": "https://example.com/images/pipe-20.jpg",
      "specifications": "{\"diameter\":\"20 мм\",\"pressure\":\"PN25\",\"length\":\"100м\"}",
      "isActive": true,
      "isFeatured": false,
      "category": {
        "id": 5,
        "name": "Полипропиленовые трубы"
      },
      "createdAt": "2025-01-15T10:30:00",
      "updatedAt": "2025-01-15T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalPages": 5,
  "totalElements": 95,
  "last": false,
  "first": true,
  "size": 20,
  "number": 0,
  "numberOfElements": 20,
  "empty": false
}
```

---

## 2. Дополнительные эндпоинты

### 2.1 Текстовый поиск
```http
GET /products/search?query=полипропиленовая труба&page=0&size=20
```

**Параметры:**
- `query` (optional) - поисковый запрос. Если не указан, вернет все активные товары
- `page` - номер страницы
- `size` - размер страницы

---

### 2.2 Товары по категории
```http
GET /products/category/{categoryId}?page=0&size=20
```

**Path параметры:**
- `categoryId` (required) - ID категории

**Query параметры:**
- `page`, `size`, `sort`

---

### 2.3 Товары по категории + подкатегории
```http
GET /products/category/{categoryId}/with-subcategories?page=0&size=20
```

Возвращает товары из указанной категории и всех её подкатегорий.

---

### 2.4 Рекомендуемые товары
```http
GET /products/featured
```

Возвращает список товаров для главной страницы (без пагинации).

---

### 2.5 Товары со скидкой
```http
GET /products/discounted?page=0&size=20
```

Возвращает товары, у которых `oldPrice` больше `price`.

---

### 2.6 Похожие товары
```http
GET /products/{id}/similar?limit=10
```

**Path параметры:**
- `id` (required) - ID товара

**Query параметры:**
- `limit` (default: 10) - максимальное количество

---

## 3. Поиск по характеристикам

### 3.1 Поиск по одной характеристике
```http
GET /products/search-by-spec?specKey=diameter&specValue=20 мм&page=0&size=20
```

**Query параметры:**
- `specKey` (required) - ключ характеристики из specifications
- `specValue` (required) - значение характеристики
- `page`, `size`

---

### 3.2 Специальный поиск труб
```http
GET /products/search-pipes?diameter=20&pressure=PN25&material=полипропилен&page=0&size=20
```

**Query параметры (все опциональны):**
- `diameter` - диаметр трубы
- `pressure` - рабочее давление
- `material` - материал
- `page`, `size`

---

## 4. Вспомогательные эндпоинты

### 4.1 Метаданные для фильтров
```http
GET /products/filters/metadata
```

**Назначение:** Получить все доступные значения для построения UI фильтров

**Формат ответа:**
```json
{
  "brands": ["PRO AQUA", "Lammin", "Valtec", "Rehau"],
  "diameters": ["16 мм", "20 мм", "25 мм", "32 мм"],
  "pressures": ["PN10", "PN16", "PN20", "PN25"],
  "materials": ["полипропилен", "металлопластик", "PEX"],
  "reinforcements": ["стекловолокно", "алюминий", "базальт"],
  "lengths": ["4м", "100м", "200м"],
  "purposes": ["холодная вода", "горячая вода", "отопление"],
  "wallThicknesses": ["2.8 мм", "3.4 мм", "4.2 мм"],
  "priceRange": {
    "min": 50.00,
    "max": 25000.00
  }
}
```

---

### 4.2 Список всех брендов
```http
GET /products/brands
```

**Формат ответа:**
```json
["PRO AQUA", "Lammin", "Valtec", "Rehau", "Tebo"]
```

---

## 5. Поля объекта Product

### Основные поля

| Поле | Тип | Nullable | Описание |
|------|-----|----------|----------|
| `id` | Long | No | Уникальный идентификатор |
| `name` | String | No | Название товара (макс. 200 символов) |
| `description` | String | Yes | Описание товара (макс. 1000 символов) |
| `price` | Decimal | No | Текущая цена (всегда > 0) |
| `oldPrice` | Decimal | Yes | Старая цена для отображения скидки |
| `brand` | String | Yes | Бренд/производитель (макс. 100 символов) |
| `sku` | String | Yes | Артикул товара (макс. 100 символов) |
| `imageUrl` | String | Yes | URL главного изображения |
| `specifications` | String (JSON) | Yes | Характеристики товара в формате JSON |
| `isActive` | Boolean | No | Активен ли товар (default: true) |
| `isFeatured` | Boolean | No | Рекомендуемый товар (default: false) |
| `category` | Object | No | Объект категории |
| `createdAt` | DateTime | No | Дата создания |
| `updatedAt` | DateTime | No | Дата последнего обновления |

### Объект Category

```json
{
  "id": 5,
  "name": "Полипропиленовые трубы",
  "description": "Трубы из полипропилена для водоснабжения и отопления",
  "imageUrl": "https://example.com/categories/ppr-pipes.jpg",
  "displayOrder": 1,
  "isActive": true,
  "parent": {
    "id": 1,
    "name": "Трубы"
  }
}
```

### Поле specifications (JSON)

Примеры возможных ключей в specifications:

```json
{
  "diameter": "20 мм",
  "pressure": "PN25",
  "material": "полипропилен",
  "length": "100м",
  "reinforcement": "стекловолокно",
  "purpose": "горячая вода",
  "wallThickness": "3.4 мм"
}
```

**Типичные ключи:**
- `diameter` - Диаметр
- `pressure` - Рабочее давление
- `material` - Материал
- `length` - Длина
- `reinforcement` - Тип армирования
- `purpose` - Назначение
- `wallThickness` - Толщина стенки

---

## 6. Варианты сортировки

### Формат параметра sort
```
sort={поле},{направление}
```

### Доступные поля для сортировки

| Поле | Описание |
|------|----------|
| `price` | Цена товара |
| `name` | Название товара |
| `createdAt` | Дата создания |
| `updatedAt` | Дата обновления |
| `brand` | Бренд |

### Направления сортировки
- `asc` - по возрастанию (A-Z, 0-9, старые-новые)
- `desc` - по убыванию (Z-A, 9-0, новые-старые)

### Примеры

```http
# По цене (возрастание)
GET /products/filter?sort=price,asc

# По цене (убывание)
GET /products/filter?sort=price,desc

# По названию (A-Z)
GET /products/filter?sort=name,asc

# По дате создания (новые первые)
GET /products/filter?sort=createdAt,desc

# Множественная сортировка (сначала по бренду, потом по цене)
GET /products/filter?sort=brand,asc&sort=price,asc
```

---

## 7. Пагинация

### Параметры пагинации

| Параметр | Тип | Default | Описание |
|----------|-----|---------|----------|
| `page` | Integer | 0 | Номер страницы (начинается с 0) |
| `size` | Integer | 20 | Количество элементов на странице |

### Поля ответа для пагинации

```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalPages": 5,
  "totalElements": 95,
  "last": false,
  "first": true,
  "size": 20,
  "number": 0,
  "numberOfElements": 20,
  "empty": false
}
```

**Важные поля:**
- `totalPages` - общее количество страниц
- `totalElements` - общее количество элементов
- `first` - первая ли это страница
- `last` - последняя ли это страница
- `number` - текущий номер страницы
- `numberOfElements` - количество элементов на текущей странице

---

## 8. Примеры использования для фронтенда

### Пример 1: Страница каталога с фильтрами

```typescript
interface ProductFilters {
  categoryId?: number;
  brand?: string;
  minPrice?: number;
  maxPrice?: number;
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
}

async function fetchProducts(filters: ProductFilters) {
  const params = new URLSearchParams();

  if (filters.categoryId) params.append('categoryId', filters.categoryId.toString());
  if (filters.brand) params.append('brand', filters.brand);
  if (filters.minPrice) params.append('minPrice', filters.minPrice.toString());
  if (filters.maxPrice) params.append('maxPrice', filters.maxPrice.toString());
  if (filters.search) params.append('search', filters.search);
  if (filters.page) params.append('page', filters.page.toString());
  if (filters.size) params.append('size', filters.size.toString());
  if (filters.sort) params.append('sort', filters.sort);

  const response = await fetch(`/api/v1/products/filter?${params.toString()}`);
  return response.json();
}

// Использование
const products = await fetchProducts({
  categoryId: 5,
  minPrice: 100,
  maxPrice: 5000,
  sort: 'price,asc',
  page: 0,
  size: 20
});
```

### Пример 2: Загрузка метаданных для фильтров

```typescript
async function loadFilterMetadata() {
  const response = await fetch('/api/v1/products/filters/metadata');
  const metadata = await response.json();

  // Теперь можно использовать metadata.brands, metadata.priceRange и т.д.
  return metadata;
}

// Использование для построения UI
const metadata = await loadFilterMetadata();

// Dropdown для брендов
const brandOptions = metadata.brands.map(brand => ({
  label: brand,
  value: brand
}));

// Slider для цены
const priceSlider = {
  min: metadata.priceRange.min,
  max: metadata.priceRange.max
};
```

### Пример 3: Поиск с автодополнением

```typescript
// Debounced поиск
let searchTimeout: NodeJS.Timeout;

function onSearchInput(query: string) {
  clearTimeout(searchTimeout);

  searchTimeout = setTimeout(async () => {
    if (query.length >= 3) {
      const response = await fetch(
        `/api/v1/products/search?query=${encodeURIComponent(query)}&size=10`
      );
      const results = await response.json();
      displaySearchResults(results.content);
    }
  }, 300);
}
```

---

## 9. Коды ошибок

| Код | Описание | Решение |
|-----|----------|---------|
| 200 | Успешно | - |
| 400 | Неверные параметры запроса | Проверить формат параметров |
| 404 | Товар/категория не найдены | Проверить существование ID |
| 500 | Внутренняя ошибка сервера | Обратиться к бэкенду |

---

## 10. Дополнительная информация

### Swagger UI
Полная документация API доступна по адресу:
```
http://localhost:8080/swagger-ui.html
```

### Формат дат
Все даты в формате ISO 8601:
```
2025-01-15T10:30:00
```

### Кодировка
Все текстовые данные в UTF-8. При отправке кириллицы в query параметрах используйте `encodeURIComponent()`.

### CORS
API настроен для работы с фронтенд-приложениями. В случае проблем с CORS обратитесь к бэкенд-команде.

---

**Дата обновления:** 24 октября 2025
**Версия API:** v1
**Контакт:** Backend Team