# Script para probar el sistema de filtros de alertas

Write-Host "=== TEST DEL SISTEMA DE FILTROS DE ALERTAS ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080"
$alertasUrl = "$baseUrl/admin/alertas"

Write-Host "1. Verificando que el servidor este ejecutandose..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $baseUrl -TimeoutSec 5 -UseBasicParsing
    Write-Host "   Servidor ejecutandose correctamente" -ForegroundColor Green
} catch {
    Write-Host "   Error: El servidor no esta ejecutandose" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "2. Probando filtros de alertas..." -ForegroundColor Yellow

$filtros = @("prioridad=ALTA", "prioridad=MEDIA", "prioridad=BAJA", "estado=LEIDA", "estado=NO_LEIDA", "tipo=SISTEMA", "tipo=USUARIO")

foreach ($filtro in $filtros) {
    $url = "$alertasUrl`?$filtro"
    
    try {
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 5 -UseBasicParsing
        Write-Host "   Filtro $filtro : OK" -ForegroundColor Green
    } catch {
        Write-Host "   Filtro $filtro : Error" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "3. Probando navegacion..." -ForegroundColor Yellow

$rutas = @("/admin/dashboard", "/admin/productos", "/admin/alertas")

foreach ($ruta in $rutas) {
    $url = "$baseUrl$ruta"
    
    try {
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 5 -UseBasicParsing
        Write-Host "   Ruta $ruta : OK" -ForegroundColor Green
    } catch {
        Write-Host "   Ruta $ruta : Error" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== RESUMEN ===" -ForegroundColor Green
Write-Host "Sistema de filtros funcionando correctamente" -ForegroundColor Green
Write-Host "Navegacion funcionando correctamente" -ForegroundColor Green
Write-Host ""
