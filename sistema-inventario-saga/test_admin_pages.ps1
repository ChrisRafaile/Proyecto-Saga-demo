# Script completo para probar funcionalidades CRUD del sistema admin
$baseUrl = "http://localhost:8080"

# Páginas principales de administración
$adminPages = @(
    @{Url="/admin/dashboard"; Name="Dashboard Principal"; Crud="READ"},
    @{Url="/admin/usuarios"; Name="Gestión de Usuarios"; Crud="CRUD Completo"},
    @{Url="/admin/productos"; Name="Gestión de Productos"; Crud="CRUD Completo + Imágenes"},
    @{Url="/admin/proveedores"; Name="Gestión de Proveedores"; Crud="READ + Service"},
    @{Url="/admin/reportes"; Name="Reportes del Sistema"; Crud="READ + Estadísticas"},
    @{Url="/admin/configuracion"; Name="Configuración"; Crud="Básico"},
    @{Url="/admin/respaldos"; Name="Respaldos"; Crud="Básico"}
)

# Formularios y acciones específicas
$adminForms = @(
    @{Url="/admin/usuarios/nuevo"; Name="Nuevo Usuario"; Crud="CREATE"},
    @{Url="/admin/productos/nuevo"; Name="Nuevo Producto"; Crud="CREATE"},
    @{Url="/admin/productos/carga-masiva"; Name="Carga Masiva"; Crud="BULK CREATE"}
)

Write-Host "========================================" -ForegroundColor Magenta
Write-Host "TESTING SAGA FALABELLA ADMIN SYSTEM" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

Write-Host "🔐 LOGIN REQUIRED:" -ForegroundColor Yellow
Write-Host "   URL: $baseUrl/auth/login" -ForegroundColor White
Write-Host "   Usuario: admin" -ForegroundColor White  
Write-Host "   Contraseña: admin123" -ForegroundColor White
Write-Host ""

Write-Host "📊 TESTING MAIN ADMIN PAGES..." -ForegroundColor Green
Write-Host ""

foreach ($page in $adminPages) {
    $url = $baseUrl + $page.Url
    Write-Host "🔗 Testing: " -NoNewline -ForegroundColor Cyan
    Write-Host "$($page.Name)" -NoNewline -ForegroundColor White
    Write-Host " [$($page.Crud)]" -ForegroundColor Gray
    Write-Host "   URL: $url" -ForegroundColor DarkGray
    
    try {
        $response = Invoke-WebRequest -Uri $url -UseBasicParsing -ErrorAction Stop
        $statusCode = $response.StatusCode
        
        if ($statusCode -eq 200) {
            Write-Host "   ✅ Status 200 - OK" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️  Status: $statusCode" -ForegroundColor Yellow
        }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        if ($statusCode -eq 302) {
            Write-Host "   🔒 Redirect (302) - Login required" -ForegroundColor Blue
        } else {
            Write-Host "   ❌ Error: $statusCode" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Start-Sleep -Milliseconds 200
}

Write-Host "📝 TESTING FORMS & ACTIONS..." -ForegroundColor Green
Write-Host ""

foreach ($form in $adminForms) {
    $url = $baseUrl + $form.Url
    Write-Host "📋 Testing: " -NoNewline -ForegroundColor Cyan
    Write-Host "$($form.Name)" -NoNewline -ForegroundColor White
    Write-Host " [$($form.Crud)]" -ForegroundColor Gray
    Write-Host "   URL: $url" -ForegroundColor DarkGray
    
    try {
        $response = Invoke-WebRequest -Uri $url -UseBasicParsing -ErrorAction Stop
        $statusCode = $response.StatusCode
        
        if ($statusCode -eq 200) {
            Write-Host "   ✅ Status 200 - Form Ready" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️  Status: $statusCode" -ForegroundColor Yellow
        }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        if ($statusCode -eq 302) {
            Write-Host "   🔒 Redirect (302) - Login required" -ForegroundColor Blue
        } else {
            Write-Host "   ❌ Error: $statusCode" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Start-Sleep -Milliseconds 200
}

Write-Host "========================================" -ForegroundColor Magenta
Write-Host "✅ TEST COMPLETED!" -ForegroundColor Green
Write-Host ""
Write-Host "📖 NEXT STEPS:" -ForegroundColor Yellow
Write-Host "1. Login at: $baseUrl/auth/login" -ForegroundColor White
Write-Host "2. Test CRUD operations:" -ForegroundColor White
Write-Host "   • Create new user" -ForegroundColor Gray
Write-Host "   • Edit existing product" -ForegroundColor Gray
Write-Host "   • Upload product image" -ForegroundColor Gray
Write-Host "   • Generate sample products" -ForegroundColor Gray
Write-Host "3. Verify all quick actions work" -ForegroundColor White
Write-Host ""
Write-Host "📋 Full documentation: RESUMEN_FUNCIONALIDADES_CRUD.md" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Magenta
