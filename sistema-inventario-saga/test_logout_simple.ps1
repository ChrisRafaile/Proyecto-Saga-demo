# Script para probar la funcionalidad de logout del Sistema de Inventario
# Autor: Christopher Lincoln Rafaile Naupay

Write-Host "=== PRUEBA DE FUNCIONALIDAD LOGOUT ===" -ForegroundColor Green
Write-Host

# Variables
$baseUrl = "http://localhost:8080"
$adminUrl = "$baseUrl/admin/dashboard"
$loginUrl = "$baseUrl/auth/login"
$logoutUrl = "$baseUrl/auth/logout"

Write-Host "1. Verificando que el servidor este corriendo..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method GET -UseBasicParsing
    Write-Host "✓ Servidor corriendo en $baseUrl" -ForegroundColor Green
} catch {
    Write-Host "✗ Error: No se puede conectar al servidor" -ForegroundColor Red
    Write-Host "Asegurate de que el servidor este corriendo con: mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

Write-Host "2. Verificando pagina de login..." -ForegroundColor Yellow
try {
    $loginResponse = Invoke-WebRequest -Uri $loginUrl -Method GET -UseBasicParsing
    Write-Host "✓ Pagina de login accesible" -ForegroundColor Green
} catch {
    Write-Host "✗ Error accediendo a pagina de login" -ForegroundColor Red
}

Write-Host
Write-Host "=== PRUEBA MANUAL RECOMENDADA ===" -ForegroundColor Cyan
Write-Host "1. Abre tu navegador en $loginUrl"
Write-Host "2. Inicia sesion con: admin / admin123"
Write-Host "3. Ve al dashboard admin"
Write-Host "4. Haz clic en el dropdown del usuario"
Write-Host "5. Haz clic en 'Cerrar Sesion'"
Write-Host "6. Confirma en el modal"
Write-Host "7. Verifica que seas redirigido a login"
Write-Host "8. Verifica que no puedas acceder a paginas protegidas"
Write-Host
Write-Host "=== PROBLEMAS CONOCIDOS A VERIFICAR ===" -ForegroundColor Yellow
Write-Host "- El modal se muestra pero logout no funciona"
Write-Host "- La sesion no se cierra completamente"
Write-Host "- No hay redireccion a login despues del logout"
Write-Host "- El formulario de logout no se envia correctamente"
Write-Host
