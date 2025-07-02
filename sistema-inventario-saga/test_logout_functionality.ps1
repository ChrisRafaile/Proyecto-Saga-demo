# Script para probar la funcionalidad de logout del Sistema de Inventario
# Autor: Christopher Lincoln Rafaile Naupay

Write-Host "=== PRUEBA DE FUNCIONALIDAD LOGOUT ===" -ForegroundColor Green
Write-Host

# Variables
$baseUrl = "http://localhost:8080"
$adminUrl = "$baseUrl/admin/dashboard"
$loginUrl = "$baseUrl/auth/login"
$logoutUrl = "$baseUrl/auth/logout"

Write-Host "1. Verificando que el servidor esté corriendo..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method GET -UseBasicParsing
    Write-Host "✓ Servidor corriendo en $baseUrl" -ForegroundColor Green
} catch {
    Write-Host "✗ Error: No se puede conectar al servidor" -ForegroundColor Red
    Write-Host "Asegúrate de que el servidor esté corriendo con: mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

Write-Host "2. Verificando página de login..." -ForegroundColor Yellow
try {
    $loginResponse = Invoke-WebRequest -Uri $loginUrl -Method GET -UseBasicParsing
    Write-Host "✓ Página de login accesible" -ForegroundColor Green
} catch {
    Write-Host "✗ Error accediendo a página de login" -ForegroundColor Red
}

Write-Host "3. Intentando hacer login como administrador..." -ForegroundColor Yellow
try {
    # Crear sesión web
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    
    # Obtener página de login para extraer CSRF token
    $loginPage = Invoke-WebRequest -Uri $loginUrl -WebSession $session -UseBasicParsing
    
    # Extraer CSRF token del contenido HTML
    $csrfPattern = 'name="_csrf"[^>]*value="([^"]*)"'
    $csrfMatch = [regex]::Match($loginPage.Content, $csrfPattern)
    
    if ($csrfMatch.Success) {
        $csrfToken = $csrfMatch.Groups[1].Value
        Write-Host "✓ Token CSRF obtenido: $($csrfToken.Substring(0,10))..." -ForegroundColor Green
        
        # Realizar login
        $loginData = @{
            username = "admin"
            password = "admin123"
            "_csrf" = $csrfToken
        }
        
        $loginResponse = Invoke-WebRequest -Uri $loginUrl -Method POST -Body $loginData -WebSession $session -UseBasicParsing
        
        if ($loginResponse.StatusCode -eq 200 -or $loginResponse.Headers.Location) {
            Write-Host "✓ Login exitoso" -ForegroundColor Green
            
            # Verificar acceso al dashboard admin
            Write-Host "4. Verificando acceso al dashboard admin..." -ForegroundColor Yellow
            $dashboardResponse = Invoke-WebRequest -Uri $adminUrl -WebSession $session -UseBasicParsing
            
            if ($dashboardResponse.StatusCode -eq 200) {
                Write-Host "✓ Dashboard admin accesible" -ForegroundColor Green
                
                # Probar logout
                Write-Host "5. Probando logout..." -ForegroundColor Yellow
                
                # Extraer CSRF token del dashboard
                $csrfPattern = 'name="_csrf"[^>]*value="([^"]*)"'
                $csrfMatch = [regex]::Match($dashboardResponse.Content, $csrfPattern)
                
                if ($csrfMatch.Success) {
                    $csrfToken = $csrfMatch.Groups[1].Value
                    Write-Host "✓ Token CSRF para logout obtenido" -ForegroundColor Green
                    
                    # Realizar logout
                    $logoutData = @{
                        "_csrf" = $csrfToken
                    }
                    
                    $logoutResponse = Invoke-WebRequest -Uri $logoutUrl -Method POST -Body $logoutData -WebSession $session -UseBasicParsing
                    
                    if ($logoutResponse.Headers.Location -like "*login*") {
                        Write-Host "✓ Logout exitoso - redirigido a login" -ForegroundColor Green
                        
                        # Verificar que no se pueda acceder al dashboard después del logout
                        Write-Host "6. Verificando que la sesión se cerró..." -ForegroundColor Yellow
                        try {
                            $testResponse = Invoke-WebRequest -Uri $adminUrl -WebSession $session -UseBasicParsing
                            if ($testResponse.Headers.Location -like "*login*") {
                                Write-Host "✓ Sesión cerrada correctamente - redirigido a login" -ForegroundColor Green
                            } else {
                                Write-Host "✗ Error: Todavía se puede acceder al dashboard" -ForegroundColor Red
                            }
                        } catch {
                            Write-Host "✓ Sesión cerrada correctamente - acceso denegado" -ForegroundColor Green
                        }
                        
                    } else {
                        Write-Host "✗ Error en logout - no redirigido correctamente" -ForegroundColor Red
                        Write-Host "Status: $($logoutResponse.StatusCode)" -ForegroundColor Yellow
                        Write-Host "Location: $($logoutResponse.Headers.Location)" -ForegroundColor Yellow
                    }
                } else {
                    Write-Host "✗ No se pudo obtener token CSRF para logout" -ForegroundColor Red
                }
            } else {
                Write-Host "✗ No se puede acceder al dashboard admin" -ForegroundColor Red
            }
        } else {
            Write-Host "✗ Login falló" -ForegroundColor Red
        }
    } else {
        Write-Host "✗ No se pudo obtener token CSRF" -ForegroundColor Red
    }
    
} catch {
    Write-Host "✗ Error durante el proceso de login/logout: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host
Write-Host "=== PRUEBA MANUAL RECOMENDADA ===" -ForegroundColor Cyan
Write-Host "1. Abre tu navegador en $loginUrl"
Write-Host "2. Inicia sesión con: admin / admin123"
Write-Host "3. Ve al dashboard admin"
Write-Host "4. Haz clic en el dropdown del usuario"
Write-Host "5. Haz clic en 'Cerrar Sesión'"
Write-Host "6. Confirma en el modal"
Write-Host "7. Verifica que seas redirigido a login"
Write-Host "8. Verifica que no puedas acceder a páginas protegidas"
Write-Host
