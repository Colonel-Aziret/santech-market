-- Удаление неудачной миграции из таблицы Liquibase
DELETE FROM databasechangelog
WHERE id = '26-restore-promo-banner-columns';

-- Проверка, что запись удалена
SELECT * FROM databasechangelog WHERE filename LIKE '%26-restore%';
