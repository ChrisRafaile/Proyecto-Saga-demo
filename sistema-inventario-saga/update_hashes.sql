-- Script para actualizar hashes de contraseñas
-- Ejecutar este script en PostgreSQL para corregir los hashes

-- Actualizar hash para admin (contraseña: admin123)
UPDATE usuario_sistema 
SET password = '$2a$10$Vv3FI5tyRzH6HMi3Ig/ZPuyFrm4lmvHTqxEv/Cc.4Y4nCPo93pPS6'
WHERE username = 'admin';

-- Actualizar hash para cliente_demo (contraseña: cliente123)
UPDATE usuario_sistema 
SET password = '$2a$10$3MUjyvzIBmTM8haHs3.lO.vgTV8WalIFbLg1TI5tKye2Lvgkb4ORy'
WHERE username = 'cliente_demo';

-- Verificar los cambios
SELECT username, email, rol, 
       CASE 
           WHEN password = '$2a$10$Vv3FI5tyRzH6HMi3Ig/ZPuyFrm4lmvHTqxEv/Cc.4Y4nCPo93pPS6' THEN 'Hash Admin Correcto'
           WHEN password = '$2a$10$3MUjyvzIBmTM8haHs3.lO.vgTV8WalIFbLg1TI5tKye2Lvgkb4ORy' THEN 'Hash Cliente Correcto'
           ELSE 'Hash Incorrecto'
       END as estado_hash
FROM usuario_sistema 
WHERE username IN ('admin', 'cliente_demo');
