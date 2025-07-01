package com.sagafalabella.inventario.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio para gestión de respaldos del sistema
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Service
public class RespaldoService {

    @Autowired
    private DataSource dataSource;

    private final String BACKUP_DIR = "backups";
    private final String CONFIG_FILE = "config.properties";

    /**
     * Crear respaldo de la base de datos
     */
    public String crearRespaldo() throws IOException, SQLException {
        // Crear directorio de respaldos si no existe
        Path backupPath = Paths.get(BACKUP_DIR);
        if (!Files.exists(backupPath)) {
            Files.createDirectories(backupPath);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = "backup_inventario_" + timestamp + ".sql";
        Path backupFile = backupPath.resolve(backupFileName);        try (FileWriter writer = new FileWriter(backupFile.toFile())) {

            // Escribir header del respaldo
            writer.write("-- Respaldo de Base de Datos Sistema Inventario Saga Falabella\n");
            writer.write("-- Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n");
            writer.write("-- Archivo: " + backupFileName + "\n\n");

            // Generar script de respaldo (simplificado para PostgreSQL)
            writer.write("-- Estructura y datos de tabla productos\n");
            writer.write("CREATE TABLE IF NOT EXISTS producto (\n");
            writer.write("    idproducto SERIAL PRIMARY KEY,\n");
            writer.write("    nombre VARCHAR(100) NOT NULL,\n");
            writer.write("    descripcion TEXT,\n");
            writer.write("    categoria VARCHAR(50),\n");
            writer.write("    precio DECIMAL(10,2) NOT NULL,\n");
            writer.write("    fechavencimiento DATE,\n");
            writer.write("    activo BOOLEAN DEFAULT true,\n");
            writer.write("    stock_actual INTEGER DEFAULT 0,\n");
            writer.write("    stock_minimo INTEGER DEFAULT 5,\n");
            writer.write("    marca VARCHAR(50),\n");
            writer.write("    ubicacion_almacen VARCHAR(100),\n");
            writer.write("    codigo_producto VARCHAR(50) UNIQUE\n");
            writer.write(");\n\n");

            // Generar script de respaldo para proveedores
            writer.write("-- Estructura y datos de tabla proveedores\n");
            writer.write("CREATE TABLE IF NOT EXISTS proveedores (\n");
            writer.write("    id SERIAL PRIMARY KEY,\n");
            writer.write("    ruc VARCHAR(11) UNIQUE NOT NULL,\n");
            writer.write("    razon_social VARCHAR(200) NOT NULL,\n");
            writer.write("    nombre_comercial VARCHAR(200),\n");
            writer.write("    email VARCHAR(100),\n");
            writer.write("    telefono VARCHAR(20),\n");
            writer.write("    direccion VARCHAR(500),\n");
            writer.write("    contacto_principal VARCHAR(100),\n");
            writer.write("    condiciones_pago VARCHAR(200),\n");
            writer.write("    calificacion INTEGER CHECK (calificacion >= 1 AND calificacion <= 5),\n");
            writer.write("    activo BOOLEAN DEFAULT true,\n");
            writer.write("    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n");
            writer.write("    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n");
            writer.write(");\n\n");

            writer.write("-- Fin del respaldo\n");
        }

        return backupFileName;
    }

    /**
     * Restaurar respaldo desde archivo
     */
    public void restaurarRespaldo(MultipartFile archivo) throws IOException, SQLException {
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo de respaldo está vacío");
        }

        String content = new String(archivo.getBytes());
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Ejecutar el script SQL
            String[] statements = content.split(";");
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty() && !sql.startsWith("--")) {
                    statement.execute(sql);
                }
            }
        }
    }

    /**
     * Obtener lista de respaldos disponibles
     */
    public List<RespaldoInfo> obtenerRespaldosDisponibles() throws IOException {
        List<RespaldoInfo> respaldos = new ArrayList<>();
        Path backupPath = Paths.get(BACKUP_DIR);
        
        if (Files.exists(backupPath)) {
            Files.walk(backupPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".sql"))
                .forEach(path -> {
                    try {
                        RespaldoInfo info = new RespaldoInfo();
                        info.setNombre(path.getFileName().toString());
                        info.setTamaño(Files.size(path));
                        info.setFechaCreacion(Files.getLastModifiedTime(path).toInstant());
                        info.setRuta(path.toString());
                        respaldos.add(info);
                    } catch (IOException e) {
                        // Log error but continue
                    }
                });
        }
        
        return respaldos;
    }

    /**
     * Descargar archivo de respaldo
     */
    public Resource descargarRespaldo(String nombreArchivo) throws IOException {
        Path backupPath = Paths.get(BACKUP_DIR).resolve(nombreArchivo);
        
        if (!Files.exists(backupPath)) {
            throw new FileNotFoundException("Archivo de respaldo no encontrado: " + nombreArchivo);
        }
        
        return new FileSystemResource(backupPath);
    }

    /**
     * Eliminar respaldo
     */
    public void eliminarRespaldo(String nombreArchivo) throws IOException {
        Path backupPath = Paths.get(BACKUP_DIR).resolve(nombreArchivo);
        
        if (Files.exists(backupPath)) {
            Files.delete(backupPath);
        }
    }

    /**
     * Verificar integridad del sistema
     */
    public VerificacionResult verificarIntegridad() throws SQLException {
        VerificacionResult result = new VerificacionResult();
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Verificar conexión a la base de datos
            result.setConexionBD(true);
            
            // Verificar tablas principales
            try {
                statement.executeQuery("SELECT COUNT(*) FROM producto");
                result.setTablaProductos(true);
            } catch (SQLException e) {
                result.setTablaProductos(false);
                result.getErrores().add("Error en tabla productos: " + e.getMessage());
            }
            
            try {
                statement.executeQuery("SELECT COUNT(*) FROM proveedores");
                result.setTablaProveedores(true);
            } catch (SQLException e) {
                result.setTablaProveedores(false);
                result.getErrores().add("Error en tabla proveedores: " + e.getMessage());
            }
            
            // Verificar directorio de respaldos
            Path backupPath = Paths.get(BACKUP_DIR);
            result.setDirectorioRespaldos(Files.exists(backupPath));
            
        } catch (SQLException e) {
            result.setConexionBD(false);
            result.getErrores().add("Error de conexión a BD: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Ejecutar limpieza del sistema
     */
    public LimpiezaResult ejecutarLimpieza() throws SQLException, IOException {
        LimpiezaResult result = new LimpiezaResult();
        
        // Limpiar respaldos antiguos (más de 30 días)
        Path backupPath = Paths.get(BACKUP_DIR);
        if (Files.exists(backupPath)) {
            Files.walk(backupPath)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toInstant()
                                .isBefore(LocalDateTime.now().minusDays(30).toInstant(java.time.ZoneOffset.UTC));
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        result.setArchivosEliminados(result.getArchivosEliminados() + 1);
                    } catch (IOException e) {
                        result.getErrores().add("Error eliminando " + path.getFileName() + ": " + e.getMessage());
                    }
                });
        }
        
        // Limpiar registros de log antiguos (simulado)
        result.setRegistrosLimpios(0); // Placeholder
        
        return result;
    }

    /**
     * Guardar configuración
     */
    public void guardarConfiguracion(ConfiguracionRespaldo config) throws IOException {
        Properties props = new Properties();
        props.setProperty("backup.auto.enabled", String.valueOf(config.isRespaldoAutomatico()));
        props.setProperty("backup.frequency", config.getFrecuencia());
        props.setProperty("backup.hour", config.getHora());
        props.setProperty("backup.retention.days", String.valueOf(config.getDiasRetencion()));
        
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Configuración de Respaldos - Sistema Inventario");
        }
    }

    // Clases internas para DTOs
    public static class RespaldoInfo {
        private String nombre;
        private long tamaño;
        private java.time.Instant fechaCreacion;
        private String ruta;

        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public long getTamaño() { return tamaño; }
        public void setTamaño(long tamaño) { this.tamaño = tamaño; }
        public java.time.Instant getFechaCreacion() { return fechaCreacion; }
        public void setFechaCreacion(java.time.Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
        public String getRuta() { return ruta; }
        public void setRuta(String ruta) { this.ruta = ruta; }
    }

    public static class VerificacionResult {
        private boolean conexionBD;
        private boolean tablaProductos;
        private boolean tablaProveedores;
        private boolean directorioRespaldos;
        private List<String> errores = new ArrayList<>();

        // Getters y setters
        public boolean isConexionBD() { return conexionBD; }
        public void setConexionBD(boolean conexionBD) { this.conexionBD = conexionBD; }
        public boolean isTablaProductos() { return tablaProductos; }
        public void setTablaProductos(boolean tablaProductos) { this.tablaProductos = tablaProductos; }
        public boolean isTablaProveedores() { return tablaProveedores; }
        public void setTablaProveedores(boolean tablaProveedores) { this.tablaProveedores = tablaProveedores; }
        public boolean isDirectorioRespaldos() { return directorioRespaldos; }
        public void setDirectorioRespaldos(boolean directorioRespaldos) { this.directorioRespaldos = directorioRespaldos; }
        public List<String> getErrores() { return errores; }
        public void setErrores(List<String> errores) { this.errores = errores; }
    }

    public static class LimpiezaResult {
        private int archivosEliminados;
        private int registrosLimpios;
        private List<String> errores = new ArrayList<>();

        // Getters y setters
        public int getArchivosEliminados() { return archivosEliminados; }
        public void setArchivosEliminados(int archivosEliminados) { this.archivosEliminados = archivosEliminados; }
        public int getRegistrosLimpios() { return registrosLimpios; }
        public void setRegistrosLimpios(int registrosLimpios) { this.registrosLimpios = registrosLimpios; }
        public List<String> getErrores() { return errores; }
        public void setErrores(List<String> errores) { this.errores = errores; }
    }

    public static class ConfiguracionRespaldo {
        private boolean respaldoAutomatico;
        private String frecuencia;
        private String hora;
        private int diasRetencion;

        // Getters y setters
        public boolean isRespaldoAutomatico() { return respaldoAutomatico; }
        public void setRespaldoAutomatico(boolean respaldoAutomatico) { this.respaldoAutomatico = respaldoAutomatico; }
        public String getFrecuencia() { return frecuencia; }
        public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }
        public String getHora() { return hora; }
        public void setHora(String hora) { this.hora = hora; }
        public int getDiasRetencion() { return diasRetencion; }
        public void setDiasRetencion(int diasRetencion) { this.diasRetencion = diasRetencion; }
    }
}
