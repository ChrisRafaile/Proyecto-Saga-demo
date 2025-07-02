# Script simple para verificar la redirección del logo

Write-Host "=== VERIFICACIÓN RÁPIDA DEL LOGO ===" -ForegroundColor Green
Write-Host ""

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/admin/usuarios" -UseBasicParsing
    
    if ($response.Content -match 'href="/admin/dashboard".*Sistema de Inventario') {
        Write-Host "✅ CORRECTO: El logo redirige a /admin/dashboard" -ForegroundColor Green
    } elseif ($response.Content -match 'href="/dashboard".*Sistema de Inventario') {
        Write-Host "❌ ERROR: El logo aún redirige a /dashboard" -ForegroundColor Red
        Write-Host "   Necesita limpiar caché del navegador (Ctrl+F5)" -ForegroundColor Yellow
    } else {
        Write-Host "⚠️  No se pudo determinar la redirección del logo" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "Para verificar manualmente:" -ForegroundColor Cyan
    Write-Host "1. Ir a: http://localhost:8080/admin/usuarios" -ForegroundColor White
    Write-Host "2. Presionar Ctrl+F5 para refrescar sin caché" -ForegroundColor White
    Write-Host "3. Hacer clic en 'Sistema de Inventario' o el logo" -ForegroundColor White
    Write-Host "4. Debería redirigir a: http://localhost:8080/admin/dashboard" -ForegroundColor White
    
} catch {
    Write-Host "❌ Error: No se pudo conectar al servidor" -ForegroundColor Red
}

Write-Host ""
