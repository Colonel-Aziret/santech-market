# Test JWT Authentication

# Login and get token
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body '{"username":"admin","password":"admin123"}'

$token = $loginResponse.token
Write-Host "Token received: $($token.Substring(0,50))..."

# Test profile endpoint
$headers = @{
    "Authorization" = "Bearer $token"
}

$profile = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/profile" `
    -Method Get `
    -Headers $headers

Write-Host "`nProfile:"
$profile | ConvertTo-Json

# Test cart endpoint
$cart = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/cart" `
    -Method Get `
    -Headers $headers

Write-Host "`nCart:"
$cart | ConvertTo-Json

Write-Host "`n✅ JWT Authentication works!"
