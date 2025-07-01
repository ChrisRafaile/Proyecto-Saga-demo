-- ============================================================================
-- SCRIPT PARA CONECTAR Y ANALIZAR LA BASE DE DATOS SAGA FALABELLA EXISTENTE
-- ============================================================================

-- Conectar a la base de datos (ejecutar en terminal)
-- psql -h localhost -U saga_user -d saga_inventario_db

-- ANÁLISIS DETALLADO DE LA ESTRUCTURA ACTUAL
-- ============================================================================

-- 1. Listar todas las tablas existentes
\dt

-- 2. Describir estructura de cada tabla
\d cliente
\d devolucion  
\d pedido
\d pedido_producto
\d producto
\d proveedor
\d ruta_entrega
\d usuario_sistema

-- 3. Ver datos de ejemplo de cada tabla (primeros 5 registros)
SELECT 'TABLA CLIENTE:' as tabla;
SELECT * FROM cliente LIMIT 5;

SELECT 'TABLA PRODUCTO:' as tabla;
SELECT * FROM producto LIMIT 5;

SELECT 'TABLA PROVEEDOR:' as tabla;
SELECT * FROM proveedor LIMIT 5;

SELECT 'TABLA PEDIDO:' as tabla;
SELECT * FROM pedido LIMIT 5;

SELECT 'TABLA PEDIDO_PRODUCTO:' as tabla;
SELECT * FROM pedido_producto LIMIT 5;

-- 4. Verificar relaciones entre tablas
SELECT 
    conname as constraint_name,
    conrelid::regclass as table_name,
    confrelid::regclass as referenced_table
FROM pg_constraint 
WHERE contype = 'f'
ORDER BY conrelid::regclass::text;

-- 5. Contar registros en cada tabla
SELECT 'CONTEO DE REGISTROS POR TABLA:' as info;
SELECT 
    'cliente' as tabla, COUNT(*) as registros FROM cliente
UNION ALL
SELECT 
    'producto' as tabla, COUNT(*) as registros FROM producto
UNION ALL
SELECT 
    'proveedor' as tabla, COUNT(*) as registros FROM proveedor
UNION ALL
SELECT 
    'pedido' as tabla, COUNT(*) as registros FROM pedido
UNION ALL
SELECT 
    'pedido_producto' as tabla, COUNT(*) as registros FROM pedido_producto
UNION ALL
SELECT 
    'devolucion' as tabla, COUNT(*) as registros FROM devolucion
UNION ALL
SELECT 
    'ruta_entrega' as tabla, COUNT(*) as registros FROM ruta_entrega
UNION ALL
SELECT 
    'usuario_sistema' as tabla, COUNT(*) as registros FROM usuario_sistema;

-- ============================================================================
-- IDENTIFICAR MEJORAS NECESARIAS
-- ============================================================================

-- Verificar si faltan campos importantes
SELECT 'ANÁLISIS DE CAMPOS FALTANTES:' as info;

-- En tabla producto
SELECT 
    CASE 
        WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_name='producto' AND column_name='fecha_creacion') 
        THEN 'OK' 
        ELSE 'FALTA fecha_creacion en producto' 
    END as fecha_creacion_producto,
    CASE 
        WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_name='producto' AND column_name='activo') 
        THEN 'OK' 
        ELSE 'FALTA campo activo en producto' 
    END as activo_producto;

-- En tabla usuario_sistema  
SELECT 
    CASE 
        WHEN EXISTS(SELECT 1 FROM information_schema.columns WHERE table_name='usuario_sistema' AND column_name='activo') 
        THEN 'OK' 
        ELSE 'FALTA campo activo en usuario_sistema' 
    END as activo_usuario;

-- Verificar si existen tablas de auditoría
SELECT 
    CASE 
        WHEN EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name='movimientos_inventario') 
        THEN 'OK' 
        ELSE 'FALTA tabla movimientos_inventario' 
    END as tabla_movimientos,
    CASE 
        WHEN EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name='ordenes_picking') 
        THEN 'OK' 
        ELSE 'FALTA tabla ordenes_picking' 
    END as tabla_picking,
    CASE 
        WHEN EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name='roles') 
        THEN 'OK' 
        ELSE 'FALTA tabla roles' 
    END as tabla_roles;
