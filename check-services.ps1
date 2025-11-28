# Проверка статуса всех сервисов

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Проверка статуса BoomBet сервисов" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Функция для проверки доступности сервиса
function Test-Service {
    param(
        [string]$Name,
        [string]$Url,
        [int]$Port
    )

    Write-Host "Проверка $Name (порт $Port)... " -NoNewline

    try {
        $response = Invoke-RestMethod -Uri $Url -TimeoutSec 5 -ErrorAction Stop
        Write-Host "✓ Работает" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "✗ Недоступен" -ForegroundColor Red
        return $false
    }
}

# Функция для проверки Docker контейнера
function Test-Container {
    param([string]$Name)

    Write-Host "Проверка контейнера $Name... " -NoNewline

    try {
        $status = docker ps --filter "name=$Name" --format "{{.Status}}" 2>$null
        if ($status -like "*Up*") {
            Write-Host "✓ Запущен" -ForegroundColor Green
            return $true
        } else {
            Write-Host "✗ Не запущен" -ForegroundColor Red
            return $false
        }
    }
    catch {
        Write-Host "✗ Ошибка проверки" -ForegroundColor Red
        return $false
    }
}

Write-Host "1. Проверка Docker контейнеров" -ForegroundColor Yellow
Write-Host "----------------------------" -ForegroundColor Gray
$postgresOk = Test-Container "postgres"
$kafkaOk = Test-Container "kafka"
$zookeeperOk = Test-Container "zookeeper"
$apiGatewayOk = Test-Container "api-gateway"
$authServiceOk = Test-Container "auth-service"
$coreServiceOk = Test-Container "core-service"
$realtimeServiceOk = Test-Container "realtime-service"

Write-Host ""
Write-Host "2. Проверка HTTP эндпоинтов" -ForegroundColor Yellow
Write-Host "----------------------------" -ForegroundColor Gray
$apiGatewayHttpOk = Test-Service "API Gateway" "http://localhost:8080/actuator/health" 8080
$authServiceHttpOk = Test-Service "Auth Service" "http://localhost:8081/actuator/health" 8081
$coreServiceHttpOk = Test-Service "Core Service" "http://localhost:8082/actuator/health" 8082
$realtimeServiceHttpOk = Test-Service "Realtime Service" "http://localhost:8083/actuator/health" 8083

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "              Результаты" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$allOk = $postgresOk -and $kafkaOk -and $zookeeperOk -and $apiGatewayHttpOk -and $authServiceHttpOk -and $coreServiceHttpOk

if ($allOk) {
    Write-Host "✓ Все сервисы работают корректно!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Можете приступать к тестированию User Profile API:" -ForegroundColor Cyan
    Write-Host "  .\test-user-profile-api.ps1" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Или смотрите руководство:" -ForegroundColor Cyan
    Write-Host "  TESTING_GUIDE.md" -ForegroundColor Yellow
} else {
    Write-Host "✗ Некоторые сервисы недоступны" -ForegroundColor Red
    Write-Host ""
    Write-Host "Попробуйте запустить сервисы:" -ForegroundColor Yellow
    Write-Host "  docker-compose up -d" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "И подождите 30-60 секунд для полной загрузки." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Для просмотра логов:" -ForegroundColor Cyan
Write-Host "  docker-compose logs -f core-service" -ForegroundColor Yellow
Write-Host ""

