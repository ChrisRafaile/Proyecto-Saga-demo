package com.sagafalabella.inventario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Rol - Representa los roles de usuarios en el sistema
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", unique = true, nullable = false, length = 50)
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 50, message = "El nombre del rol no puede exceder 50 caracteres")
    private String nombre;

    @Column(name = "descripcion", length = 200)
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    // Métodos de negocio
    public boolean esRolAdministrador() {
        return "ADMIN".equalsIgnoreCase(this.nombre) || "ADMINISTRADOR".equalsIgnoreCase(this.nombre);
    }

    public boolean esRolSupervisor() {
        return "SUPERVISOR".equalsIgnoreCase(this.nombre) || "SUPERVISOR_ALMACEN".equalsIgnoreCase(this.nombre);
    }

    public boolean esRolOperario() {
        return "OPERARIO".equalsIgnoreCase(this.nombre) || "OPERARIO_LOGISTICO".equalsIgnoreCase(this.nombre);
    }

    // Roles predefinidos del sistema
    public static final String ADMIN = "ADMIN";
    public static final String SUPERVISOR_ALMACEN = "SUPERVISOR_ALMACEN";
    public static final String OPERARIO_LOGISTICO = "OPERARIO_LOGISTICO";
    public static final String ATENCION_CLIENTE = "ATENCION_CLIENTE";
    public static final String COMPRADOR_CORPORATIVO = "COMPRADOR_CORPORATIVO";
}
