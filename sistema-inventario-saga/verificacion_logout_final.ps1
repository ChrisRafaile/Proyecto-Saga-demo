# Script de Verificación Final - Logout Sistema de Inventario
# Autor: Christopher Lincoln Rafaile Naupay

Write-Host "=== VERIFICACIÓN FINAL DEL LOGOUT ===" -ForegroundColor Green
Write-Host

$baseUrl = "http://localhost:8080"

# Verificar servidor
Write-Host "✓ Verificando servidor..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method GET -UseBasicParsing -TimeoutSec 5
    Write-Host "  ✅ Servidor activo en $baseUrl" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Servidor no responde" -ForegroundColor Red
    Write-Host "  💡 Ejecuta: mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

Write-Host
Write-Host "=== PASOS DE VERIFICACIÓN MANUAL ===" -ForegroundColor Cyan
Write-Host
Write-Host "1. 🌐 ABRIR NAVEGADOR:" -ForegroundColor White
Write-Host "   → $baseUrl/auth/login" -ForegroundColor Gray
Write-Host

Write-Host "2. 🔐 HACER LOGIN:" -ForegroundColor White
Write-Host "   → Usuario: admin" -ForegroundColor Gray
Write-Host "   → Contraseña: admin123" -ForegroundColor Gray
Write-Host

Write-Host "3. 📊 IR AL DASHBOARD:" -ForegroundColor White
Write-Host "   → Deberías ver el Dashboard Administrador" -ForegroundColor Gray
Write-Host

Write-Host "4. 👤 ABRIR DROPDOWN USUARIO:" -ForegroundColor White
Write-Host "   → Hacer clic en el icono de usuario (esquina superior derecha)" -ForegroundColor Gray
Write-Host

Write-Host "5. 🚪 HACER CLIC EN 'CERRAR SESIÓN':" -ForegroundColor White
Write-Host "   → Se debe abrir un modal con countdown de 5 segundos" -ForegroundColor Gray
Write-Host

Write-Host "6. ✅ CONFIRMAR LOGOUT:" -ForegroundColor White
Write-Host "   → Hacer clic en 'Cerrar Sesión Ahora'" -ForegroundColor Gray
Write-Host "   → O esperar 5 segundos para logout automático" -ForegroundColor Gray
Write-Host

Write-Host "7. 🔄 VERIFICAR RESULTADO:" -ForegroundColor White
Write-Host "   → Deberías ver mensaje 'Cerrando sesión...'" -ForegroundColor Gray
Write-Host "   → Redirección automática a login" -ForegroundColor Gray
Write-Host "   → Mensaje 'Has cerrado sesión exitosamente'" -ForegroundColor Gray
Write-Host

Write-Host "=== QUÉ BUSCAR (DEBUGGING) ===" -ForegroundColor Magenta
Write-Host
Write-Host "🔧 Abrir Developer Tools (F12) → Console" -ForegroundColor White
Write-Host "   📝 Mensajes esperados:"
Write-Host "      - 'Formulario de logout encontrado: true'" -ForegroundColor Gray
Write-Host "      - 'Token CSRF encontrado: true'" -ForegroundColor Gray
Write-Host "      - 'Mostrando modal de logout...'" -ForegroundColor Gray
Write-Host "      - 'Iniciando proceso de logout...'" -ForegroundColor Gray
Write-Host "      - 'Enviando formulario de logout...'" -ForegroundColor Gray
Write-Host

Write-Host "=== OPCIONES DE FALLBACK ===" -ForegroundColor Yellow
Write-Host
Write-Host "🔧 Si el logout principal no funciona:" -ForegroundColor White
Write-Host "   1. Usar el botón pequeño de 'power' en el modal" -ForegroundColor Gray
Write-Host "   2. Ir directamente a: $baseUrl/auth/logout" -ForegroundColor Gray
Write-Host "   3. Revisar mensajes de error en la consola" -ForegroundColor Gray
Write-Host

Write-Host "=== ROLES A PROBAR ===" -ForegroundColor Cyan
Write-Host
Write-Host "👑 ADMINISTRADOR:" -ForegroundColor White
Write-Host "   → admin / admin123" -ForegroundColor Gray
Write-Host "   → Logout desde /admin/dashboard" -ForegroundColor Gray
Write-Host
Write-Host "👥 CLIENTE:" -ForegroundColor White  
Write-Host "   → cliente1 / cliente123" -ForegroundColor Gray
Write-Host "   → Logout desde /client/dashboard" -ForegroundColor Gray
Write-Host
Write-Host "👷 EMPLEADO:" -ForegroundColor White
Write-Host "   → empleado1 / empleado123" -ForegroundColor Gray
Write-Host "   → Logout desde /empleado/dashboard" -ForegroundColor Gray
Write-Host

Write-Host "=== ÉXITO CONFIRMADO SI ===" -ForegroundColor Green
Write-Host
Write-Host "✅ Modal aparece correctamente" -ForegroundColor Green
Write-Host "✅ Countdown funciona (5 segundos)" -ForegroundColor Green
Write-Host "✅ Botón 'Cerrar Sesión Ahora' funciona" -ForegroundColor Green
Write-Host "✅ Aparece mensaje 'Cerrando sesión...'" -ForegroundColor Green
Write-Host "✅ Redirección automática a /auth/login" -ForegroundColor Green
Write-Host "✅ Mensaje de logout exitoso en login" -ForegroundColor Green
Write-Host "✅ NO se puede acceder a páginas protegidas" -ForegroundColor Green
Write-Host

Write-Host "🚀 ¡Comenzar verificación manual ahora!" -ForegroundColor Yellow
Write-Host
