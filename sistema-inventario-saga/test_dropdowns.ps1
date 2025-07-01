# Script para probar los dropdowns del navbar
# Asumiendo que la aplicación está funcionando en localhost:8080

$baseUrl = "http://localhost:8080"
$loginUrl = "$baseUrl/auth/login"
$adminDashboardUrl = "$baseUrl/admin/dashboard"

Write-Host "=== Test de Dropdowns del Navbar ===" -ForegroundColor Green
Write-Host ""

# Lista de rutas de admin que deberían estar accesibles desde los dropdowns
$adminRoutes = @(
    "/admin/dashboard",
    "/admin/usuarios", 
    "/admin/productos",
    "/admin/proveedores",
    "/admin/pedidos",
    "/admin/reportes",
    "/admin/alertas",
    "/admin/actividad", 
    "/admin/configuracion",
    "/admin/respaldos"
)

Write-Host "Rutas de administración que deberían ser accesibles desde los dropdowns:" -ForegroundColor Yellow
foreach ($route in $adminRoutes) {
    Write-Host "  - $baseUrl$route" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Para probar:" -ForegroundColor Yellow
Write-Host "1. Inicie la aplicación Spring Boot" -ForegroundColor White
Write-Host "2. Vaya a: $loginUrl" -ForegroundColor White
Write-Host "3. Inicie sesión con usuario admin (username: admin, password: admin123)" -ForegroundColor White
Write-Host "4. Verifique que los dropdowns del navbar funcionen:" -ForegroundColor White
Write-Host "   - Dropdown 'Administración' debe mostrar todas las opciones" -ForegroundColor Gray
Write-Host "   - Dropdown de notificaciones (campana) debe abrir un menú" -ForegroundColor Gray  
Write-Host "   - Dropdown del usuario 'admin' debe mostrar perfil y logout" -ForegroundColor Gray
Write-Host ""

Write-Host "Mejoras implementadas:" -ForegroundColor Green
Write-Host "✓ Bootstrap JavaScript agregado al head.html" -ForegroundColor Green
Write-Host "✓ IDs únicos agregados a todos los dropdowns para mejor accesibilidad" -ForegroundColor Green
Write-Host "✓ Inicialización automática de componentes Bootstrap en saga-system.js" -ForegroundColor Green
Write-Host "✓ Estilos CSS mejorados para dropdowns con animaciones" -ForegroundColor Green
Write-Host "✓ Enlaces del dropdown de notificaciones actualizados a rutas admin" -ForegroundColor Green
Write-Host "✓ Enlaces del dropdown de usuario actualizados con rutas válidas" -ForegroundColor Green

Write-Host ""
Write-Host "Nota: Si aún no funcionan los dropdowns, verifique:" -ForegroundColor Red
Write-Host "- Que Bootstrap JavaScript esté cargándose correctamente" -ForegroundColor Red
Write-Host "- Que no haya errores en la consola del navegador" -ForegroundColor Red
Write-Host "- Que los archivos CSS y JS estén siendo servidos correctamente" -ForegroundColor Red
