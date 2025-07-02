# Script de Prueba de Logout - Diagnóstico Completo
# Autor: Christopher Lincoln Rafaile Naupay

Write-Host "=== DIAGNÓSTICO COMPLETO DEL LOGOUT ===" -ForegroundColor Green
Write-Host

$baseUrl = "http://localhost:8080"
$adminDashboard = "$baseUrl/admin/dashboard"
$loginUrl = "$baseUrl/auth/login"
$logoutUrl = "$baseUrl/auth/logout"

Write-Host "🔍 PASO 1: Verificando servidor..." -ForegroundColor Yellow
try {
    $serverCheck = Invoke-WebRequest -Uri $baseUrl -Method GET -UseBasicParsing -TimeoutSec 5
    Write-Host "✅ Servidor activo" -ForegroundColor Green
} catch {
    Write-Host "❌ Servidor no responde - ejecuta 'mvn spring-boot:run'" -ForegroundColor Red
    exit 1
}

Write-Host
Write-Host "🎯 PROBLEMA IDENTIFICADO:" -ForegroundColor Red
Write-Host "  - Modal se abre pero logout no funciona" -ForegroundColor Yellow
Write-Host "  - URL se queda en /admin/dashboard#" -ForegroundColor Yellow
Write-Host "  - JavaScript no envía formulario correctamente" -ForegroundColor Yellow

Write-Host
Write-Host "🔧 SOLUCIONES IMPLEMENTADAS:" -ForegroundColor Cyan
Write-Host "  1. Cambiado href='#' por href='javascript:void(0)'" -ForegroundColor White
Write-Host "  2. Añadido 'return false' para prevenir navegación" -ForegroundColor White
Write-Host "  3. Múltiples métodos de logout como fallback" -ForegroundColor White
Write-Host "  4. Botones de logout directo en el modal" -ForegroundColor White
Write-Host "  5. Logging detallado para debugging" -ForegroundColor White

Write-Host
Write-Host "🧪 PASOS DE PRUEBA INMEDIATA:" -ForegroundColor Magenta
Write-Host
Write-Host "1. 🌐 Abrir navegador:" -ForegroundColor White
Write-Host "   $loginUrl" -ForegroundColor Gray
Write-Host

Write-Host "2. 🔐 Login:" -ForegroundColor White
Write-Host "   Usuario: admin" -ForegroundColor Gray
Write-Host "   Contraseña: admin123" -ForegroundColor Gray
Write-Host

Write-Host "3. 🎛️ Abrir Developer Tools:" -ForegroundColor White
Write-Host "   Presiona F12 → pestaña Console" -ForegroundColor Gray
Write-Host "   Busca mensajes que empiecen con '=== DEBUG LOGOUT ==='" -ForegroundColor Gray
Write-Host

Write-Host "4. 👤 Hacer clic en dropdown del usuario" -ForegroundColor White
Write-Host

Write-Host "5. 🚪 Hacer clic en 'Cerrar Sesión'" -ForegroundColor White
Write-Host "   👀 OBSERVAR en consola:" -ForegroundColor Yellow
Write-Host "   - 'Mostrando modal de logout...'" -ForegroundColor Gray
Write-Host "   - '=== DEBUG LOGOUT ===' al cargar página" -ForegroundColor Gray
Write-Host

Write-Host "6. ⚙️ En el modal que aparece:" -ForegroundColor White
Write-Host "   OPCIÓN A: Esperar 5 segundos (logout automático)" -ForegroundColor Gray
Write-Host "   OPCIÓN B: Clic en 'Cerrar Sesión Ahora'" -ForegroundColor Gray
Write-Host "   OPCIÓN C: Clic en 'Logout Directo' (abajo del modal)" -ForegroundColor Gray
Write-Host "   OPCIÓN D: Clic en 'Logout Simple' (sin JavaScript)" -ForegroundColor Gray
Write-Host

Write-Host "7. 📊 VERIFICAR RESULTADO:" -ForegroundColor White
Write-Host "   ✅ ÉXITO: Redirigido a $loginUrl con mensaje de logout" -ForegroundColor Green
Write-Host "   ❌ FALLO: Se queda en $adminDashboard#" -ForegroundColor Red
Write-Host

Write-Host "🔍 QUÉ BUSCAR EN LA CONSOLA:" -ForegroundColor Yellow
Write-Host
Write-Host "MENSAJES ESPERADOS AL CARGAR PÁGINA:" -ForegroundColor White
Write-Host "=== DEBUG LOGOUT ===" -ForegroundColor Gray
Write-Host "Formulario principal encontrado: true" -ForegroundColor Gray
Write-Host "Formulario directo encontrado: true" -ForegroundColor Gray
Write-Host "Token CSRF encontrado: true" -ForegroundColor Gray
Write-Host
Write-Host "MENSAJES ESPERADOS AL HACER LOGOUT:" -ForegroundColor White
Write-Host "Mostrando modal de logout..." -ForegroundColor Gray
Write-Host "Iniciando proceso de logout..." -ForegroundColor Gray
Write-Host "Método 1: Enviando formulario principal..." -ForegroundColor Gray
Write-Host

Write-Host "🆘 SI NADA FUNCIONA:" -ForegroundColor Red
Write-Host
Write-Host "LOGOUT MANUAL DIRECTO:" -ForegroundColor White
Write-Host "1. Ve directamente a: $logoutUrl" -ForegroundColor Yellow
Write-Host "2. O usa estos enlaces directos:" -ForegroundColor White
Write-Host "   - Logout GET: $baseUrl/auth/logout" -ForegroundColor Gray
Write-Host

Write-Host "📞 REPORTAR PROBLEMA:" -ForegroundColor Cyan
Write-Host "Si ningún método funciona, reporta:" -ForegroundColor White
Write-Host "1. Mensajes de la consola (todos)" -ForegroundColor Gray
Write-Host "2. URL donde se queda" -ForegroundColor Gray
Write-Host "3. Qué botones probaste" -ForegroundColor Gray
Write-Host "4. Errores en la pestaña Network (F12)" -ForegroundColor Gray
Write-Host

Write-Host "🚀 ¡COMENZAR PRUEBA AHORA!" -ForegroundColor Green -BackgroundColor Black
Write-Host
