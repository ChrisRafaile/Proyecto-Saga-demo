-- ============================================================================
-- SCRIPT DE ANÁLISIS Y MEJORAS PARA LA BASE DE DATOS SAGA FALABELLA
-- Sistema de Gestión de Inventario
-- Autor: Christopher Lincoln Rafaile Naupay
-- ============================================================================

-- PASO 1: Verificar estructura actual de las tablas
SELECT 
    schemaname,
    tablename,
    tableowner
FROM pg_tables 
WHERE schemaname = 'public' 
ORDER BY tablename;

-- PASO 2: Analizar columnas de cada tabla
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = 'public' 
ORDER BY table_name, ordinal_position;

-- PASO 3: Verificar constraints y llaves primarias
SELECT
    tc.table_name,
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_schema = 'public'
ORDER BY tc.table_name, tc.constraint_type;

-- PASO 4: Verificar índices existentes
SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- ============================================================================
-- MEJORAS SUGERIDAS PARA LA BASE DE DATOS
-- ============================================================================

-- MEJORA 1: Agregar campos de auditoría si no existen
-- Para tabla producto
DO $$
BEGIN
    -- Verificar y agregar fecha_creacion si no existe
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'producto' AND column_name = 'fecha_creacion'
    ) THEN
        ALTER TABLE producto ADD COLUMN fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    -- Verificar y agregar fecha_actualizacion si no existe
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'producto' AND column_name = 'fecha_actualizacion'
    ) THEN
        ALTER TABLE producto ADD COLUMN fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    -- Verificar y agregar campo activo si no existe
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'producto' AND column_name = 'activo'
    ) THEN
        ALTER TABLE producto ADD COLUMN activo BOOLEAN DEFAULT TRUE;
    END IF;
END $$;

-- MEJORA 2: Crear tabla movimientos_inventario si no existe
CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    tipo_movimiento VARCHAR(50) NOT NULL,
    cantidad INTEGER NOT NULL,
    stock_anterior INTEGER,
    stock_nuevo INTEGER,
    motivo VARCHAR(200),
    usuario_responsable VARCHAR(100),
    numero_documento VARCHAR(50),
    fecha_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (producto_id) REFERENCES producto(id)
);

-- MEJORA 3: Crear tabla ordenes_picking si no existe
CREATE TABLE IF NOT EXISTS ordenes_picking (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    codigo_orden VARCHAR(20) UNIQUE NOT NULL,
    estado VARCHAR(20) DEFAULT 'PENDIENTE',
    fecha_asignacion TIMESTAMP,
    fecha_inicio_picking TIMESTAMP,
    fecha_fin_picking TIMESTAMP,
    operario_asignado VARCHAR(100),
    observaciones VARCHAR(500),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pedido_id) REFERENCES pedido(id)
);

-- MEJORA 4: Crear tabla roles si no existe
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(200),
    activo BOOLEAN DEFAULT TRUE
);

-- MEJORA 5: Crear tabla usuario_roles para relación many-to-many
CREATE TABLE IF NOT EXISTS usuario_roles (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES usuario_sistema(id),
    FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- MEJORA 6: Agregar índices para optimizar consultas
-- Índices para tabla producto
CREATE INDEX IF NOT EXISTS idx_producto_codigo ON producto(codigo_producto);
CREATE INDEX IF NOT EXISTS idx_producto_categoria ON producto(categoria);
CREATE INDEX IF NOT EXISTS idx_producto_marca ON producto(marca);
CREATE INDEX IF NOT EXISTS idx_producto_stock_bajo ON producto(stock_actual) WHERE stock_actual <= stock_minimo;
CREATE INDEX IF NOT EXISTS idx_producto_activo ON producto(activo);

-- Índices para tabla pedido
CREATE INDEX IF NOT EXISTS idx_pedido_cliente ON pedido(cliente_id);
CREATE INDEX IF NOT EXISTS idx_pedido_fecha ON pedido(fecha_pedido);
CREATE INDEX IF NOT EXISTS idx_pedido_estado ON pedido(estado);

-- Índices para tabla movimientos_inventario
CREATE INDEX IF NOT EXISTS idx_movimiento_producto ON movimientos_inventario(producto_id);
CREATE INDEX IF NOT EXISTS idx_movimiento_fecha ON movimientos_inventario(fecha_movimiento);
CREATE INDEX IF NOT EXISTS idx_movimiento_tipo ON movimientos_inventario(tipo_movimiento);

-- MEJORA 7: Crear triggers para actualizar fecha_actualizacion
CREATE OR REPLACE FUNCTION update_fecha_actualizacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Aplicar trigger a producto
DROP TRIGGER IF EXISTS tr_producto_update ON producto;
CREATE TRIGGER tr_producto_update 
    BEFORE UPDATE ON producto 
    FOR EACH ROW 
    EXECUTE PROCEDURE update_fecha_actualizacion();

-- MEJORA 8: Insertar roles básicos del sistema
INSERT INTO roles (nombre, descripcion) VALUES 
    ('ADMIN', 'Administrador del sistema con acceso completo'),
    ('SUPERVISOR_ALMACEN', 'Supervisor de almacén con permisos de gestión'),
    ('OPERARIO_LOGISTICO', 'Operario logístico para picking y empaque'),
    ('ATENCION_CLIENTE', 'Personal de atención al cliente'),
    ('COMPRADOR_CORPORATIVO', 'Comprador corporativo para gestión de proveedores')
ON CONFLICT (nombre) DO NOTHING;

-- MEJORA 9: Crear vistas útiles para reportes
CREATE OR REPLACE VIEW vista_productos_stock_bajo AS
SELECT 
    p.id,
    p.codigo_producto,
    p.nombre,
    p.stock_actual,
    p.stock_minimo,
    p.categoria,
    p.marca,
    prov.razon_social as proveedor
FROM producto p
LEFT JOIN proveedor prov ON p.proveedor_id = prov.id
WHERE p.stock_actual <= p.stock_minimo 
AND p.activo = TRUE;

CREATE OR REPLACE VIEW vista_movimientos_resumen AS
SELECT 
    DATE(mi.fecha_movimiento) as fecha,
    p.categoria,
    mi.tipo_movimiento,
    COUNT(*) as cantidad_movimientos,
    SUM(mi.cantidad) as total_productos
FROM movimientos_inventario mi
JOIN producto p ON mi.producto_id = p.id
GROUP BY DATE(mi.fecha_movimiento), p.categoria, mi.tipo_movimiento
ORDER BY fecha DESC;

-- MEJORA 10: Crear función para generar código de pedido automático
CREATE OR REPLACE FUNCTION generar_codigo_pedido()
RETURNS TEXT AS $$
DECLARE
    nuevo_codigo TEXT;
    contador INTEGER;
BEGIN
    -- Obtener el próximo número secuencial
    SELECT COALESCE(MAX(CAST(SUBSTRING(numero_pedido FROM 'PED-(.*)') AS INTEGER)), 0) + 1
    INTO contador
    FROM pedido 
    WHERE numero_pedido LIKE 'PED-%';
    
    -- Generar el código con formato PED-000001
    nuevo_codigo := 'PED-' || LPAD(contador::TEXT, 6, '0');
    
    RETURN nuevo_codigo;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- CONSULTAS DE VERIFICACIÓN
-- ============================================================================

-- Verificar estructura final
SELECT 'Tablas creadas:' as info;
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;

SELECT 'Índices creados:' as info;
SELECT indexname FROM pg_indexes WHERE schemaname = 'public' AND indexname LIKE 'idx_%';

SELECT 'Roles insertados:' as info;
SELECT nombre, descripcion FROM roles;

-- Estadísticas básicas
SELECT 'Estadísticas de la base de datos:' as info;
SELECT 
    (SELECT COUNT(*) FROM producto WHERE activo = TRUE) as productos_activos,
    (SELECT COUNT(*) FROM cliente) as total_clientes,
    (SELECT COUNT(*) FROM pedido) as total_pedidos,
    (SELECT COUNT(*) FROM proveedor) as total_proveedores;
