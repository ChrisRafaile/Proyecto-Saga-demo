-- Script SQL para insertar alertas de ejemplo en el sistema
-- Ejecutar en la base de datos PostgreSQL

-- Insertar alertas de ejemplo para probar el sistema
INSERT INTO alertas (
    titulo, 
    descripcion, 
    tipo_alerta, 
    nivel_prioridad, 
    estado, 
    fecha_creacion, 
    notificacion_enviada,
    entidad_relacionada
) VALUES 
-- Alerta crítica de stock bajo
(
    'Stock Crítico: Laptop HP Pavilion', 
    'Solo quedan 2 unidades en inventario del modelo HP Pavilion 15-eh1500la. Se requiere reabastecimiento urgente.', 
    'STOCK_BAJO', 
    'CRITICA', 
    'NO_LEIDA', 
    NOW(), 
    false,
    'Producto'
),
-- Alerta de nuevo usuario
(
    'Nuevo Usuario Registrado', 
    'Se ha registrado un nuevo usuario en el sistema: maria.gonzalez@sagafalabella.com', 
    'USUARIO', 
    'MEDIA', 
    'NO_LEIDA', 
    NOW() - INTERVAL '2 hours', 
    false,
    'Usuario'
),
-- Alerta crítica del sistema
(
    'Error de Conexión a Base de Datos', 
    'Se detectaron errores intermitentes en la conexión a la base de datos principal. Sistema de respaldo activado.', 
    'SISTEMA', 
    'CRITICA', 
    'EN_PROCESO', 
    NOW() - INTERVAL '1 hour', 
    true,
    'Sistema'
),
-- Alerta de pedido urgente
(
    'Pedido Urgente Pendiente de Aprobación', 
    'El pedido #PED-2024-1001 por valor de $15,000 requiere aprobación inmediata del gerente.', 
    'PEDIDO', 
    'ALTA', 
    'NO_LEIDA', 
    NOW() - INTERVAL '30 minutes', 
    false,
    'Pedido'
),
-- Alerta de proveedor (ya leída)
(
    'Nuevo Proveedor Registrado', 
    'Se ha registrado exitosamente el proveedor TechSupply SA con RUC 20123456789', 
    'PROVEEDOR', 
    'MEDIA', 
    'LEIDA', 
    NOW() - INTERVAL '3 hours', 
    false,
    'Proveedor'
),
-- Alerta crítica de seguridad
(
    'Múltiples Intentos de Acceso Fallidos', 
    'Se detectaron 15 intentos de login fallidos desde la IP 192.168.1.100 en los últimos 10 minutos', 
    'SEGURIDAD', 
    'CRITICA', 
    'NO_LEIDA', 
    NOW() - INTERVAL '5 minutes', 
    true,
    'Seguridad'
),
-- Alerta de producto con stock medio
(
    'Stock Bajo: Mouse Logitech MX Master', 
    'Quedan 8 unidades en inventario del Mouse Logitech MX Master 3. Nivel por debajo del mínimo recomendado.', 
    'STOCK_BAJO', 
    'MEDIA', 
    'LEIDA', 
    NOW() - INTERVAL '4 hours', 
    false,
    'Producto'
),
-- Alerta de mantenimiento
(
    'Mantenimiento Programado del Servidor', 
    'Se ha programado mantenimiento del servidor principal para el próximo domingo de 2:00 AM a 6:00 AM', 
    'SISTEMA', 
    'MEDIA', 
    'NO_LEIDA', 
    NOW() - INTERVAL '1 day', 
    false,
    'Sistema'
),
-- Alerta resuelta
(
    'Problema de Conectividad Resuelto', 
    'El problema de conectividad en la sucursal Norte ha sido resuelto exitosamente.', 
    'SISTEMA', 
    'ALTA', 
    'RESUELTA', 
    NOW() - INTERVAL '6 hours', 
    true,
    'Sistema'
),
-- Alerta de inventario
(
    'Actualización Masiva de Inventario', 
    'Se completó la actualización masiva de inventario. 1,247 productos fueron actualizados correctamente.', 
    'SISTEMA', 
    'MEDIA', 
    'RESUELTA', 
    NOW() - INTERVAL '2 days', 
    false,
    'Sistema'
);

-- Verificar que las alertas se insertaron correctamente
SELECT 
    titulo,
    tipo_alerta,
    nivel_prioridad,
    estado,
    fecha_creacion
FROM alertas 
ORDER BY fecha_creacion DESC;
