@echo off
echo ===== REGISTRO DE PRODUCTO SAGA FALABELLA =====
echo.

echo Verificando sistema...
curl -s http://localhost:8080 > nul
if %errorlevel% equ 0 (
    echo ✓ Sistema funcionando
) else (
    echo X Error: Sistema no disponible
    pause
    exit /b 1
)

echo.
echo Realizando login y registro de producto...
echo.
echo DATOS DEL PRODUCTO:
echo Nombre: Smartphone Samsung Galaxy S24 Ultra
echo Codigo: SAM-S24U-512GB-TIT
echo Categoria: Electronica
echo Marca: Samsung
echo Precio: S/ 4299.99
echo Stock: 25 unidades (minimo: 5)
echo Ubicacion: A-15-C
echo Vencimiento: 2026-06-22
echo.

echo Enviando datos del producto...

curl -X POST "http://localhost:8080/admin/productos/guardar" ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
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
  -c cookies.txt ^
  -b cookies.txt ^
  -u "admin:admin123" ^
  -L

echo.
echo ===== PROCESO COMPLETADO =====
echo.
echo Verificar producto en: http://localhost:8080/admin/productos
echo Codigo del producto: SAM-S24U-512GB-TIT
echo.

echo Abriendo pagina de productos...
start http://localhost:8080/admin/productos

pause
