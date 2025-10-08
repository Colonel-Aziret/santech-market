# 📁 Система загрузки файлов (изображений)

## 🎯 Обзор

Гибридная система хранения изображений с поддержкой:
- **Локального хранилища** (для разработки)
- **AWS S3** (для продакшна)

## 🚀 Быстрый старт (Локальное хранилище)

### 1. Конфигурация уже настроена

По умолчанию используется локальное хранилище:

```yaml
file-storage:
  type: local  # local или s3
  local:
    upload-dir: ./uploads
    base-url: http://localhost:8080/api/v1
```

### 2. API для загрузки

#### Загрузить изображение
```bash
POST /api/v1/upload/image
Content-Type: multipart/form-data
Authorization: Bearer {JWT_TOKEN}

Параметры:
- file: файл изображения (обязательно)
- category: products | categories | banners (обязательно)
```

**Пример с curl:**
```bash
curl -X POST http://localhost:8080/api/v1/upload/image \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg" \
  -F "category=products"
```

**Ответ:**
```json
{
  "relativePath": "/images/products/uuid-generated-name.jpg",
  "fullUrl": "http://localhost:8080/api/v1/images/products/uuid-generated-name.jpg"
}
```

#### Удалить изображение
```bash
DELETE /api/v1/upload/image?fileUrl=/images/products/uuid.jpg
Authorization: Bearer {JWT_TOKEN}
```

### 3. Доступ к изображениям

После загрузки изображения доступны по URL:
```
http://localhost:8080/api/v1/images/{category}/{filename}
```

Пример:
```
http://localhost:8080/api/v1/images/products/abc123-uuid.jpg
```

## 📋 Ограничения

- **Максимальный размер:** 5 MB
- **Разрешённые форматы:** JPEG, JPG, PNG, GIF, WebP
- **Категории:** products, categories, banners
- **Доступ:** только ADMIN и MANAGER

## 🔐 Безопасность

1. **Аутентификация:** требуется JWT токен
2. **Авторизация:** только роли ADMIN/MANAGER
3. **Валидация:**
   - Проверка типа файла
   - Проверка размера
   - Защита от path traversal
4. **Уникальные имена:** UUID для каждого файла

## 📂 Структура директорий

```
./uploads/
├── products/       # Изображения товаров
├── categories/     # Изображения категорий
└── banners/        # Промо-баннеры
```

## 🌐 Переход на AWS S3 (Production)

### 1. Добавить AWS SDK в pom.xml

```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.20.0</version>
</dependency>
```

### 2. Настроить переменные окружения

```bash
export AWS_ACCESS_KEY=your_access_key
export AWS_SECRET_KEY=your_secret_key
export AWS_CLOUDFRONT_DOMAIN=https://d123456.cloudfront.net  # опционально
```

### 3. Изменить конфигурацию

В `application.yml` или через переменные окружения:

```yaml
file-storage:
  type: s3  # Изменить на s3
  s3:
    bucket-name: santech-market-images
    region: us-east-1
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}
    cloudfront-domain: ${AWS_CLOUDFRONT_DOMAIN:}
```

### 4. Раскомментировать код в S3FileStorageService

Откройте `S3FileStorageService.java` и раскомментируйте TODO блоки.

## 📊 Использование в коде

### Обновление товара с изображением

1. **Загрузить изображение:**
```bash
POST /upload/image
Response: { "relativePath": "/images/products/abc.jpg" }
```

2. **Обновить товар:**
```bash
PUT /products/{id}
{
  "name": "Товар",
  "imageUrl": "/images/products/abc.jpg"  # Использовать relativePath
}
```

### Удаление старого изображения при обновлении

```java
// В ProductService
if (existingProduct.getImageUrl() != null) {
    fileStorageService.delete(existingProduct.getImageUrl());
}
existingProduct.setImageUrl(newImageUrl);
```

## 🧪 Тестирование

### Swagger UI

Перейдите на: `http://localhost:8080/api/v1/swagger-ui.html`

Найдите раздел "File Upload" и протестируйте:
1. Авторизуйтесь через `/auth/login`
2. Скопируйте JWT токен
3. Нажмите "Authorize" и вставьте токен
4. Попробуйте загрузить изображение

### Postman

```
POST http://localhost:8080/api/v1/upload/image
Headers:
  Authorization: Bearer YOUR_JWT_TOKEN
Body (form-data):
  file: [выбрать файл]
  category: products
```

## 🔄 Миграция с локального на S3

```bash
# 1. Создать S3 bucket
aws s3 mb s3://santech-market-images

# 2. Настроить CORS для bucket
aws s3api put-bucket-cors --bucket santech-market-images --cors-configuration file://cors.json

# 3. Загрузить существующие файлы
aws s3 sync ./uploads s3://santech-market-images

# 4. Изменить file-storage.type на s3 в application.yml

# 5. Перезапустить приложение
```

## 📝 Примеры интеграции

### React Native (фронтенд)

```javascript
// Загрузка изображения
const uploadImage = async (imageUri) => {
  const formData = new FormData();
  formData.append('file', {
    uri: imageUri,
    type: 'image/jpeg',
    name: 'photo.jpg',
  });
  formData.append('category', 'products');

  const response = await fetch('http://localhost:8080/api/v1/upload/image', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${jwtToken}`,
    },
    body: formData,
  });

  const data = await response.json();
  return data.fullUrl; // Использовать для отображения
};
```

## ⚡ Performance Tips

1. **Используйте CDN** (CloudFront с S3)
2. **Оптимизируйте изображения** перед загрузкой
3. **Используйте WebP** формат где возможно
4. **Настройте кэширование** в Nginx/CloudFront

## 🐛 Troubleshooting

### Ошибка "Директория не найдена"
```bash
mkdir -p ./uploads/products ./uploads/categories ./uploads/banners
```

### Ошибка "Permission denied"
```bash
chmod -R 755 ./uploads
```

### Изображения не загружаются через URL
Проверьте:
1. Директория `./uploads` существует
2. WebConfig правильно настроен
3. CORS разрешён для вашего фронтенда

## 📚 Дополнительные ресурсы

- [AWS S3 Documentation](https://docs.aws.amazon.com/s3/)
- [Spring Multipart File Upload](https://spring.io/guides/gs/uploading-files/)
- [CloudFront CDN Setup](https://docs.aws.amazon.com/cloudfront/)
