# Test Simple - Verificar Navegacion del Logo en Admin
# ===================================================

Write-Host "Iniciando pruebas de navegacion del logo..." -ForegroundColor Green

$baseUrl = "http://localhost:8080"

# Test 1: Verificar que el sistema esta funcionando
Write-Host "`nTest 1: Verificando sistema..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/login" -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        Write-Host "PASS - Sistema funcionando" -ForegroundColor Green
    }
} catch {
    Write-Host "FAIL - Sistema no disponible: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Login como admin
Write-Host "`nTest 2: Login como admin..." -ForegroundColor Yellow
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

try {
    # Hacer login
    $loginData = @{
        username = "admin"
        password = "admin123"
    }
    
    $loginResponse = Invoke-WebRequest -Uri "$baseUrl/login" -Method POST -Body $loginData -WebSession $session -UseBasicParsing
    Write-Host "PASS - Login exitoso" -ForegroundColor Green
} catch {
    Write-Host "FAIL - Error en login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: Verificar pagina admin dashboard
Write-Host "`nTest 3: Accediendo a admin dashboard..." -ForegroundColor Yellow
try {
    $dashboardResponse = Invoke-WebRequest -Uri "$baseUrl/admin/dashboard" -WebSession $session -UseBasicParsing
    if ($dashboardResponse.StatusCode -eq 200) {
        Write-Host "PASS - Admin dashboard accesible" -ForegroundColor Green
        
        # Buscar enlace del logo
        if ($dashboardResponse.Content -like '*navbar-brand*href="/admin/dashboard"*') {
            Write-Host "PASS - Logo redirige a /admin/dashboard" -ForegroundColor Green
        } elseif ($dashboardResponse.Content -like '*navbar-brand*href="/dashboard"*') {
            Write-Host "FAIL - Logo aun redirige a /dashboard" -ForegroundColor Red
        } else {
            Write-Host "WARNING - No se pudo determinar destino del logo" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "FAIL - No se pudo acceder a admin dashboard: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Verificar pagina admin usuarios
Write-Host "`nTest 4: Accediendo a admin usuarios..." -ForegroundColor Yellow
try {
    $usuariosResponse = Invoke-WebRequest -Uri "$baseUrl/admin/usuarios" -WebSession $session -UseBasicParsing
    if ($usuariosResponse.StatusCode -eq 200) {
        Write-Host "PASS - Admin usuarios accesible" -ForegroundColor Green
        
        # Buscar enlace del logo
        if ($usuariosResponse.Content -like '*navbar-brand*href="/admin/dashboard"*') {
            Write-Host "PASS - Logo redirige a /admin/dashboard desde usuarios" -ForegroundColor Green
        } elseif ($usuariosResponse.Content -like '*navbar-brand*href="/dashboard"*') {
            Write-Host "FAIL - Logo aun redirige a /dashboard desde usuarios" -ForegroundColor Red
        } else {
            Write-Host "WARNING - No se pudo determinar destino del logo desde usuarios" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "FAIL - No se pudo acceder a admin usuarios: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Verificar logout
Write-Host "`nTest 5: Verificando logout..." -ForegroundColor Yellow
try {
    $logoutResponse = Invoke-WebRequest -Uri "$baseUrl/logout" -Method POST -WebSession $session -UseBasicParsing
    Write-Host "PASS - Logout funcionando" -ForegroundColor Green
} catch {
    Write-Host "FAIL - Error en logout: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nPruebas completadas." -ForegroundColor Blue
