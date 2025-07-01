-- Insertar alertas de ejemplo directamente en la base de datos
-- Verificar primero si ya existen alertas
SELECT COUNT(*) as alertas_existentes FROM alertas;

-- Limpiar alertas existentes si es necesario (opcional)
-- DELETE FROM alertas WHERE titulo LIKE 'Stock Crítico%' OR titulo LIKE 'Intento de Acceso%' OR titulo LIKE 'Backup%' OR titulo LIKE 'Nuevo Usuario%' OR titulo LIKE 'Mantenimiento%';

-- Insertar alertas de ejemplo
INSERT INTO alertas (
    titulo, 
    descripcion, 
    tipo_alerta, 
    nivel_prioridad, 
    estado, 
    entidad_relacionada, 
    id_entidad_relacionada, 
    fecha_creacion, 
    fecha_actualizacion, 
    leida
) VALUES 
(
    'Stock Crítico - Laptop HP Pavilion',
    'El producto Laptop HP Pavilion tiene stock crítico (2 unidades restantes)',
    'STOCK_BAJO',
    'CRITICA',
    'PENDIENTE',
    'PRODUCTO',
    1,
    NOW() - INTERVAL '30 minutes',
    NOW() - INTERVAL '30 minutes',
    false
),
(
    'Intento de Acceso No Autorizado',
    'Se detectó un intento de acceso no autorizado desde IP: 192.168.1.100',
    'SEGURIDAD',
    'ALTA',
    'PENDIENTE',
    'SISTEMA',
    NULL,
    NOW() - INTERVAL '45 minutes',
    NOW() - INTERVAL '45 minutes',
    false
),
(
    'Backup Automático Fallido',
    'El backup automático programado para las 2:00 AM ha fallado',
    'SISTEMA',
    'MEDIA',
    'PENDIENTE',
    'SISTEMA',
    NULL,
    NOW() - INTERVAL '1 hour',
    NOW() - INTERVAL '1 hour',
    false
),
(
    'Nuevo Usuario Registrado',
    'Se ha registrado un nuevo usuario: María González',
    'USUARIO',
    'BAJA',
    'PENDIENTE',
    'USUARIO',
    2,
    NOW() - INTERVAL '2 hours',
    NOW() - INTERVAL '2 hours',
    false
),
(
    'Mantenimiento Programado Completado',
    'El mantenimiento programado del servidor se completó exitosamente',
    'SISTEMA',
    'BAJA',
    'RESUELTA',
    'SISTEMA',
    NULL,
    NOW() - INTERVAL '3 hours',
    NOW() - INTERVAL '3 hours',
    true
),
(
    'Stock Bajo - Mouse Logitech',
    'El producto Mouse Logitech tiene stock bajo (5 unidades restantes)',
    'STOCK_BAJO',
    'MEDIA',
    'PENDIENTE',
    'PRODUCTO',
    3,
    NOW() - INTERVAL '20 minutes',
    NOW() - INTERVAL '20 minutes',
    false
),
(
    'Producto Sin Stock - Teclado Mecánico',
    'El producto Teclado Mecánico se encuentra sin stock disponible',
    'STOCK_BAJO',
    'ALTA',
    'PENDIENTE',
    'PRODUCTO',
    4,
    NOW() - INTERVAL '10 minutes',
    NOW() - INTERVAL '10 minutes',
    false
);

-- Verificar que las alertas se insertaron correctamente
SELECT 
    id,
    titulo,
    tipo_alerta,
    nivel_prioridad,
    estado,
    leida,
    fecha_creacion
FROM alertas 
ORDER BY fecha_creacion DESC;

-- Estadísticas de alertas
SELECT 
    tipo_alerta,
    nivel_prioridad,
    estado,
    COUNT(*) as cantidad
FROM alertas 
GROUP BY tipo_alerta, nivel_prioridad, estado 
ORDER BY tipo_alerta, nivel_prioridad;
