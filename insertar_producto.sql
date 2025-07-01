-- Script SQL para insertar directamente el producto en la base de datos
-- Conectar a PostgreSQL y ejecutar estos comandos

-- Insertar el producto Samsung Galaxy S24 Ultra
INSERT INTO producto (
    nombre, 
    codigo_producto, 
    categoria, 
    marca, 
    precio, 
    descripcion, 
    stock_actual, 
    stock_minimo, 
    ubicacion_almacen, 
    fechavencimiento, 
    activo
) VALUES (
    'Smartphone Samsung Galaxy S24 Ultra',
    'SAM-S24U-512GB-TIT',
    'Tecnologia',
    'Samsung',
    4299.99,
    'Smartphone premium Galaxy S24 Ultra, 512GB, cámara 200MP, pantalla AMOLED 6.8", S Pen, Snapdragon 8 Gen 3, IP68, carga rápida 45W, 5G, Color Titanium Gray.',
    25,
    5,
    'A-15-C',
    '2026-06-22',
    true
);

-- Verificar que se insertó correctamente
SELECT * FROM producto WHERE codigo_producto = 'SAM-S24U-512GB-TIT';
