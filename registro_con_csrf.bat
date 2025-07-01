@echo off
setlocal enabledelayedexpansion

echo ===== REGISTRO DE PRODUCTO CON CSRF =====
echo.

echo Verificando sistema...
curl.exe -s -o nul http://localhost:8080
if %errorlevel% equ 0 (
    echo ✓ Sistema funcionando
) else (
    echo ✗ Sistema no disponible
    exit /b 1
)

echo.
echo Paso 1: Obteniendo token CSRF del login...

REM Obtener página de login
curl.exe -s -c cookies.txt http://localhost:8080/auth/login -o login.html

REM Extraer token CSRF
for /f "tokens=2 delims==^"" %%a in ('findstr "_csrf" login.html') do (
    set CSRF_TOKEN=%%a
)

echo Token CSRF obtenido: !CSRF_TOKEN!

echo.
echo Paso 2: Realizando login...

REM Hacer login con token CSRF
curl.exe -s -c cookies.txt -b cookies.txt ^
  -d "username=admin" ^
  -d "password=admin123" ^
  -d "_csrf=!CSRF_TOKEN!" ^
  -X POST ^
  http://localhost:8080/auth/login ^
  -L -o login_result.html

echo Login completado

echo.
echo Paso 3: Obteniendo página de nuevo producto...

curl.exe -s -b cookies.txt http://localhost:8080/admin/productos/nuevo -o producto_form.html

REM Extraer token CSRF del formulario de producto
for /f "tokens=2 delims==^"" %%a in ('findstr "_csrf" producto_form.html') do (
    set PRODUCT_CSRF_TOKEN=%%a
)

echo Token CSRF para producto: !PRODUCT_CSRF_TOKEN!

echo.
echo Paso 4: Registrando producto...
echo.
echo DATOS DEL PRODUCTO:
echo   Nombre: Smartphone Samsung Galaxy S24 Ultra
echo   Codigo: SAM-S24U-512GB-TIT  
echo   Categoria: Electronica
echo   Marca: Samsung
echo   Precio: S/ 4299.99
echo   Stock: 25 unidades (minimo: 5)
echo   Ubicacion: A-15-C
echo   Vencimiento: 2026-06-22
echo.

REM Registrar producto con token CSRF
curl.exe -v -c cookies.txt -b cookies.txt ^
  -d "nombre=Smartphone Samsung Galaxy S24 Ultra" ^
  -d "codigoProducto=SAM-S24U-512GB-TIT" ^
  -d "categoria=Electronica" ^
  -d "marca=Samsung" ^
  -d "precio=4299.99" ^
  -d "descripcion=Smartphone premium Samsung Galaxy S24 Ultra con 512GB de almacenamiento, camara de 200MP, pantalla AMOLED de 6.8 pulgadas, S Pen incluido, procesador Snapdragon 8 Gen 3, resistente al agua IP68, carga rapida de 45W y conectividad 5G. Color Titanium Gray." ^
  -d "stockActual=25" ^
  -d "stockMinimo=5" ^
  -d "ubicacionAlmacen=A-15-C" ^
  -d "fechaVencimiento=2026-06-22" ^
  -d "activo=true" ^
  -d "_csrf=!PRODUCT_CSRF_TOKEN!" ^
  -X POST ^
  http://localhost:8080/admin/productos/guardar ^
  -L -o producto_result.html

echo.
echo ===== VERIFICACION =====
echo.
echo Verificando que el producto aparezca en la lista...

curl.exe -s -b cookies.txt http://localhost:8080/admin/productos -o productos_lista.html

findstr "SAM-S24U-512GB-TIT" productos_lista.html > nul
if %errorlevel% equ 0 (
    echo ✓ PRODUCTO ENCONTRADO EN LA LISTA
) else (
    echo ⚠ Producto no visible en la lista aun
)

findstr "Samsung Galaxy S24 Ultra" productos_lista.html > nul  
if %errorlevel% equ 0 (
    echo ✓ NOMBRE DEL PRODUCTO ENCONTRADO
) else (
    echo ⚠ Nombre del producto no visible
)

echo.
echo ===== RESULTADO COMPLETO =====
echo Codigo del producto: SAM-S24U-512GB-TIT
echo Verificar en: http://localhost:8080/admin/productos
echo.

REM Mostrar un snippet del resultado para debug
echo Extracto del resultado del registro:
type producto_result.html | findstr /I "error\|success\|guardado\|registro" | head -5

echo.
echo Abriendo pagina de productos...
start http://localhost:8080/admin/productos

REM Limpiar archivos temporales
del login.html > nul 2>&1
del login_result.html > nul 2>&1
del producto_form.html > nul 2>&1
del producto_result.html > nul 2>&1
del productos_lista.html > nul 2>&1
del cookies.txt > nul 2>&1
