# Script para probar la robustez del perfil de administrador
Write-Host "=== PRUEBA DE ROBUSTEZ DEL PERFIL DE ADMINISTRADOR ===" -ForegroundColor Cyan
Write-Host "Probando la página de perfil múltiples veces para verificar consistencia..." -ForegroundColor Yellow

# Configuración
$baseUrl = "http://localhost:8080"
$perfilUrl = "$baseUrl/admin/perfil"
$loginUrl = "$baseUrl/auth/login"

# Función para probar la carga del perfil
function Test-PerfilLoad {
    param([int]$attempt)
    
    try {
        Write-Host "Intento $attempt..." -ForegroundColor White
        
        # Hacer petición al perfil
        $response = Invoke-WebRequest -Uri $perfilUrl -Method Get -SessionVariable session -ErrorAction Stop
        
        # Verificar si la respuesta contiene los datos esperados
        $hasNombreCompleto = $response.Content -match 'Administrador Principal|admin@sagafalabella.com'
        $hasUserInfo = $response.Content -match 'Nombre de Usuario:|Correo Electrónico:'
        $hasStats = $response.Content -match 'Total de Sesiones|Acciones Realizadas'
        
        if ($hasNombreCompleto -and $hasUserInfo -and $hasStats) {
            Write-Host "  ✓ Perfil cargado correctamente" -ForegroundColor Green
            return $true
        } else {
            Write-Host "  ✗ Perfil NO cargado correctamente - Datos faltantes" -ForegroundColor Red
            return $false
        }
        
    } catch {
        Write-Host "  ✗ Error al cargar perfil: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Ejecutar múltiples pruebas
Write-Host "`nEjecutando 10 pruebas de carga del perfil..." -ForegroundColor Yellow

$successCount = 0
$totalTests = 10

for ($i = 1; $i -le $totalTests; $i++) {
    $result = Test-PerfilLoad -attempt $i
    if ($result) {
        $successCount++
    }
    
    # Pequeña pausa entre pruebas
    Start-Sleep -Milliseconds 500
}

# Mostrar resultados
Write-Host "`n=== RESULTADOS ===" -ForegroundColor Cyan
Write-Host "Pruebas exitosas: $successCount/$totalTests" -ForegroundColor White

if ($successCount -eq $totalTests) {
    Write-Host "✓ TODAS LAS PRUEBAS EXITOSAS - El perfil es robusto" -ForegroundColor Green
} elseif ($successCount -ge ($totalTests * 0.8)) {
    Write-Host "⚠ MAYORÍA DE PRUEBAS EXITOSAS - El perfil es mayormente estable" -ForegroundColor Yellow
} else {
    Write-Host "✗ MÚLTIPLES FALLAS - El perfil requiere más trabajo" -ForegroundColor Red
}

Write-Host "`n=== RECOMENDACIONES ===" -ForegroundColor Cyan
Write-Host "1. Si hay fallas, revise los logs del servidor" -ForegroundColor White
Write-Host "2. Verifique la conexión a la base de datos" -ForegroundColor White
Write-Host "3. Asegúrese de que el servidor esté corriendo en $baseUrl" -ForegroundColor White
Write-Host "4. Los cambios realizados mejoran la robustez del controller" -ForegroundColor White

Write-Host "`nPrueba completada. Presione Enter para continuar..." -ForegroundColor Gray
Read-Host
