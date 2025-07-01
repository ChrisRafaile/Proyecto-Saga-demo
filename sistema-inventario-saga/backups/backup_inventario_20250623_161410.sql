-- Respaldo de Base de Datos Sistema Inventario Saga Falabella
-- Fecha: 23/06/2025 16:14:10
-- Archivo: backup_inventario_20250623_161410.sql

-- Estructura y datos de tabla productos
CREATE TABLE IF NOT EXISTS producto (
    idproducto SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    categoria VARCHAR(50),
    precio DECIMAL(10,2) NOT NULL,
    fechavencimiento DATE,
    activo BOOLEAN DEFAULT true,
    stock_actual INTEGER DEFAULT 0,
    stock_minimo INTEGER DEFAULT 5,
    marca VARCHAR(50),
    ubicacion_almacen VARCHAR(100),
    codigo_producto VARCHAR(50) UNIQUE
);

-- Estructura y datos de tabla proveedores
CREATE TABLE IF NOT EXISTS proveedores (
    id SERIAL PRIMARY KEY,
    ruc VARCHAR(11) UNIQUE NOT NULL,
    razon_social VARCHAR(200) NOT NULL,
    nombre_comercial VARCHAR(200),
    email VARCHAR(100),
    telefono VARCHAR(20),
    direccion VARCHAR(500),
    contacto_principal VARCHAR(100),
    condiciones_pago VARCHAR(200),
    calificacion INTEGER CHECK (calificacion >= 1 AND calificacion <= 5),
    activo BOOLEAN DEFAULT true,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Fin del respaldo
