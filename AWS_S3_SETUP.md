# AWS S3 Setup Guide

## 📋 Что нужно сделать

### 1. Создать AWS аккаунт
1. Зайди на https://aws.amazon.com
2. Создай аккаунт (если нет)
3. Войди в AWS Console

### 2. Создать S3 Bucket

1. **Зайди в S3 Console:**
   - https://s3.console.aws.amazon.com/s3/

2. **Создай новый bucket:**
   - Нажми "Create bucket"
   - **Bucket name**: `santech-market-images` (или любое уникальное имя)
   - **Region**: выбери ближайший регион (например, `eu-central-1` для Европы)
   - **Object Ownership**: выбери "ACLs enabled"
   - **Block Public Access**: СНИМИ галочки (чтобы файлы были публичными)
   - Подтверди, что понимаешь риски публичного доступа
   - Нажми "Create bucket"

3. **Настрой публичный доступ:**
   - Открой созданный bucket
   - Перейди в "Permissions"
   - В разделе "Bucket policy" добавь:
   ```json
   {
       "Version": "2012-10-17",
       "Statement": [
           {
               "Sid": "PublicReadGetObject",
               "Effect": "Allow",
               "Principal": "*",
               "Action": "s3:GetObject",
               "Resource": "arn:aws:s3:::santech-market-images/*"
           }
       ]
   }
   ```
   ⚠️ Замени `santech-market-images` на имя твоего bucket!

### 3. Создать IAM пользователя для доступа

1. **Зайди в IAM Console:**
   - https://console.aws.amazon.com/iam/

2. **Создай пользователя:**
   - Перейди в "Users" → "Create user"
   - **User name**: `santech-market-s3-user`
   - **Access type**: Programmatic access
   - Нажми "Next"

3. **Настрой права доступа:**
   - Выбери "Attach policies directly"
   - Найди и выбери `AmazonS3FullAccess` (или создай кастомную политику)
   - Нажми "Next" → "Create user"

4. **Сохрани credentials:**
   - ⚠️ **ВАЖНО!** Скопируй и сохрани:
     - **Access Key ID** (например: `AKIAIOSFODNN7EXAMPLE`)
     - **Secret Access Key** (например: `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY`)
   - Эти ключи нужно будет добавить в конфиг

### 4. Настроить application.yml

В файле `src/main/resources/application.yml` измени:

```yaml
file-storage:
  # Измени на s3 для использования AWS S3
  type: s3

  # AWS S3 configuration
  s3:
    bucket-name: santech-market-images  # ИМЯ ТВОЕГО BUCKET
    region: eu-central-1                # РЕГИОН ТВОЕГО BUCKET
    access-key: ${AWS_ACCESS_KEY:}      # ACCESS KEY (через переменные окружения)
    secret-key: ${AWS_SECRET_KEY:}      # SECRET KEY (через переменные окружения)
    cloudfront-domain: ${AWS_CLOUDFRONT_DOMAIN:}  # Опционально
```

### 5. Установить переменные окружения

#### Windows (PowerShell):
```powershell
$env:AWS_ACCESS_KEY="ТВОЙ_ACCESS_KEY"
$env:AWS_SECRET_KEY="ТВОЙ_SECRET_KEY"
```

#### Windows (CMD):
```cmd
set AWS_ACCESS_KEY=ТВОЙ_ACCESS_KEY
set AWS_SECRET_KEY=ТВОЙ_SECRET_KEY
```

#### Linux/Mac:
```bash
export AWS_ACCESS_KEY="ТВОЙ_ACCESS_KEY"
export AWS_SECRET_KEY="ТВОЙ_SECRET_KEY"
```

#### Или добавь в IntelliJ IDEA:
1. Run → Edit Configurations
2. Найди свою Spring Boot конфигурацию
3. В "Environment variables" добавь:
   ```
   AWS_ACCESS_KEY=ТВОЙ_ACCESS_KEY;AWS_SECRET_KEY=ТВОЙ_SECRET_KEY
   ```

### 6. Запустить приложение

```bash
./mvnw spring-boot:run
```

В логах должно появиться:
```
S3 клиент инициализирован для bucket: santech-market-images
```

### 7. Протестировать загрузку

```bash
curl -X POST http://localhost:8080/api/v1/upload/image \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@test-image.jpg" \
  -F "category=products"
```

Ответ:
```json
{
  "relativePath": "products/uuid-filename.jpg",
  "fullUrl": "https://santech-market-images.s3.eu-central-1.amazonaws.com/products/uuid-filename.jpg"
}
```

---

## 💰 Стоимость

AWS S3 тарифы (примерно):
- **Хранение**: ~$0.023 за GB/месяц
- **Передача данных**: первые 100 GB бесплатно, потом ~$0.09 за GB
- **Запросы**: ~$0.0004 за 1000 GET запросов

**Для небольшого проекта**: ~$1-5 в месяц

---

## 🚀 Опционально: CloudFront CDN

Для ускорения загрузки изображений можно использовать CloudFront:

1. Создай CloudFront distribution с S3 bucket как origin
2. Получи CloudFront domain (например: `d111111abcdef8.cloudfront.net`)
3. Добавь в `application.yml`:
   ```yaml
   s3:
     cloudfront-domain: https://d111111abcdef8.cloudfront.net
   ```

---

## 🔄 Переключение между Local и S3

### Для разработки (локальное хранилище):
```yaml
file-storage:
  type: local
```

### Для продакшна (AWS S3):
```yaml
file-storage:
  type: s3
```

Приложение автоматически выберет нужный сервис!

---

## ⚠️ Важно для безопасности

1. **НЕ коммить** access keys в git!
2. **Используй** переменные окружения
3. **Настрой** AWS IAM права (минимальные необходимые)
4. **Включи** CloudTrail для аудита
5. **Настрой** S3 Lifecycle для автоудаления старых файлов

---

## 🆘 Проблемы?

### Ошибка: Access Denied
- Проверь bucket policy (должен быть публичным)
- Проверь IAM права пользователя

### Ошибка: InvalidAccessKeyId
- Проверь правильность ACCESS_KEY
- Убедись, что переменные окружения установлены

### Ошибка: NoSuchBucket
- Проверь имя bucket в конфиге
- Убедись, что bucket создан в правильном регионе
