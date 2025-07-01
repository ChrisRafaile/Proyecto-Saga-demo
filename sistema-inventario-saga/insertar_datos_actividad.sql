-- Script para insertar datos de actividad de prueba
-- Ejecutar en PostgreSQL

-- Insertar actividades de prueba
INSERT INTO actividades (
    accion, descripcion, entidad, id_entidad, fecha_actividad, tipo_actividad, nivel, direccion_ip, user_agent, id_usuario
) VALUES 
-- Actividades de login
('LOGIN', 'Inicio de sesión exitoso del administrador', 'Usuario', 1, NOW() - INTERVAL '2 hours', 'LOGIN', 'INFO', '192.168.1.100', 'Mozilla/5.0', 1),
('LOGIN', 'Inicio de sesión exitoso del empleado', 'Usuario', 2, NOW() - INTERVAL '1 hour 30 minutes', 'LOGIN', 'INFO', '192.168.1.105', 'Mozilla/5.0', 2),

-- Actividades CRUD
('CREAR_PRODUCTO', 'Creación de nuevo producto: Laptop Dell', 'Producto', 123, NOW() - INTERVAL '1 hour 15 minutes', 'CREATE', 'INFO', '192.168.1.100', 'Mozilla/5.0', 1),
('ACTUALIZAR_PRODUCTO', 'Actualización de precio de producto ID 123', 'Producto', 123, NOW() - INTERVAL '1 hour', 'UPDATE', 'INFO', '192.168.1.100', 'Mozilla/5.0', 1),
('CONSULTAR_PRODUCTOS', 'Consulta de lista de productos', 'Producto', NULL, NOW() - INTERVAL '50 minutes', 'READ', 'INFO', '192.168.1.105', 'Mozilla/5.0', 2),
('ELIMINAR_PRODUCTO', 'Eliminación de producto obsoleto', 'Producto', 456, NOW() - INTERVAL '45 minutes', 'DELETE', 'WARNING', '192.168.1.100', 'Mozilla/5.0', 1),

-- Actividades de pedidos
('CREAR_PEDIDO', 'Creación de nuevo pedido PED-001', 'Pedido', 1, NOW() - INTERVAL '40 minutes', 'CREATE', 'INFO', '192.168.1.110', 'Mozilla/5.0', 3),
('ACTUALIZAR_PEDIDO', 'Cambio de estado de pedido a "En proceso"', 'Pedido', 1, NOW() - INTERVAL '35 minutes', 'UPDATE', 'INFO', '192.168.1.105', 'Mozilla/5.0', 2),

-- Actividades de exportación
('EXPORTAR_EXCEL', 'Exportación de productos a Excel', 'Reporte', NULL, NOW() - INTERVAL '30 minutes', 'EXPORT', 'INFO', '192.168.1.100', 'Mozilla/5.0', 1),
('EXPORTAR_PDF', 'Exportación de pedidos a PDF', 'Reporte', NULL, NOW() - INTERVAL '25 minutes', 'EXPORT', 'INFO', '192.168.1.100', 'Mozilla/5.0', 1),

-- Actividades de sistema
('RESPALDO_BD', 'Respaldo automático de base de datos', 'Sistema', NULL, NOW() - INTERVAL '20 minutes', 'BACKUP', 'INFO', '127.0.0.1', 'Sistema/1.0', NULL),
('ERROR_CONEXION', 'Error temporal de conexión a base de datos', 'Sistema', NULL, NOW() - INTERVAL '15 minutes', 'SISTEMA', 'ERROR', '127.0.0.1', 'Sistema/1.0', NULL),

-- Actividades de configuración
('CONFIG_SISTEMA', 'Modificación de configuración de email', 'Configuracion', NULL, NOW() - INTERVAL '10 minutes', 'CONFIGURACION', 'INFO', '192.168.1.100', 'Mozilla/5.0', 1),
('LIMPIAR_LOGS', 'Limpieza de logs antiguos (30 días)', 'Sistema', NULL, NOW() - INTERVAL '5 minutes', 'DELETE', 'INFO', '192.168.1.100', 'Mozilla/5.0', 1),

-- Actividades recientes
('CONSULTAR_ACTIVIDAD', 'Acceso a página de actividad del sistema', 'Actividad', NULL, NOW() - INTERVAL '2 minutes', 'READ', 'INFO', '192.168.1.100', 'Mozilla/5.0', 1),
('LOGIN', 'Inicio de sesión reciente', 'Usuario', 1, NOW() - INTERVAL '1 minute', 'LOGIN', 'INFO', '192.168.1.100', 'Mozilla/5.0', 1);

-- Verificar que se insertaron los datos
SELECT COUNT(*) as total_actividades FROM actividades;

-- Mostrar las últimas 10 actividades
SELECT 
    id_actividad,
    accion,
    descripcion,
    entidad,
    fecha_actividad,
    tipo_actividad,
    nivel
FROM actividades 
ORDER BY fecha_actividad DESC 
LIMIT 10;
