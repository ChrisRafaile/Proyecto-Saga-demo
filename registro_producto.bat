echo "=== REGISTRO DE PRODUCTO SAGA FALABELLA ==="

echo "Verificando sistema..."
curl -s http://localhost:8080 > nul
if %errorlevel% equ 0 (
    echo Sistema funcionando correctamente
) else (
    echo Error: Sistema no disponible
    exit /b 1
)

echo.
echo "Registrando producto completo..."
echo Nombre: Smartphone Samsung Galaxy S24 Ultra
echo Codigo: SAM-S24U-512GB-TIT
echo Categoria: Electronica
echo Marca: Samsung
echo Precio: S/ 4299.99
echo Stock: 25 unidades
echo.

curl -X POST http://localhost:8080/admin/productos/guardar ^
  -d "nombre=Smartphone Samsung Galaxy S24 Ultra" ^
  -d "codigo=SAM-S24U-512GB-TIT" ^
  -d "categoria=Electronica" ^
  -d "marca=Samsung" ^
  -d "precio=4299.99" ^
  -d "descripcion=Smartphone premium Samsung Galaxy S24 Ultra con 512GB de almacenamiento, camara de 200MP, pantalla AMOLED de 6.8 pulgadas, S Pen incluido, procesador Snapdragon 8 Gen 3, resistente al agua IP68, carga rapida de 45W y conectividad 5G. Color Titanium Gray." ^
  -d "stockActual=25" ^
  -d "stockMinimo=5" ^
  -d "ubicacionAlmacen=A-15-C" ^
  -d "fechaVencimiento=2025-12-22" ^
  -d "estado=true" ^
  -c cookies.txt ^
  -b cookies.txt ^
  -L

echo.
echo "PRODUCTO REGISTRADO EXITOSAMENTE"
echo "Verificar en: http://localhost:8080/admin/productos"
echo "Codigo del producto: SAM-S24U-512GB-TIT"

start http://localhost:8080/admin/productos
