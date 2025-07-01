package com.sagafalabella.inventario.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad para registrar todas las actividades del sistema
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Entity
@Table(name = "actividades")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad")
    private Long idActividad;    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "accion", nullable = false, length = 100)
    @NotBlank(message = "La acción es obligatoria")
    private String accion;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "entidad", length = 50)
    private String entidad;

    @Column(name = "id_entidad")
    private Long idEntidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_actividad", nullable = false)
    @NotNull(message = "El tipo de actividad es obligatorio")
    private TipoActividad tipoActividad;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false)
    @NotNull(message = "El nivel es obligatorio")
    private NivelActividad nivel;

    @Column(name = "direccion_ip", length = 45)
    private String direccionIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "valores_anteriores", columnDefinition = "TEXT")
    private String valoresAnteriores;

    @Column(name = "valores_nuevos", columnDefinition = "TEXT")
    private String valoresNuevos;

    @Column(name = "resultado", length = 20)
    private String resultado; // SUCCESS, ERROR, WARNING

    @Column(name = "mensaje_error", length = 1000)
    private String mensajeError;

    @Column(name = "fecha_actividad", nullable = false)
    private LocalDateTime fechaActividad;

    @Column(name = "duracion_ms")
    private Long duracionMs;

    // Enums
    public enum TipoActividad {
        LOGIN("Inicio de Sesión"),
        LOGOUT("Cierre de Sesión"),
        CREATE("Crear"),
        READ("Consultar"),
        UPDATE("Actualizar"),
        DELETE("Eliminar"),
        EXPORT("Exportar"),
        IMPORT("Importar"),
        BACKUP("Respaldo"),
        RESTORE("Restaurar"),
        SISTEMA("Sistema"),
        AUTENTICACION("Autenticación"),
        CONFIGURACION("Configuración");

        private final String descripcion;

        TipoActividad(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    public enum NivelActividad {
        INFO("Información"),
        WARNING("Advertencia"),
        ERROR("Error"),
        DEBUG("Debug"),
        TRACE("Trace");

        private final String descripcion;

        NivelActividad(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    // Lifecycle methods
    @PrePersist
    protected void onCreate() {
        if (this.fechaActividad == null) {
            this.fechaActividad = LocalDateTime.now();
        }
        if (this.nivel == null) {
            this.nivel = NivelActividad.INFO;
        }
        if (this.resultado == null) {
            this.resultado = "SUCCESS";
        }
    }

    // Métodos de utilidad
    public boolean isError() {
        return NivelActividad.ERROR.equals(this.nivel) || "ERROR".equals(this.resultado);
    }

    public boolean isWarning() {
        return NivelActividad.WARNING.equals(this.nivel) || "WARNING".equals(this.resultado);
    }

    public boolean isSuccess() {
        return "SUCCESS".equals(this.resultado);
    }

    public String getNombreUsuario() {
        return this.usuario != null ? this.usuario.getNombreCompleto() : "Sistema";
    }

    public String getUsuarioEmail() {
        return this.usuario != null ? this.usuario.getEmail() : "sistema@sagafalabella.com";
    }

    // Constructors
    public Actividad() {}

    public Actividad(String accion, String descripcion, TipoActividad tipoActividad, NivelActividad nivel) {
        this.accion = accion;
        this.descripcion = descripcion;
        this.tipoActividad = tipoActividad;
        this.nivel = nivel;
        this.fechaActividad = LocalDateTime.now();
    }

    public Actividad(Usuario usuario, String accion, String descripcion, TipoActividad tipoActividad, NivelActividad nivel) {
        this.usuario = usuario;
        this.accion = accion;
        this.descripcion = descripcion;
        this.tipoActividad = tipoActividad;
        this.nivel = nivel;
        this.fechaActividad = LocalDateTime.now();
    }

    // Factory methods para crear actividades comunes
    public static Actividad crearActividad(Usuario usuario, String accion, String descripcion, String entidad, Long idEntidad) {
        Actividad actividad = new Actividad();
        actividad.setUsuario(usuario);
        actividad.setAccion(accion);
        actividad.setDescripcion(descripcion);
        actividad.setEntidad(entidad);
        actividad.setIdEntidad(idEntidad);
        actividad.setTipoActividad(TipoActividad.CREATE);
        actividad.setNivel(NivelActividad.INFO);
        return actividad;
    }

    public static Actividad actualizarActividad(Usuario usuario, String accion, String descripcion, String entidad, Long idEntidad, String valoresAnteriores, String valoresNuevos) {
        Actividad actividad = new Actividad();
        actividad.setUsuario(usuario);
        actividad.setAccion(accion);
        actividad.setDescripcion(descripcion);
        actividad.setEntidad(entidad);
        actividad.setIdEntidad(idEntidad);
        actividad.setValoresAnteriores(valoresAnteriores);
        actividad.setValoresNuevos(valoresNuevos);
        actividad.setTipoActividad(TipoActividad.UPDATE);
        actividad.setNivel(NivelActividad.INFO);
        return actividad;
    }

    public static Actividad eliminarActividad(Usuario usuario, String accion, String descripcion, String entidad, Long idEntidad) {
        Actividad actividad = new Actividad();
        actividad.setUsuario(usuario);
        actividad.setAccion(accion);
        actividad.setDescripcion(descripcion);
        actividad.setEntidad(entidad);
        actividad.setIdEntidad(idEntidad);
        actividad.setTipoActividad(TipoActividad.DELETE);
        actividad.setNivel(NivelActividad.WARNING);
        return actividad;
    }

    public static Actividad loginActividad(Usuario usuario, String direccionIp, String userAgent) {
        Actividad actividad = new Actividad();
        actividad.setUsuario(usuario);
        actividad.setAccion("LOGIN");
        actividad.setDescripcion("Usuario inició sesión en el sistema");
        actividad.setDireccionIp(direccionIp);
        actividad.setUserAgent(userAgent);
        actividad.setTipoActividad(TipoActividad.LOGIN);
        actividad.setNivel(NivelActividad.INFO);
        return actividad;
    }

    public static Actividad errorActividad(Usuario usuario, String accion, String descripcion, String mensajeError) {
        Actividad actividad = new Actividad();
        actividad.setUsuario(usuario);
        actividad.setAccion(accion);
        actividad.setDescripcion(descripcion);
        actividad.setMensajeError(mensajeError);
        actividad.setResultado("ERROR");
        actividad.setTipoActividad(TipoActividad.SISTEMA);
        actividad.setNivel(NivelActividad.ERROR);
        return actividad;
    }

    // Getters and Setters
    public Long getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(Long idActividad) {
        this.idActividad = idActividad;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public Long getIdEntidad() {
        return idEntidad;
    }

    public void setIdEntidad(Long idEntidad) {
        this.idEntidad = idEntidad;
    }

    public TipoActividad getTipoActividad() {
        return tipoActividad;
    }

    public void setTipoActividad(TipoActividad tipoActividad) {
        this.tipoActividad = tipoActividad;
    }

    public NivelActividad getNivel() {
        return nivel;
    }

    public void setNivel(NivelActividad nivel) {
        this.nivel = nivel;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getValoresAnteriores() {
        return valoresAnteriores;
    }

    public void setValoresAnteriores(String valoresAnteriores) {
        this.valoresAnteriores = valoresAnteriores;
    }

    public String getValoresNuevos() {
        return valoresNuevos;
    }

    public void setValoresNuevos(String valoresNuevos) {
        this.valoresNuevos = valoresNuevos;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }

    public LocalDateTime getFechaActividad() {
        return fechaActividad;
    }

    public void setFechaActividad(LocalDateTime fechaActividad) {
        this.fechaActividad = fechaActividad;
    }

    public Long getDuracionMs() {
        return duracionMs;
    }

    public void setDuracionMs(Long duracionMs) {
        this.duracionMs = duracionMs;
    }

    @Override
    public String toString() {
        return "Actividad{" +
                "idActividad=" + idActividad +
                ", accion='" + accion + '\'' +
                ", tipoActividad=" + tipoActividad +
                ", nivel=" + nivel +
                ", usuario='" + getNombreUsuario() + '\'' +
                ", fechaActividad=" + fechaActividad +
                '}';
    }
}
