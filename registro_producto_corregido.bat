@echo off
echo ===== REGISTRO DE PRODUCTO CORREGIDO =====
echo.

echo Verificando sistema...
curl -s -o nul http://localhost:8080
if %errorlevel% equ 0 (
    echo ✓ Sistema funcionando
) else (
    echo ✗ Sistema no disponible
    exit /b 1
)

echo.
echo Obteniendo sesion y token CSRF...

REM Crear archivo de cookies
echo. > cookies.txt

REM Obtener pagina de login para obtener token CSRF
curl -s -c cookies.txt -b cookies.txt http://localhost:8080/login > login_page.html

REM Realizar login
echo Realizando login...
curl -s -c cookies.txt -b cookies.txt ^
  -d "username=admin" ^
  -d "password=admin123" ^
  -X POST ^
  http://localhost:8080/login ^
  -o login_result.html

echo.
echo Registrando producto con campos correctos...
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

REM Registrar producto con nombres de campos correctos
curl -v -c cookies.txt -b cookies.txt ^
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
  -X POST ^
  http://localhost:8080/admin/productos/guardar

echo.
echo ===== VERIFICACION =====
echo.
echo Verificando que el producto aparezca en la lista...

curl -s -b cookies.txt http://localhost:8080/admin/productos > productos_lista.html

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
echo ===== PROCESO COMPLETADO =====
echo Verificar producto en: http://localhost:8080/admin/productos
echo Codigo del producto: SAM-S24U-512GB-TIT
echo.

REM Limpiar archivos temporales
del login_page.html > nul 2>&1
del login_result.html > nul 2>&1
del cookies.txt > nul 2>&1

echo Abriendo pagina de productos para verificacion visual...
start http://localhost:8080/admin/productos
