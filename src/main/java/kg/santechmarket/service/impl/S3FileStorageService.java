package kg.santechmarket.service.impl;

import kg.santechmarket.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

/**
 * Реализация хранения файлов в AWS S3
 * <p>
 * Используется в продакшн окружении
 * <p>
 * Для использования необходимо:
 * 1. Добавить зависимость AWS SDK в pom.xml:
 * <dependency>
 * <groupId>software.amazon.awssdk</groupId>
 * <artifactId>s3</artifactId>
 * <version>2.20.0</version>
 * </dependency>
 * <p>
 * 2. Установить переменные окружения:
 * AWS_ACCESS_KEY
 * AWS_SECRET_KEY
 * AWS_CLOUDFRONT_DOMAIN (опционально)
 * <p>
 * 3. В application.yml установить:
 * file-storage.type: s3
 */
@Service
@ConditionalOnProperty(name = "file-storage.type", havingValue = "s3")
@Slf4j
public class S3FileStorageService implements FileStorageService {

    @Value("${file-storage.s3.bucket-name}")
    private String bucketName;

    @Value("${file-storage.s3.region}")
    private String region;

    @Value("${file-storage.s3.access-key}")
    private String accessKey;

    @Value("${file-storage.s3.secret-key}")
    private String secretKey;

    @Value("${file-storage.s3.cloudfront-domain:}")
    private String cloudfrontDomain;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        // Инициализация S3 клиента
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
        log.info("S3 клиент инициализирован для bucket: {}", bucketName);
    }

    @Override
    public String store(MultipartFile file, String category) {
        // Валидация
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Невозможно сохранить пустой файл");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Имя файла содержит недопустимую последовательность: " + originalFilename);
        }

        // Генерация уникального имени
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + extension;
        String s3Key = category + "/" + newFilename;

        try {
            // Загрузка в S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Файл загружен в S3: {}", s3Key);

            // Возврат URL
            if (cloudfrontDomain != null && !cloudfrontDomain.isEmpty()) {
                return cloudfrontDomain + "/" + s3Key;
            } else {
                return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
            }

        } catch (IOException e) {
            log.error("Ошибка чтения файла: {}", originalFilename, e);
            throw new RuntimeException("Не удалось прочитать файл: " + originalFilename, e);
        } catch (Exception e) {
            log.error("Ошибка загрузки файла в S3: {}", originalFilename, e);
            throw new RuntimeException("Не удалось загрузить файл в S3: " + originalFilename, e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        try {
            // Извлечение S3 key из URL
            String s3Key = extractS3Key(fileUrl);

            // Удаление из S3
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("Файл удалён из S3: {}", s3Key);

        } catch (Exception e) {
            log.error("Ошибка удаления файла из S3: {}", fileUrl, e);
            throw new RuntimeException("Не удалось удалить файл из S3: " + fileUrl, e);
        }
    }

    @Override
    public boolean exists(String fileUrl) {
        try {
            String s3Key = extractS3Key(fileUrl);

            // Проверка существования в S3
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            log.debug("Файл не найден в S3: {}", fileUrl);
            return false;
        } catch (Exception e) {
            log.error("Ошибка проверки существования файла в S3: {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public String getFullUrl(String relativePath) {
        if (cloudfrontDomain != null && !cloudfrontDomain.isEmpty()) {
            return cloudfrontDomain + relativePath;
        } else {
            return String.format("https://%s.s3.%s.amazonaws.com%s", bucketName, region, relativePath);
        }
    }

    /**
     * Извлечь S3 key из URL
     */
    private String extractS3Key(String fileUrl) {
        if (fileUrl.contains(cloudfrontDomain)) {
            return fileUrl.replace(cloudfrontDomain + "/", "");
        } else {
            String s3Url = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
            return fileUrl.replace(s3Url, "");
        }
    }

    /**
     * Получить расширение файла
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }

        return filename.substring(lastDotIndex);
    }
}
