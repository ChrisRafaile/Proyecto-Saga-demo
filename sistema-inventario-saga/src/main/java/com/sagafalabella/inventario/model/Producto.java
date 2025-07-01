package com.sagafalabella.inventario.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Producto - Representa los productos en el inventario de Saga Falabella
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idproducto")
    private Long idproducto;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "categoria", length = 50)
    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    private String categoria;    @Column(name = "precio", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;
    
    @Column(name = "fechavencimiento")
    private LocalDate fechavencimiento;
    
    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "stock_actual")
    @Builder.Default
    private Integer stockActual = 0;

    @Column(name = "stock_minimo")
    @Builder.Default
    private Integer stockMinimo = 5;

    @Column(name = "marca", length = 50)
    private String marca;

    @Column(name = "ubicacion_almacen", length = 100)
    private String ubicacionAlmacen;    @Column(name = "codigo_producto", unique = true, length = 50)
    @NotBlank(message = "El código del producto es obligatorio")
    private String codigoProducto;
    
    @Column(name = "idproveedor")
    private Long idproveedor;

    // Campo para imagen del producto
    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "imagen_nombre")
    private String imagenNombre;

    // Relación con Proveedor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproveedor", insertable = false, updatable = false)
    private Proveedor proveedor;
      // Métodos de negocio adaptados a tu estructura
    public boolean estaProximoAVencer(int diasAntes) {
        if (this.fechavencimiento == null) return false;
        return this.fechavencimiento.isBefore(LocalDate.now().plusDays(diasAntes));
    }
    
    public String getDescripcionCompleta() {
        StringBuilder descripcionCompleta = new StringBuilder(this.nombre);
        if (this.descripcion != null && !this.descripcion.trim().isEmpty()) {
            descripcionCompleta.append(" - ").append(this.descripcion);
        }
        return descripcionCompleta.toString();
    }

    public boolean esProductoValido() {
        return this.nombre != null && !this.nombre.trim().isEmpty() && 
               this.precio.compareTo(BigDecimal.ZERO) > 0;
    }
}
