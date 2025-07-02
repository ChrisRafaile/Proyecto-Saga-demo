# Test Script - Verificar Navegación del Logo en Páginas de Admin
# ================================================================

Write-Host "INICIANDO PRUEBAS DE NAVEGACIÓN DEL LOGO EN PÁGINAS ADMIN..." -ForegroundColor Green
Write-Host "===============================================================" -ForegroundColor Green

$baseUrl = "http://localhost:8080"
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# Función para hacer login como admin
function Login-AsAdmin {
    Write-Host "`n[LOGIN] Autenticando como administrador..." -ForegroundColor Yellow
    
    try {
        # Obtener página de login
        $loginPage = Invoke-WebRequest -Uri "$baseUrl/login" -SessionVariable session
        Write-Host "✓ Página de login obtenida" -ForegroundColor Green
        
        # Hacer login
        $loginData = @{
            username = "admin"
            password = "admin123"
        }
        
        $loginResponse = Invoke-WebRequest -Uri "$baseUrl/login" -Method POST -Body $loginData -WebSession $session
        Write-Host "✓ Login realizado" -ForegroundColor Green
        
        return $session
    }
    catch {
        Write-Host "✗ Error en login: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Función para verificar que el logo redirige correctamente
function Test-LogoNavigation {
    param([string]$currentPage, [string]$expectedRedirect)
    
    Write-Host "`n[TEST] Verificando navegación del logo desde: $currentPage" -ForegroundColor Cyan
    Write-Host "Expectativa: Redirección a $expectedRedirect" -ForegroundColor Yellow
    
    try {
        # Acceder a la página actual
        $response = Invoke-WebRequest -Uri "$baseUrl$currentPage" -WebSession $session
        
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ Acceso exitoso a $currentPage" -ForegroundColor Green
            
            # Buscar el enlace del logo en el HTML
            $logoLinkPattern = '<a[^>]*class="navbar-brand[^"]*"[^>]*href="([^"]*)"'
            if ($response.Content -match $logoLinkPattern) {
                $logoHref = $matches[1]
                Write-Host "✓ Enlace del logo encontrado: $logoHref" -ForegroundColor Green
                
                # Verificar que coincide con lo esperado
                if ($logoHref -eq $expectedRedirect) {
                    Write-Host "✓ CORRECTO: El logo redirige a $logoHref" -ForegroundColor Green
                    return $true
                } else {
                    Write-Host "✗ ERROR: Se esperaba '$expectedRedirect' pero se encontró '$logoHref'" -ForegroundColor Red
                    return $false
                }
            } else {
                Write-Host "✗ No se encontró el enlace del logo en la página" -ForegroundColor Red
                return $false
            }
        } else {
            Write-Host "✗ No se pudo acceder a $currentPage (Status: $($response.StatusCode))" -ForegroundColor Red
            return $false
        }
    }
    catch {
        Write-Host "✗ Error accediendo a $currentPage : $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Función para verificar logout
function Test-Logout {
    Write-Host "`n[TEST] Verificando logout desde páginas admin..." -ForegroundColor Cyan
    
    try {
        $logoutResponse = Invoke-WebRequest -Uri "$baseUrl/logout" -Method POST -WebSession $session
        Write-Host "✓ Logout exitoso" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "✗ Error en logout: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# ===========================
# EJECUTAR PRUEBAS
# ===========================

$session = Login-AsAdmin
if (-not $session) {
    Write-Host "No se pudo hacer login. Saliendo..." -ForegroundColor Red
    exit 1
}

Write-Host "`n" + "="*60 -ForegroundColor Blue
Write-Host "TESTING NAVEGACIÓN DEL LOGO EN PÁGINAS ADMIN" -ForegroundColor Blue
Write-Host "="*60 -ForegroundColor Blue

$testResults = @()

# Probar páginas de administrador - todas deben redirigir a /admin/dashboard
$adminPages = @(
    "/admin/dashboard",
    "/admin/productos", 
    "/admin/usuarios",
    "/admin/alertas",
    "/admin/proveedores"
)

foreach ($page in $adminPages) {
    $result = Test-LogoNavigation -currentPage $page -expectedRedirect "/admin/dashboard"
    $testResults += @{
        Page = $page
        Expected = "/admin/dashboard"
        Success = $result
    }
}

# Verificar logout
$logoutResult = Test-Logout

# ===========================
# RESUMEN DE RESULTADOS
# ===========================

Write-Host "`n" + "="*60 -ForegroundColor Magenta
Write-Host "RESUMEN DE RESULTADOS" -ForegroundColor Magenta
Write-Host "="*60 -ForegroundColor Magenta

$successCount = ($testResults | Where-Object { $_.Success }).Count
$totalTests = $testResults.Count

Write-Host "`nPruebas de Navegación del Logo:"
foreach ($result in $testResults) {
    $status = if ($result.Success) { "✓ PASS" } else { "✗ FAIL" }
    $color = if ($result.Success) { "Green" } else { "Red" }
    Write-Host "  $($result.Page) → $($result.Expected): $status" -ForegroundColor $color
}

Write-Host "`nPrueba de Logout:" 
$logoutStatus = if ($logoutResult) { "✓ PASS" } else { "✗ FAIL" }
$logoutColor = if ($logoutResult) { "Green" } else { "Red" }
Write-Host "  Logout desde admin: $logoutStatus" -ForegroundColor $logoutColor

Write-Host "`n" + "-"*40 -ForegroundColor Gray
Write-Host "TOTAL: $successCount/$totalTests pruebas de navegación exitosas" -ForegroundColor $(if ($successCount -eq $totalTests) { "Green" } else { "Yellow" })

if ($successCount -eq $totalTests -and $logoutResult) {
    Write-Host "`n🎉 TODAS LAS PRUEBAS PASARON - Navegación funcionando correctamente!" -ForegroundColor Green
} else {
    Write-Host "`n⚠️  Algunas pruebas fallaron. Revisar implementación." -ForegroundColor Yellow
}

Write-Host "`nPruebas completadas." -ForegroundColor Blue
