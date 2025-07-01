package com.sagafalabella.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad OrdenPicking - Representa las órdenes de picking generadas para los pedidos
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Entity
@Table(name = "ordenes_picking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenPicking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_orden", unique = true, nullable = false, length = 20)
    @NotBlank(message = "El código de orden es obligatorio")
    private String codigoOrden;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @NotNull(message = "El estado es obligatorio")
    @Builder.Default
    private EstadoOrdenPicking estado = EstadoOrdenPicking.PENDIENTE;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_inicio_picking")
    private LocalDateTime fechaInicioPicking;

    @Column(name = "fecha_fin_picking")
    private LocalDateTime fechaFinPicking;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "operario_asignado", length = 100)
    private String operarioAsignado;

    @Column(name = "fecha_creacion", updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    // Relaciones
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @NotNull(message = "El pedido es obligatorio")
    private Pedido pedido;

    // Métodos de negocio
    public void iniciarPicking(String operario) {
        this.operarioAsignado = operario;
        this.fechaInicioPicking = LocalDateTime.now();
        this.estado = EstadoOrdenPicking.EN_PROCESO;
    }

    public void completarPicking() {
        this.fechaFinPicking = LocalDateTime.now();
        this.estado = EstadoOrdenPicking.COMPLETADO;
    }

    public void cancelarPicking(String motivo) {
        this.observaciones = motivo;
        this.estado = EstadoOrdenPicking.CANCELADO;
    }

    public boolean puedeSerProcesada() {
        return this.estado == EstadoOrdenPicking.PENDIENTE || this.estado == EstadoOrdenPicking.ASIGNADO;
    }

    public Long getDuracionPickingEnMinutos() {
        if (this.fechaInicioPicking != null && this.fechaFinPicking != null) {
            return java.time.Duration.between(this.fechaInicioPicking, this.fechaFinPicking).toMinutes();
        }
        return null;
    }

    // Enum
    public enum EstadoOrdenPicking {
        PENDIENTE,
        ASIGNADO,
        EN_PROCESO,
        COMPLETADO,
        CANCELADO
    }
}
