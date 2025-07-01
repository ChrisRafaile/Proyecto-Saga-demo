package com.sagafalabella.inventario.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Cliente basada en la tabla 'cliente' de la BD dbsaga
 * Representa los clientes que pueden realizar pedidos
 */
@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;

    @Column(name = "email", unique = true, nullable = false)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @Column(name = "telefono", length = 20)
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Formato de teléfono inválido")
    private String telefono;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente")
    private TipoCliente tipoCliente = TipoCliente.REGULAR;

    // Relación con Usuario (si el cliente tiene cuenta en el sistema)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Relación con Pedidos
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pedido> pedidos;

    // Enum para tipo de cliente
    public enum TipoCliente {
        REGULAR("Cliente Regular"),
        PREMIUM("Cliente Premium"),
        CORPORATIVO("Cliente Corporativo"),
        VIP("Cliente VIP");

        private final String descripcion;

        TipoCliente(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    // Métodos de conveniencia
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getDireccionCompleta() {
        StringBuilder direccionCompleta = new StringBuilder();
        if (direccion != null && !direccion.isEmpty()) {
            direccionCompleta.append(direccion);
        }
        if (ciudad != null && !ciudad.isEmpty()) {
            if (direccionCompleta.length() > 0) {
                direccionCompleta.append(", ");
            }
            direccionCompleta.append(ciudad);
        }
        if (codigoPostal != null && !codigoPostal.isEmpty()) {
            if (direccionCompleta.length() > 0) {
                direccionCompleta.append(" - ");
            }
            direccionCompleta.append(codigoPostal);
        }
        return direccionCompleta.toString();
    }

    public boolean tieneUsuario() {
        return usuario != null;
    }

    public int getCantidadPedidos() {
        return pedidos != null ? pedidos.size() : 0;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public TipoCliente getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(TipoCliente tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}
