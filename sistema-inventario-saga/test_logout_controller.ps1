# Script de Testing para LogoutController
# ==========================================

Write-Host "SCRIPT DE TESTING - LOGOUTCONTROLLER" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Variables
$baseUrl = "http://localhost:8080"
$adminUser = "admin"
$adminPass = "admin123"

Write-Host "URLs de logout a probar:" -ForegroundColor Yellow
Write-Host "   1. /logout/page - Pagina intermedia de logout"
Write-Host "   2. /logout/process - Logout directo POST"
Write-Host "   3. /logout/direct - Logout directo GET"
Write-Host "   4. /logout/force - Logout forzado"
Write-Host ""

Write-Host "Credenciales de prueba:" -ForegroundColor Yellow
Write-Host "   Usuario: $adminUser"
Write-Host "   Password: $adminPass"
Write-Host ""

Write-Host "Abriendo navegador para testing manual..." -ForegroundColor Green
Start-Process "http://localhost:8080/auth/login"

Write-Host ""
Write-Host "PASOS DE TESTING MANUAL:" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta
Write-Host ""
Write-Host "1. Abrir http://localhost:8080/auth/login"
Write-Host "2. Iniciar sesion con admin/admin123"
Write-Host "3. Navegar a /admin/dashboard"
Write-Host "4. Hacer clic en el menu del usuario (esquina superior derecha)"
Write-Host "5. Hacer clic en Cerrar Sesion (principal, color rojo)"
Write-Host "6. Verificar que aparece la pagina intermedia de logout"
Write-Host "7. Verificar que se redirige a /auth/login?logout=true"
Write-Host "8. Verificar que la sesion esta cerrada (intentar acceder a /admin/dashboard)"
Write-Host ""

Write-Host "OPCIONES DE FALLBACK (despues de 5 segundos):" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "Si el logout principal no funciona, apareceran opciones adicionales:"
Write-Host "   • Logout Directo (POST) - boton amarillo"
Write-Host "   • Logout Directo (GET) - enlace azul"
Write-Host "   • Logout Forzado - enlace gris"
Write-Host ""

Write-Host "TESTING DE URLs DIRECTAS:" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$testUrls = @(
    "/logout/page",
    "/logout/direct",
    "/logout/force"
)

foreach ($url in $testUrls) {
    $fullUrl = "$baseUrl$url"
    Write-Host "URL: $fullUrl" -ForegroundColor Blue
    try {
        $response = Invoke-WebRequest -Uri $fullUrl -Method GET -ErrorAction SilentlyContinue
        Write-Host "   OK: Respuesta $($response.StatusCode)" -ForegroundColor Green
    } catch {
        Write-Host "   ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "VERIFICACIONES IMPORTANTES:" -ForegroundColor Red
Write-Host "============================================" -ForegroundColor Red
Write-Host ""
Write-Host "OK: El logout debe cerrar la sesion completamente"
Write-Host "OK: Debe redirigir a /auth/login?logout=true"
Write-Host "OK: No debe quedarse en la misma pagina con # en la URL"
Write-Host "OK: Debe funcionar desde todas las vistas (admin, cliente, empleado)"
Write-Host "OK: Debe funcionar desde admin/portal.html especificamente"
Write-Host ""

Write-Host "Para testing desde admin/portal.html:" -ForegroundColor Magenta
Write-Host "   1. Ir a: $baseUrl/admin/portal"
Write-Host "   2. Usar el logout desde ese template especificamente"
Write-Host "   3. Verificar que funciona igual que desde dashboard"
Write-Host ""

Write-Host "DEBUGGING:" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "Si hay problemas, revisar en la consola del navegador:"
Write-Host "   • Token CSRF disponible"
Write-Host "   • Errores JavaScript"
Write-Host "   • Requests HTTP en Network tab"
Write-Host ""

Write-Host "NOTA: Despues de 5 segundos de cargar la pagina," -ForegroundColor Cyan
Write-Host "   apareceran opciones adicionales de logout para debugging" -ForegroundColor Cyan
Write-Host ""

Write-Host "Press any key to continue testing..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
