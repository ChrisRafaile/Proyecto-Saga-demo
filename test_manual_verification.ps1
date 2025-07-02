# Script para verificar manual login y navegacion
Write-Host "Verificando estado del sistema..." -ForegroundColor Green

$baseUrl = "http://localhost:8080"

# Verificar que el sistema responde
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/" -UseBasicParsing
    Write-Host "Sistema funcionando - Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "Error: Sistema no disponible" -ForegroundColor Red
    exit 1
}

Write-Host "`nPuede abrir manualmente en navegador:" -ForegroundColor Yellow
Write-Host "1. $baseUrl/login" -ForegroundColor Cyan
Write-Host "2. Login con: admin / admin123" -ForegroundColor Cyan  
Write-Host "3. Verificar que en /admin/dashboard el logo apunta a /admin/dashboard" -ForegroundColor Cyan
Write-Host "4. Ir a /admin/usuarios y verificar que el logo sigue apuntando a /admin/dashboard" -ForegroundColor Cyan

Write-Host "`nInstrucciones de verificacion manual:" -ForegroundColor Yellow
Write-Host "- Click derecho en el logo -> Inspeccionar elemento" -ForegroundColor White
Write-Host "- Verificar que href='/admin/dashboard' en todas las paginas admin" -ForegroundColor White
Write-Host "- Probar click en logo desde diferentes paginas admin" -ForegroundColor White
