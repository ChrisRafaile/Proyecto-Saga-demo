package com.sagafalabella.inventario.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Usuario basada en la tabla 'usuario_sistema' de la BD dbsaga
 * Implementa UserDetails para integración con Spring Security
 */
@Entity
@Table(name = "usuario_sistema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;    @Column(name = "password", nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @Column(name = "email", unique = true, nullable = false)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolUsuario rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false)
    private TipoUsuario tipoUsuario;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ultimo_acceso")
    private LocalDateTime fechaUltimoAcceso;

    // Enums para roles y tipos de usuario
    public enum RolUsuario {
        SUPER_ADMIN("Super Administrador - Saga Falabella"),
        ADMIN_INVENTARIO("Administrador de Inventario"),
        ADMIN_VENTAS("Administrador de Ventas"),
        EMPLEADO_ALMACEN("Empleado de Almacén"),
        EMPLEADO_VENTAS("Empleado de Ventas"),
        CLIENTE("Cliente");

        private final String descripcion;

        RolUsuario(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    public enum TipoUsuario {
        INTERNO("Saga Falabella - Personal Interno"),
        EXTERNO("Cliente Externo");

        private final String descripcion;

        TipoUsuario(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    // Métodos de UserDetails para Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return activo;
    }

    @Override
    public boolean isAccountNonLocked() {
        return activo;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    // Métodos de conveniencia
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public boolean esAdministrador() {
        return rol == RolUsuario.SUPER_ADMIN || 
               rol == RolUsuario.ADMIN_INVENTARIO || 
               rol == RolUsuario.ADMIN_VENTAS;
    }

    public boolean esEmpleado() {
        return rol == RolUsuario.EMPLEADO_ALMACEN || 
               rol == RolUsuario.EMPLEADO_VENTAS;
    }

    public boolean esCliente() {
        return rol == RolUsuario.CLIENTE;
    }

    public boolean esSagaFalabella() {
        return tipoUsuario == TipoUsuario.INTERNO;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public void setFechaUltimoAcceso(LocalDateTime fechaUltimoAcceso) {
        this.fechaUltimoAcceso = fechaUltimoAcceso;
    }

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
