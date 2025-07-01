package com.sagafalabella.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad Pedido - Representa los pedidos realizados por clientes
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_pedido", unique = true, nullable = false, length = 20)
    @NotBlank(message = "El número de pedido es obligatorio")
    private String numeroPedido;

    @Column(name = "fecha_pedido", nullable = false)
    @NotNull(message = "La fecha del pedido es obligatoria")
    private LocalDateTime fechaPedido;

    @Column(name = "fecha_entrega_estimada")
    private LocalDateTime fechaEntregaEstimada;

    @Column(name = "fecha_entrega_real")
    private LocalDateTime fechaEntregaReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @NotNull(message = "El estado del pedido es obligatorio")
    private EstadoPedido estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega", nullable = false)
    @NotNull(message = "El tipo de entrega es obligatorio")
    private TipoEntrega tipoEntrega;

    @Column(name = "direccion_entrega", length = 500)
    private String direccionEntrega;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Column(name = "total", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "El total debe ser mayor a 0")
    private BigDecimal total;

    @Column(name = "descuento", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    private BigDecimal descuento;

    @Column(name = "impuestos", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Los impuestos no pueden ser negativos")
    private BigDecimal impuestos;

    @Column(name = "fecha_creacion", updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @NotNull(message = "El cliente es obligatorio")
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetallePedido> detalles;

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OrdenPicking ordenPicking;

    // Métodos de negocio
    public boolean puedeSerCancelado() {
        return this.estado == EstadoPedido.PENDIENTE || this.estado == EstadoPedido.CONFIRMADO;
    }

    public boolean estaPendienteDeEntrega() {
        return this.estado == EstadoPedido.EN_PREPARACION || this.estado == EstadoPedido.LISTO_PARA_ENVIO;
    }

    public BigDecimal calcularSubtotal() {
        if (this.detalles == null || this.detalles.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return this.detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularTotalFinal() {
        BigDecimal subtotal = calcularSubtotal();
        BigDecimal descuentoAplicado = this.descuento != null ? this.descuento : BigDecimal.ZERO;
        BigDecimal impuestosAplicados = this.impuestos != null ? this.impuestos : BigDecimal.ZERO;
        
        return subtotal.subtract(descuentoAplicado).add(impuestosAplicados);
    }

    // Enums
    public enum EstadoPedido {
        PENDIENTE,
        CONFIRMADO,
        EN_PREPARACION,
        LISTO_PARA_ENVIO,
        EN_TRANSITO,
        ENTREGADO,
        CANCELADO,
        DEVUELTO
    }

    public enum TipoEntrega {
        DOMICILIO,
        RECOJO_EN_TIENDA,
        PUNTO_DE_RECOJO
    }
}
