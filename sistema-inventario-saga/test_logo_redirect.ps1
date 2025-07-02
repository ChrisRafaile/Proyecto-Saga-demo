# Script para verificar la redirección del logo en todas las páginas de admin

Write-Host "=== VERIFICACIÓN DE REDIRECCIÓN DEL LOGO ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080"

# Páginas de admin para probar
$paginasAdmin = @(
    "/admin/dashboard",
    "/admin/usuarios", 
    "/admin/productos",
    "/admin/alertas",
    "/admin/proveedores",
    "/admin/pedidos",
    "/admin/reportes",
    "/admin/actividad",
    "/admin/configuracion",
    "/admin/respaldos"
)

Write-Host "Verificando que el servidor esté ejecutándose..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $baseUrl -TimeoutSec 5 -UseBasicParsing
    Write-Host "✓ Servidor ejecutándose correctamente" -ForegroundColor Green
} catch {
    Write-Host "✗ Error: El servidor no está ejecutándose" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Probando acceso a páginas de administrador..." -ForegroundColor Yellow

foreach ($pagina in $paginasAdmin) {
    $url = "$baseUrl$pagina"
    
    try {
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 10 -UseBasicParsing
        
        if ($response.StatusCode -eq 200) {
            # Verificar si contiene el navbar dinámico
            if ($response.Content -match "hasAnyRole.*ADMIN.*admin/dashboard") {
                Write-Host "   ✓ $pagina : Navbar dinámico correcto" -ForegroundColor Green
            } elseif ($response.Content -match "admin/dashboard") {
                Write-Host "   ⚠ $pagina : Contiene admin/dashboard pero puede ser estático" -ForegroundColor Yellow
            } else {
                Write-Host "   ✗ $pagina : No contiene referencia a admin/dashboard" -ForegroundColor Red
            }
        } else {
            Write-Host "   ⚠ $pagina : Código $($response.StatusCode)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "   ✗ $pagina : No accesible" -ForegroundColor Red
    }
    
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "=== INSTRUCCIONES PARA VERIFICACIÓN MANUAL ===" -ForegroundColor Cyan
Write-Host "1. Abra http://localhost:8080/auth/login" -ForegroundColor White
Write-Host "2. Inicie sesión como administrador" -ForegroundColor White  
Write-Host "3. Navegue a http://localhost:8080/admin/usuarios" -ForegroundColor White
Write-Host "4. Haga clic en el logo 'Saga Falabella' o 'Sistema de Inventario'" -ForegroundColor White
Write-Host "5. Debería redirigir a http://localhost:8080/admin/dashboard" -ForegroundColor White
Write-Host ""
Write-Host "Si sigue redirigiendo a /dashboard (sin /admin), puede ser:" -ForegroundColor Yellow
Write-Host "- Caché del navegador (Ctrl+F5 para refrescar)" -ForegroundColor Yellow
Write-Host "- Archivo compilado no actualizado" -ForegroundColor Yellow
Write-Host ""
