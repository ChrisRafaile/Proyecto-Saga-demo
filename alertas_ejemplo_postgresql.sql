-- Script SQL simple para insertar alertas en PostgreSQL
-- Ejecutar en pgAdmin o consola de PostgreSQL

-- Verificar conexión
SELECT current_database(), current_user;

-- Limpiar alertas existentes (opcional)
-- DELETE FROM alertas;

-- Insertar alertas de ejemplo
INSERT INTO alertas (
    titulo, descripcion, tipo_alerta, nivel_prioridad, estado,
    fecha_creacion, entidad_relacionada, id_entidad_relacionada,
    activo
) VALUES 
('Stock Crítico - Laptop HP Pavilion', 
 'El producto Laptop HP Pavilion tiene stock crítico (2 unidades restantes)', 
 'STOCK_BAJO', 'CRITICA', 'NO_LEIDA', 
 NOW(), 'PRODUCTO', 1, true),

('Intento de Acceso No Autorizado', 
 'Se detectó un intento de acceso no autorizado desde IP: 192.168.1.100', 
 'SEGURIDAD', 'ALTA', 'NO_LEIDA', 
 NOW(), 'SISTEMA', NULL, true),

('Backup Automático Fallido', 
 'El backup automático programado para las 2:00 AM ha fallado', 
 'SISTEMA', 'MEDIA', 'NO_LEIDA', 
 NOW(), 'SISTEMA', NULL, true),

('Nuevo Usuario Registrado', 
 'Se ha registrado un nuevo usuario: María González', 
 'USUARIO', 'BAJA', 'NO_LEIDA', 
 NOW(), 'USUARIO', 2, true),

('Mantenimiento Programado Completado', 
 'El mantenimiento programado del servidor se completó exitosamente', 
 'SISTEMA', 'BAJA', 'RESUELTA', 
 NOW() - INTERVAL '2 HOURS', 'SISTEMA', NULL, true);

-- Verificar que se insertaron
SELECT 
    id_alerta, titulo, tipo_alerta, nivel_prioridad, estado, fecha_creacion
FROM alertas 
ORDER BY fecha_creacion DESC;
