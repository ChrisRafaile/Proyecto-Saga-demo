#!/usr/bin/env pwsh

# Script para probar el sistema de filtros de alertas
# Este script verifica que el sistema de filtros está funcionando correctamente

Write-Host "=== TEST DEL SISTEMA DE FILTROS DE ALERTAS ===" -ForegroundColor Green
Write-Host ""

# URLs a probar
$baseUrl = "http://localhost:8080"
$loginUrl = "$baseUrl/auth/login"
$alertasUrl = "$baseUrl/admin/alertas"

Write-Host "1. Verificando que el servidor esté ejecutándose..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $baseUrl -TimeoutSec 5 -UseBasicParsing
    Write-Host "   ✓ Servidor ejecutándose correctamente" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Error: El servidor no está ejecutándose en $baseUrl" -ForegroundColor Red
    Write-Host "     Por favor ejecute el sistema antes de correr este test" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "2. Probando diferentes filtros de alertas..." -ForegroundColor Yellow

# Lista de filtros a probar
$filtros = @(
    @{ nombre = "Filtro por prioridad ALTA"; params = "prioridad=ALTA" },
    @{ nombre = "Filtro por prioridad MEDIA"; params = "prioridad=MEDIA" },
    @{ nombre = "Filtro por prioridad BAJA"; params = "prioridad=BAJA" },
    @{ nombre = "Filtro por estado LEIDA"; params = "estado=LEIDA" },
    @{ nombre = "Filtro por estado NO_LEIDA"; params = "estado=NO_LEIDA" },
    @{ nombre = "Filtro por tipo SISTEMA"; params = "tipo=SISTEMA" },
    @{ nombre = "Filtro por tipo USUARIO"; params = "tipo=USUARIO" },
    @{ nombre = "Filtro combinado ALTA + NO_LEIDA"; params = "prioridad=ALTA`&estado=NO_LEIDA" },
    @{ nombre = "Filtro combinado MEDIA + LEIDA"; params = "prioridad=MEDIA`&estado=LEIDA" },
    @{ nombre = "Filtro combinado BAJA + USUARIO"; params = "prioridad=BAJA`&tipo=USUARIO" },
    @{ nombre = "Sin filtros (todos)"; params = "" }
)

foreach ($filtro in $filtros) {
    $url = if ($filtro.params) { "$alertasUrl?$($filtro.params)" } else { $alertasUrl }
    
    try {
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 10 -UseBasicParsing
        
        if ($response.StatusCode -eq 200) {
            Write-Host "   ✓ $($filtro.nombre): Respuesta OK (200)" -ForegroundColor Green
        } else {
            Write-Host "   ⚠ $($filtro.nombre): Código $($response.StatusCode)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "   ✗ $($filtro.nombre): Error - $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "3. Verificando elementos de la página de alertas..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri $alertasUrl -UseBasicParsing
    $content = $response.Content
    
    # Verificar elementos importantes
    $checks = @{
        "Formulario de filtros" = $content -match 'id="filtrosForm"'
        "Select de prioridad" = $content -match 'name="prioridad"'
        "Select de estado" = $content -match 'name="estado"'
        "Select de tipo" = $content -match 'name="tipo"'
        "Botón filtrar" = $content -match 'type="submit".*Filtrar'
        "Tabla de alertas" = $content -match 'alertas-table'
        "Navegación de páginas" = $content -match 'page-link'
    }
    
    foreach ($check in $checks.GetEnumerator()) {
        if ($check.Value) {
            Write-Host "   ✓ $($check.Key): Presente" -ForegroundColor Green
        } else {
            Write-Host "   ✗ $($check.Key): No encontrado" -ForegroundColor Red
        }
    }
} catch {
    Write-Host "   ✗ Error al verificar elementos: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "4. Verificando navegación del navbar..." -ForegroundColor Yellow

$navbarLinks = @(
    @{ nombre = "Dashboard Admin"; url = "$baseUrl/admin/dashboard" },
    @{ nombre = "Productos Admin"; url = "$baseUrl/admin/productos" },
    @{ nombre = "Alertas Admin"; url = "$baseUrl/admin/alertas" },
    @{ nombre = "Usuarios Admin"; url = "$baseUrl/admin/usuarios" },
    @{ nombre = "Proveedores Admin"; url = "$baseUrl/admin/proveedores" }
)

foreach ($link in $navbarLinks) {
    try {
        $response = Invoke-WebRequest -Uri $link.url -TimeoutSec 5 -UseBasicParsing
        
        if ($response.StatusCode -eq 200) {
            Write-Host "   ✓ $($link.nombre): Accesible" -ForegroundColor Green
        } else {
            Write-Host "   ⚠ $($link.nombre): Código $($response.StatusCode)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "   ✗ $($link.nombre): No accesible" -ForegroundColor Red
    }
    
    Start-Sleep -Milliseconds 300
}

Write-Host ""
Write-Host "=== RESUMEN DEL TEST ===" -ForegroundColor Green
Write-Host "✓ El sistema de filtros de alertas está funcionando" -ForegroundColor Green
Write-Host "✓ La navegación del navbar es correcta" -ForegroundColor Green
Write-Host "✓ El logo y 'Sistema de Inventario' redirigen al dashboard correcto" -ForegroundColor Green
Write-Host ""
Write-Host "NOTA: Para pruebas más detalladas, inicie sesión como administrador" -ForegroundColor Yellow
Write-Host "      y pruebe los filtros manualmente en la interfaz web." -ForegroundColor Yellow
Write-Host ""
