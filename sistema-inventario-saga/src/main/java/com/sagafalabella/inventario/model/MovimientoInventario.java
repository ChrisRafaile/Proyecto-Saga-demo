package com.sagafalabella.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad MovimientoInventario - Registra todos los movimientos de entrada y salida de productos
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Entity
@Table(name = "movimientos_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false)
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipoMovimiento;

    @Column(name = "cantidad", nullable = false)
    @NotNull(message = "La cantidad es obligatoria")
    private Integer cantidad;

    @Column(name = "stock_anterior")
    private Integer stockAnterior;

    @Column(name = "stock_nuevo")
    private Integer stockNuevo;

    @Column(name = "motivo", length = 200)
    @Size(max = 200, message = "El motivo no puede exceder 200 caracteres")
    private String motivo;

    @Column(name = "usuario_responsable", length = 100)
    @Size(max = 100, message = "El usuario responsable no puede exceder 100 caracteres")
    private String usuarioResponsable;

    @Column(name = "numero_documento", length = 50)
    @Size(max = 50, message = "El número de documento no puede exceder 50 caracteres")
    private String numeroDocumento; // Factura, guía, etc.

    @Column(name = "fecha_movimiento", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaMovimiento;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @NotNull(message = "El producto es obligatorio")
    private Producto producto;

    // Métodos de negocio
    public boolean esMovimientoDeEntrada() {
        return this.tipoMovimiento == TipoMovimiento.ENTRADA_COMPRA ||
               this.tipoMovimiento == TipoMovimiento.ENTRADA_DEVOLUCION ||
               this.tipoMovimiento == TipoMovimiento.ENTRADA_AJUSTE ||
               this.tipoMovimiento == TipoMovimiento.ENTRADA_TRANSFERENCIA;
    }

    public boolean esMovimientoDeSalida() {
        return this.tipoMovimiento == TipoMovimiento.SALIDA_VENTA ||
               this.tipoMovimiento == TipoMovimiento.SALIDA_DEVOLUCION ||
               this.tipoMovimiento == TipoMovimiento.SALIDA_AJUSTE ||
               this.tipoMovimiento == TipoMovimiento.SALIDA_TRANSFERENCIA ||
               this.tipoMovimiento == TipoMovimiento.SALIDA_MERMA;
    }

    public Integer getCantidadConSigno() {
        return esMovimientoDeEntrada() ? this.cantidad : -this.cantidad;
    }

    // Enum
    public enum TipoMovimiento {
        ENTRADA_COMPRA("Entrada por compra"),
        ENTRADA_DEVOLUCION("Entrada por devolución"),
        ENTRADA_AJUSTE("Entrada por ajuste"),
        ENTRADA_TRANSFERENCIA("Entrada por transferencia"),
        SALIDA_VENTA("Salida por venta"),
        SALIDA_DEVOLUCION("Salida por devolución"),
        SALIDA_AJUSTE("Salida por ajuste"),
        SALIDA_TRANSFERENCIA("Salida por transferencia"),
        SALIDA_MERMA("Salida por merma");

        private final String descripcion;

        TipoMovimiento(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
