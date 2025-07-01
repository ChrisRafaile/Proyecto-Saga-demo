package com.sagafalabella.inventario.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad DetallePedido - Representa los detalles de cada producto en un pedido
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Entity
@Table(name = "detalle_pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cantidad", nullable = false)
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor a 0")
    private BigDecimal precioUnitario;

    @Column(name = "descuento_unitario", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    @Builder.Default
    private BigDecimal descuentoUnitario = BigDecimal.ZERO;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @NotNull(message = "El pedido es obligatorio")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @NotNull(message = "El producto es obligatorio")
    private Producto producto;    // Métodos de negocio
    public BigDecimal calcularSubtotal() {
        BigDecimal subtotalBruto = this.precioUnitario.multiply(BigDecimal.valueOf(this.cantidad));
        BigDecimal descuento = this.descuentoUnitario != null ? 
            this.descuentoUnitario.multiply(BigDecimal.valueOf(this.cantidad)) : BigDecimal.ZERO;
          return subtotalBruto.subtract(descuento);
    }

    @PrePersist
    @PreUpdate
    @SuppressWarnings("unused") // Método llamado automáticamente por JPA
    private void calcularSubtotalAutomatico() {
        this.subtotal = calcularSubtotal();
    }

    public BigDecimal getPrecioFinalUnitario() {
        BigDecimal descuento = this.descuentoUnitario != null ? this.descuentoUnitario : BigDecimal.ZERO;
        return this.precioUnitario.subtract(descuento);
    }
}
