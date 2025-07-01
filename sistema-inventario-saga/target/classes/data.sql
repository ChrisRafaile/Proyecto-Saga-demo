-- Datos de muestra para el sistema de inventario Saga Falabella
-- SOLO USUARIOS PARA TESTING - Simplificado para solucionar problema de login

-- Insertar usuarios de prueba con diferentes roles
-- Contraseña para todos: "admin123" (hash BCrypt válido)

-- Super Administrador
INSERT INTO usuario_sistema (username, email, password, nombre, apellido, rol, tipo_usuario, activo, fecha_creacion) VALUES
('admin', 'admin@sagafalabella.com', '$2a$10$pahDQoVikfbl3Vvlq8yVOOhjBl2R0sbM4iPKNKjG2R5kTiFTpws8C', 'Administrador', 'Principal', 'SUPER_ADMIN', 'INTERNO', true, CURRENT_TIMESTAMP);

-- Administrador de Inventario
INSERT INTO usuario_sistema (username, email, password, nombre, apellido, rol, tipo_usuario, activo, fecha_creacion) VALUES
('admin_inventario', 'inventario@sagafalabella.com', '$2a$10$pahDQoVikfbl3Vvlq8yVOOhjBl2R0sbM4iPKNKjG2R5kTiFTpws8C', 'María', 'González Ruiz', 'ADMIN_INVENTARIO', 'INTERNO', true, CURRENT_TIMESTAMP);

-- Administrador de Ventas
INSERT INTO usuario_sistema (username, email, password, nombre, apellido, rol, tipo_usuario, activo, fecha_creacion) VALUES
('admin_ventas', 'ventas@sagafalabella.com', '$2a$10$pahDQoVikfbl3Vvlq8yVOOhjBl2R0sbM4iPKNKjG2R5kTiFTpws8C', 'Carlos', 'Mendoza López', 'ADMIN_VENTAS', 'INTERNO', true, CURRENT_TIMESTAMP);

-- Empleado de Almacén
INSERT INTO usuario_sistema (username, email, password, nombre, apellido, rol, tipo_usuario, activo, fecha_creacion) VALUES
('empleado_almacen', 'almacen@sagafalabella.com', '$2a$10$pahDQoVikfbl3Vvlq8yVOOhjBl2R0sbM4iPKNKjG2R5kTiFTpws8C', 'Ana', 'Torres Vega', 'EMPLEADO_ALMACEN', 'INTERNO', true, CURRENT_TIMESTAMP);

-- Empleado de Ventas
INSERT INTO usuario_sistema (username, email, password, nombre, apellido, rol, tipo_usuario, activo, fecha_creacion) VALUES
('empleado_ventas', 'vendedor@sagafalabella.com', '$2a$10$pahDQoVikfbl3Vvlq8yVOOhjBl2R0sbM4iPKNKjG2R5kTiFTpws8C', 'Luis', 'Ramírez Castro', 'EMPLEADO_VENTAS', 'INTERNO', true, CURRENT_TIMESTAMP);

-- Cliente de prueba
INSERT INTO usuario_sistema (username, email, password, nombre, apellido, rol, tipo_usuario, activo, fecha_creacion) VALUES
('cliente_demo', 'cliente@email.com', '$2a$10$pahDQoVikfbl3Vvlq8yVOOhjBl2R0sbM4iPKNKjG2R5kTiFTpws8C', 'Juan', 'Pérez Morales', 'CLIENTE', 'EXTERNO', true, CURRENT_TIMESTAMP);
