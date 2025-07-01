# Sistema de Gestión de Inventario - Saga Falabella

## Descripción

Sistema web integral para la gestión de inventario de Saga Falabella, desarrollado con **Spring Boot 3.5.2** y **Java 17**. Implementa un sistema completo de autenticación y autorización por roles, con interfaces diferenciadas para cada tipo de usuario.

## Características Principales

### 🔐 **Sistema de Autenticación y Autorización**
- **Registro diferenciado** por tipo de usuario (Cliente/Empleado)
- **Login unificado** con redirección automática según el rol
- **Control de acceso** basado en roles (RBAC)
- **Gestión de sesiones** con Spring Security

### 👥 **Roles de Usuario**
1. **SUPER_ADMIN**: Control total del sistema
2. **ADMIN_INVENTARIO**: Gestión de inventario y productos
3. **ADMIN_VENTAS**: Gestión de ventas y pedidos
4. **EMPLEADO_ALMACEN**: Operaciones de almacén
5. **EMPLEADO_VENTAS**: Operaciones de ventas
6. **CLIENTE**: Compras y consultas

### 🎨 **Interfaces de Usuario**
- **Dashboards personalizados** por rol
- **Navegación dinámica** según permisos
- **Diseño responsive** con Bootstrap 5
- **Componentes reutilizables** con Thymeleaf fragments

## Tecnologías Utilizadas

### Backend
- **Java 17**
- **Spring Boot 3.5.2**
- **Spring MVC** (Patrón Model-View-Controller)
- **Spring Data JPA** (ORM y repositorios)
- **Spring Security** (Autenticación y autorización)
- **Spring Validation** (Validación de datos)
- **Lombok** (Reducción de código boilerplate)

### Frontend
- **Thymeleaf** (Motor de plantillas)
- **Bootstrap 5.3.0** (Framework CSS)
- **Font Awesome 6.4.0** (Iconografía)
- **Bootstrap Icons** (Iconos adicionales)
- **Chart.js** (Gráficos y estadísticas)

### Base de Datos
- **PostgreSQL** (Base de datos principal)
- **HikariCP** (Pool de conexiones)

### Herramientas de Desarrollo
- **Maven** (Gestión de dependencias)
- **Spring Boot DevTools** (Desarrollo en caliente)

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/sagafalabella/inventario/
│   │   ├── config/          # Configuraciones (Security, etc.)
│   │   ├── controller/      # Controladores MVC
│   │   ├── model/           # Entidades JPA
│   │   ├── repository/      # Repositorios Spring Data
│   │   ├── service/         # Lógica de negocio
│   │   └── SistemaInventarioApplication.java
│   └── resources/
│       ├── templates/       # Plantillas Thymeleaf
│       │   ├── admin/       # Vistas de administrador
│       │   ├── auth/        # Vistas de autenticación
│       │   ├── client/      # Vistas de cliente
│       │   ├── email/       # Plantillas de email
│       │   ├── error/       # Páginas de error
│       │   ├── fragments/   # Fragmentos reutilizables
│       │   └── test/        # Vistas de prueba
│       ├── static/          # Recursos estáticos
│       │   ├── css/         # Estilos CSS
│       │   ├── js/          # JavaScript
│       │   ├── images/      # Imágenes
│       │   └── fonts/       # Fuentes
│       └── application.properties
```

## Funcionalidades Implementadas

### 🔐 **Autenticación**
- [x] Formulario de login unificado
- [x] Página de selección de tipo de registro
- [x] Formulario de registro para clientes
- [x] Formulario de registro para empleados
- [x] Validación de datos de entrada
- [x] Manejo de errores de autenticación
- [x] Página de acceso denegado

### 🏠 **Dashboards**
- [x] Dashboard para administradores con estadísticas del sistema
- [x] Dashboard para clientes con información personal
- [x] Dashboard para empleados (estructura base)
- [x] Redirección automática según rol de usuario

### 🧩 **Componentes de UI**
- [x] Navbar dinámica con menús por rol
- [x] Fragmentos Thymeleaf (head, navbar, footer)
- [x] Estilos CSS personalizados para Saga Falabella
- [x] JavaScript con utilidades comunes
- [x] Sistema de notificaciones (toasts)
- [x] Modalidades de confirmación

### 🎨 **Diseño y UX**
- [x] Diseño responsive
- [x] Paleta de colores corporativa
- [x] Animaciones y transiciones CSS
- [x] Iconografía consistente
- [x] Formularios con validación visual

## Configuración de la Base de Datos

### PostgreSQL
```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/saga_inventario
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update
```

## Instalación y Ejecución

### Prerrequisitos
- Java 17 o superior
- PostgreSQL 12 o superior
- Maven 3.6 o superior

### Pasos
1. **Clonar el repositorio**
   ```bash
   git clone [URL_DEL_REPOSITORIO]
   cd sistema-inventario-saga
   ```

2. **Configurar la base de datos**
   - Crear base de datos PostgreSQL: `saga_inventario`
   - Actualizar credenciales en `application.properties`

3. **Compilar el proyecto**
   ```bash
   mvn clean compile
   ```

4. **Ejecutar la aplicación**
   ```bash
   mvn spring-boot:run
   ```

5. **Acceder al sistema**
   - URL: `http://localhost:8080`
   - La aplicación creará las tablas automáticamente

## Rutas Principales

### Públicas
- `/` - Página principal
- `/auth/login` - Iniciar sesión
- `/auth/register` - Seleccionar tipo de registro
- `/registro/cliente` - Registro de cliente
- `/registro/empleado` - Registro de empleado

### Autenticadas
- `/dashboard` - Dashboard principal (redirige según rol)
- `/admin/dashboard` - Dashboard de administrador
- `/client/dashboard` - Dashboard de cliente
- `/auth/access-denied` - Acceso denegado

## Características de Seguridad

### Spring Security
- **Autenticación** basada en formulario
- **Autorización** por roles y URLs
- **Protección CSRF** habilitada
- **Encriptación** de contraseñas con BCrypt
- **Gestión de sesiones** segura

### Validaciones
- **Validación de entrada** en formularios
- **Sanitización** de datos
- **Validación de roles** en controladores
- **Control de acceso** a recursos

## Próximas Funcionalidades

### 📦 **Gestión de Productos**
- [ ] CRUD completo de productos
- [ ] Gestión de categorías
- [ ] Control de stock
- [ ] Alertas de stock bajo

### 🛒 **Gestión de Pedidos**
- [ ] Carrito de compras
- [ ] Proceso de checkout
- [ ] Seguimiento de pedidos
- [ ] Historial de compras

### 👥 **Gestión de Usuarios**
- [ ] Panel de administración de usuarios
- [ ] Gestión de permisos
- [ ] Perfil de usuario editable
- [ ] Sistema de notificaciones

### 📊 **Reportes y Analytics**
- [ ] Reportes de ventas
- [ ] Análisis de inventario
- [ ] Dashboard con métricas
- [ ] Exportación de datos

## Autor

**Christopher Lincoln Rafaile Naupay**  
Desarrollo del Sistema de Gestión de Inventario para Saga Falabella  
Análisis y Diseño de Sistemas - Ciclo VII

## Licencia

Este proyecto es desarrollado con fines académicos para el curso de Análisis y Diseño de Sistemas.

---

*Sistema desarrollado con ❤️ usando Spring Boot y las mejores prácticas de desarrollo web*
