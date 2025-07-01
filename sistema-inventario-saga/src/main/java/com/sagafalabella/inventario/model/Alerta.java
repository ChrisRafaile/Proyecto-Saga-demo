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
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una alerta del sistema
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Entity
@Table(name = "alertas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alerta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long idAlerta;
    
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Column(name = "descripcion", nullable = false, length = 1000)
    private String descripcion;
    
    @NotNull(message = "El tipo de alerta es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false)
    private TipoAlerta tipoAlerta;
    
    @NotNull(message = "El nivel de prioridad es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_prioridad", nullable = false)
    private NivelPrioridad nivelPrioridad;
    
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoAlerta estado;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_leida")
    private LocalDateTime fechaLeida;
    
    @Column(name = "fecha_resuelta")
    private LocalDateTime fechaResuelta;
      @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "usuario_creador_id", nullable = true)
    private Usuario usuarioCreador;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "usuario_asignado_id", nullable = true)
    private Usuario usuarioAsignado;
    
    @Column(name = "entidad_relacionada", length = 100)
    private String entidadRelacionada;
    
    @Column(name = "id_entidad_relacionada")
    private Long idEntidadRelacionada;
    
    @Column(name = "acciones_realizadas", columnDefinition = "TEXT")
    private String accionesRealizadas;
    
    @Column(name = "notificacion_enviada", nullable = false)
    private Boolean notificacionEnviada = false;
    
    /**
     * Método que se ejecuta antes de persistir
     */
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoAlerta.NO_LEIDA;
        }
        if (notificacionEnviada == null) {
            notificacionEnviada = false;
        }
    }
    
    /**
     * Marcar alerta como leída
     */
    public void marcarComoLeida() {
        this.estado = EstadoAlerta.LEIDA;
        this.fechaLeida = LocalDateTime.now();
    }
    
    /**
     * Marcar alerta como resuelta
     */
    public void marcarComoResuelta() {
        this.estado = EstadoAlerta.RESUELTA;
        this.fechaResuelta = LocalDateTime.now();
    }
    
    /**
     * Verificar si la alerta es crítica
     */
    public boolean esCritica() {
        return this.nivelPrioridad == NivelPrioridad.CRITICA;
    }
    
    /**
     * Obtener color CSS según el tipo de alerta
     */
    public String getColorCSS() {
        return switch (this.nivelPrioridad) {
            case CRITICA -> "danger";
            case ALTA -> "warning";
            case MEDIA -> "info";
            case BAJA -> "success";
        };
    }
    
    /**
     * Obtener icono FontAwesome según el tipo
     */
    public String getIcono() {
        return switch (this.tipoAlerta) {
            case STOCK_BAJO -> "fas fa-boxes";
            case SISTEMA -> "fas fa-cog";
            case USUARIO -> "fas fa-user";
            case PRODUCTO -> "fas fa-box";
            case PEDIDO -> "fas fa-shopping-cart";
            case PROVEEDOR -> "fas fa-truck";
            case SEGURIDAD -> "fas fa-shield-alt";
            case OTROS -> "fas fa-bell";
        };
    }
    
    /**
     * Tipos de alerta disponibles
     */
    public enum TipoAlerta {
        STOCK_BAJO("Stock Bajo"),
        SISTEMA("Sistema"),
        USUARIO("Usuario"),
        PRODUCTO("Producto"),
        PEDIDO("Pedido"),
        PROVEEDOR("Proveedor"),
        SEGURIDAD("Seguridad"),
        OTROS("Otros");
        
        private final String descripcion;
        
        TipoAlerta(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    /**
     * Niveles de prioridad
     */
    public enum NivelPrioridad {
        BAJA("Baja"),
        MEDIA("Media"),
        ALTA("Alta"),
        CRITICA("Crítica");
        
        private final String descripcion;
        
        NivelPrioridad(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    /**
     * Estados de la alerta
     */
    public enum EstadoAlerta {
        NO_LEIDA("No Leída"),
        LEIDA("Leída"),
        EN_PROCESO("En Proceso"),
        RESUELTA("Resuelta"),
        DESCARTADA("Descartada");
        
        private final String descripcion;
        
        EstadoAlerta(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
}
