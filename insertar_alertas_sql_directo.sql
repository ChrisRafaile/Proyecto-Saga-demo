-- Script SQL directo para insertar alertas de ejemplo
-- Ejecutar este script directamente en tu base de datos PostgreSQL

-- Primero verificar la estructura de la tabla
\d alertas;

-- Limpiar alertas existentes (opcional)
-- DELETE FROM alertas WHERE titulo LIKE '%Laptop HP Pavilion%' OR titulo LIKE '%Acceso No Autorizado%';

-- Insertar alertas de ejemplo directamente
INSERT INTO alertas (
    titulo, 
    descripcion, 
    tipo_alerta, 
    nivel_prioridad, 
    estado, 
    entidad_relacionada, 
    id_entidad_relacionada, 
    fecha_creacion
) VALUES 
-- Alerta 1: Stock Crítico
(
    'Stock Crítico - Laptop HP Pavilion',
    'El producto Laptop HP Pavilion tiene stock crítico (2 unidades restantes)',
    'STOCK_BAJO',
    'CRITICA',
    'NO_LEIDA',
    'PRODUCTO',
    1,
    NOW()
),
-- Alerta 2: Seguridad
(
    'Intento de Acceso No Autorizado',
    'Se detectó un intento de acceso no autorizado desde IP: 192.168.1.100',
    'SEGURIDAD',
    'ALTA',
    'NO_LEIDA',
    'SISTEMA',
    NULL,
    NOW()
),
-- Alerta 3: Sistema
(
    'Backup Automático Fallido',
    'El backup automático programado para las 2:00 AM ha fallado',
    'SISTEMA',
    'MEDIA',
    'NO_LEIDA',
    'SISTEMA',
    NULL,
    NOW()
),
-- Alerta 4: Usuario
(
    'Nuevo Usuario Registrado',
    'Se ha registrado un nuevo usuario: María González',
    'USUARIO',
    'BAJA',
    'NO_LEIDA',
    'USUARIO',
    2,
    NOW()
),
-- Alerta 5: Sistema Resuelta
(
    'Mantenimiento Programado Completado',
    'El mantenimiento programado del servidor se completó exitosamente',
    'SISTEMA',
    'BAJA',
    'RESUELTA',
    'SISTEMA',
    NULL,
    NOW() - INTERVAL '2 hours'
);

-- Verificar que las alertas se insertaron correctamente
SELECT 
    id_alerta,
    titulo,
    tipo_alerta,
    nivel_prioridad,
    estado,
    fecha_creacion
FROM alertas 
ORDER BY fecha_creacion DESC;

-- Contar alertas por tipo y estado
SELECT 
    tipo_alerta,
    nivel_prioridad,
    estado,
    COUNT(*) as cantidad
FROM alertas 
GROUP BY tipo_alerta, nivel_prioridad, estado
ORDER BY tipo_alerta;
