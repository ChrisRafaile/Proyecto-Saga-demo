-- Verificar usuarios en la base de datos
SELECT username, email, rol, activo, 
       SUBSTRING(password, 1, 20) as password_hash_inicio
FROM usuario_sistema
WHERE username IN ('admin', 'cliente_demo')
ORDER BY username;
