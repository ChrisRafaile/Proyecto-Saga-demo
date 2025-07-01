-- Script para insertar alertas de ejemplo y diagnosticar problemas
-- Usar en la consola H2 o base de datos

-- Limpiar alertas existentes
DELETE FROM alertas WHERE 1=1;

-- Insertar alertas de ejemplo
INSERT INTO alertas (
    titulo, descripcion, tipo_alerta, nivel_prioridad, estado,
    fecha_creacion, entidad_relacionada, id_entidad_relacionada,
    activo
) VALUES 
-- Alerta crítica de stock bajo
('Stock Crítico - Laptop HP Pavilion', 
 'El producto Laptop HP Pavilion tiene stock crítico (2 unidades restantes)', 
 'STOCK_BAJO', 'CRITICA', 'NO_LEIDA', 
 CURRENT_TIMESTAMP, 'PRODUCTO', 1, true),

-- Alerta de seguridad
('Intento de Acceso No Autorizado', 
 'Se detectó un intento de acceso no autorizado desde IP: 192.168.1.100', 
 'SEGURIDAD', 'ALTA', 'NO_LEIDA', 
 CURRENT_TIMESTAMP, 'SISTEMA', NULL, true),

-- Alerta de sistema
('Backup Automático Fallido', 
 'El backup automático programado para las 2:00 AM ha fallado', 
 'SISTEMA', 'MEDIA', 'NO_LEIDA', 
 CURRENT_TIMESTAMP, 'SISTEMA', NULL, true),

-- Alerta de usuario
('Nuevo Usuario Registrado', 
 'Se ha registrado un nuevo usuario: María González', 
 'USUARIO', 'BAJA', 'NO_LEIDA', 
 CURRENT_TIMESTAMP, 'USUARIO', 2, true),

-- Alerta resuelta para testing
('Mantenimiento Programado Completado', 
 'El mantenimiento programado del servidor se completó exitosamente', 
 'SISTEMA', 'BAJA', 'RESUELTA', 
 DATEADD('HOUR', -2, CURRENT_TIMESTAMP), 'SISTEMA', NULL, true);

-- Verificar que se insertaron
SELECT 
    id_alerta, titulo, tipo_alerta, nivel_prioridad, estado, fecha_creacion
FROM alertas 
ORDER BY fecha_creacion DESC;
