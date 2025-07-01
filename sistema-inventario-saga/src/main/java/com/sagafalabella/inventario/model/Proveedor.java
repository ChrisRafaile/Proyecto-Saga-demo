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
import java.util.List;

/**
 * Entidad Proveedor - Representa los proveedores de Saga Falabella
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ruc", unique = true, nullable = false, length = 11)
    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 11, max = 11, message = "El RUC debe tener 11 dígitos")
    private String ruc;

    @Column(name = "razon_social", nullable = false, length = 200)
    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 200, message = "La razón social no puede exceder 200 caracteres")
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 200)
    @Size(max = 200, message = "El nombre comercial no puede exceder 200 caracteres")
    private String nombreComercial;

    @Column(name = "email", length = 100)
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @Column(name = "telefono", length = 20)
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Column(name = "direccion", length = 500)
    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String direccion;

    @Column(name = "contacto_principal", length = 100)
    @Size(max = 100, message = "El contacto principal no puede exceder 100 caracteres")
    private String contactoPrincipal;

    @Column(name = "condiciones_pago", length = 200)
    @Size(max = 200, message = "Las condiciones de pago no pueden exceder 200 caracteres")
    private String condicionesPago;

    @Column(name = "calificacion")
    @Min(value = 1, message = "La calificación debe ser mínimo 1")
    @Max(value = 5, message = "La calificación debe ser máximo 5")
    private Integer calificacion;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_registro", updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    // Relaciones
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Producto> productos;

    // Métodos de negocio
    public String getNombreDisplay() {
        return this.nombreComercial != null ? this.nombreComercial : this.razonSocial;
    }

    public boolean esProveedorConfiable() {
        return this.calificacion != null && this.calificacion >= 4;
    }
}
