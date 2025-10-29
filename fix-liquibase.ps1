# PowerShell скрипт для очистки неудачной миграции Liquibase
# Использование: .\fix-liquibase.ps1

$env:PGPASSWORD = "12345"  # Пароль из application.yml

& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d santech_market -c "DELETE FROM databasechangelog WHERE id = '26-restore-promo-banner-columns';"

Write-Host "Неудачная миграция удалена. Теперь можно запустить приложение."
