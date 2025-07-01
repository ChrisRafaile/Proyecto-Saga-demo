# DOCUMENTACIÓN DE PATRONES DE DISEÑO IMPLEMENTADOS
## Sistema de Gestión de Inventario - Saga Falabella

### Autor: Christopher Lincoln Rafaile Naupay
### Fecha: Junio 2025

---

## 📋 RESUMEN EJECUTIVO

El Sistema de Gestión de Inventario Saga Falabella ha sido desarrollado implementando los patrones de diseño solicitados: **MVC (Model-View-Controller)**, **DAO (Data Access Object)** y **Arquitectura por Capas**, utilizando **Spring Boot** como framework principal y **PostgreSQL** como base de datos.

---

## 🏗️ 1. PATRÓN MVC (MODEL-VIEW-CONTROLLER)

### 📱 **MODELO (Model)**
**Ubicación:** `src/main/java/com/sagafalabella/inventario/model/`

Las entidades del sistema representan las tablas de la base de datos y encapsulan la lógica de negocio:

#### **Entidades Principales:**

```java
// Producto.java - Entidad principal del inventario
@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    
    @Column(name = "cantidad_stock", nullable = false)
    private Integer cantidadStock;
    
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 5;
    
    @Enumerated(EnumType.STRING)
    private CategoriaProducto categoria;
    
    // Constructores, getters, setters y métodos de negocio
}
```

#### **Otras Entidades del Sistema:**
- **Usuario.java** - Gestión de usuarios del sistema
- **Cliente.java** - Información de clientes
- **Proveedor.java** - Datos de proveedores
- **Pedido.java** - Órdenes de compra
- **MovimientoInventario.java** - Trazabilidad de stock
- **OrdenPicking.java** - Gestión de despachos

### 🎮 **CONTROLADOR (Controller)**
**Ubicación:** `src/main/java/com/sagafalabella/inventario/controller/`

Los controladores gestionan las peticiones HTTP y coordinan la interacción entre Vista y Modelo:

```java
// HomeController.java - Controlador principal
@Controller
public class HomeController {
    
    @GetMapping("/")
    public String index() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && 
            !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        
        return "index"; // Vista principal
    }
    
    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("titulo", "Demo del Sistema");
        return "demo";
    }
}
```

```java
// InventarioController.java - Gestión de inventario
@Controller
@RequestMapping("/inventario")
public class InventarioController {
    
    @Autowired
    private ProductoService productoService;
    
    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        // Obtener métricas del inventario
        long totalProductos = productoService.contarProductos();
        long productosStockBajo = productoService.contarProductosStockBajo();
        
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("productosStockBajo", productosStockBajo);
        
        return "inventario/dashboard";
    }
    
    @GetMapping("/productos")
    public String gestionProductos(
            @RequestParam(required = false) String search,
            Model model) {
        
        List<Producto> productos;
        if (search != null && !search.trim().isEmpty()) {
            productos = productoService.buscarProductos(search);
            model.addAttribute("searchQuery", search);
        } else {
            productos = productoService.obtenerTodosLosProductos();
        }
        
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        
        return "inventario/productos";
    }
}
```

#### **Controladores del Sistema:**
- **HomeController.java** - Páginas públicas y navegación principal
- **InventarioController.java** - Gestión de inventario y productos
- **AuthController.java** - Autenticación y autorización
- **RegistroController.java** - Registro de usuarios
- **DashboardController.java** - Panel de control principal

### 🖼️ **VISTA (View)**
**Ubicación:** `src/main/resources/templates/`

Las vistas utilizan **Thymeleaf** como motor de plantillas para renderizar la interfaz:

```html
<!-- index.html - Vista principal -->
<section class="hero-section position-relative">
    <div class="container position-relative">
        <div class="row align-items-center min-vh-75">
            <div class="col-lg-6 order-2 order-lg-1">
                <h1 class="display-4 fw-bold text-dark mb-4 typing-effect">
                    Sistema de Gestión de Inventario
                    <span class="gradient-text d-block">Saga Falabella</span>
                </h1>
                
                <!-- Botones dinámicos según autenticación -->
                <div class="hero-buttons">
                    <th:block sec:authorize="!isAuthenticated()">
                        <a th:href="@{/auth/login}" class="btn btn-saga-primary btn-lg">
                            <i class="fas fa-sign-in-alt me-2"></i>Iniciar Sesión
                        </a>
                    </th:block>
                    
                    <th:block sec:authorize="isAuthenticated()">
                        <a th:href="@{/dashboard}" class="btn btn-saga-primary btn-lg">
                            <i class="fas fa-tachometer-alt me-2"></i>Ir al Dashboard
                        </a>
                    </th:block>
                </div>
            </div>
        </div>
    </div>
</section>
```

#### **Estructura de Vistas:**
- **index.html** - Página principal del sistema
- **inventario/dashboard.html** - Panel de control de inventario
- **inventario/productos.html** - Gestión de productos
- **demo.html** - Demostración del sistema
- **help.html** - Centro de ayuda
- **about.html** - Información del sistema

---

## 🗃️ 2. PATRÓN DAO (DATA ACCESS OBJECT)

### **Implementación con Spring Data JPA**
**Ubicación:** `src/main/java/com/sagafalabella/inventario/repository/`

El patrón DAO se implementa mediante **Spring Data JPA** que proporciona una abstracción sobre el acceso a datos:

```java
// ProductoRepository.java - DAO para entidad Producto
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    // Consultas derivadas del nombre del método
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    
    List<Producto> findByCategoria(CategoriaProducto categoria);
    
    List<Producto> findByCantidadStockLessThan(Integer stock);
    
    boolean existsByCodigo(String codigo);
    
    // Consultas personalizadas con @Query
    @Query("SELECT p FROM Producto p WHERE p.activo = true")
    List<Producto> findProductosActivos();
    
    @Query("SELECT p FROM Producto p WHERE p.cantidadStock <= p.stockMinimo")
    List<Producto> findProductosStockBajo();
    
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.cantidadStock <= p.stockMinimo")
    long countProductosStockBajo();
}
```

```java
// UsuarioRepository.java - DAO para gestión de usuarios
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByUsername(String username);
    
    Optional<Usuario> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<Usuario> findByRol(Rol rol);
    
    List<Usuario> findByActivoTrue();
}
```

#### **Repositorios del Sistema:**
- **ProductoRepository.java** - Acceso a datos de productos
- **UsuarioRepository.java** - Gestión de usuarios
- **ClienteRepository.java** - Información de clientes
- **PedidoRepository.java** - Órdenes y pedidos
- **MovimientoInventarioRepository.java** - Trazabilidad

### **Ventajas del Patrón DAO Implementado:**
1. **Separación de responsabilidades** - La lógica de acceso a datos está aislada
2. **Reutilización** - Los repositorios pueden ser utilizados por múltiples servicios
3. **Mantenibilidad** - Cambios en la BD no afectan la lógica de negocio
4. **Testabilidad** - Fácil creación de mocks para pruebas unitarias

---

## 🏢 3. ARQUITECTURA POR CAPAS

### **Estructura del Proyecto:**

```
src/main/java/com/sagafalabella/inventario/
├── 📁 controller/          # CAPA DE PRESENTACIÓN
│   ├── HomeController.java
│   ├── InventarioController.java
│   ├── AuthController.java
│   └── RegistroController.java
│
├── 📁 service/             # CAPA DE LÓGICA DE NEGOCIO
│   ├── ProductoService.java
│   ├── UsuarioService.java
│   └── ClienteService.java
│
├── 📁 repository/          # CAPA DE ACCESO A DATOS
│   ├── ProductoRepository.java
│   ├── UsuarioRepository.java
│   └── ClienteRepository.java
│
├── 📁 model/              # CAPA DE ENTIDADES/MODELO
│   ├── Producto.java
│   ├── Usuario.java
│   ├── Cliente.java
│   └── MovimientoInventario.java
│
├── 📁 config/             # CONFIGURACIÓN
│   ├── SecurityConfig.java
│   └── DatabaseConfig.java
│
└── SistemaInventarioApplication.java  # PUNTO DE ENTRADA
```

### **📊 CAPA DE PRESENTACIÓN (Controllers)**
**Responsabilidad:** Gestionar peticiones HTTP, validar datos de entrada y coordinar respuestas.

```java
@Controller
@RequestMapping("/inventario")
public class InventarioController {
    
    @Autowired
    private ProductoService productoService; // Inyección de dependencias
    
    @GetMapping("/productos")
    public String gestionProductos(
            @RequestParam(required = false) String search,
            Model model) {
        
        // Delegación a la capa de servicio
        List<Producto> productos = (search != null) ? 
            productoService.buscarProductos(search) : 
            productoService.obtenerTodosLosProductos();
        
        // Preparación de datos para la vista
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        
        return "inventario/productos"; // Retorno de vista
    }
}
```

### **⚙️ CAPA DE LÓGICA DE NEGOCIO (Services)**
**Responsabilidad:** Implementar las reglas de negocio y coordinar operaciones complejas.

```java
@Service
@Transactional
public class ProductoService {
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private MovimientoInventarioRepository movimientoRepository;
    
    // Lógica de negocio: actualizar stock con validaciones
    public void actualizarStock(Long productoId, Integer nuevaCantidad, 
                               String tipoMovimiento, String motivo) {
        
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        
        // Validaciones de negocio
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        
        Integer stockAnterior = producto.getCantidadStock();
        producto.setCantidadStock(nuevaCantidad);
        
        // Guardar producto actualizado
        productoRepository.save(producto);
        
        // Registrar movimiento de inventario (trazabilidad)
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento(TipoMovimiento.valueOf(tipoMovimiento));
        movimiento.setCantidadAnterior(stockAnterior);
        movimiento.setCantidadNueva(nuevaCantidad);
        movimiento.setMotivo(motivo);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        
        movimientoRepository.save(movimiento);
        
        // Verificar stock mínimo y generar alertas si es necesario
        if (nuevaCantidad <= producto.getStockMinimo()) {
            generarAlertaStockMinimo(producto);
        }
    }
    
    // Método de consulta con lógica de filtrado
    public List<Producto> buscarProductos(String termino) {
        return productoRepository.findByNombreContainingIgnoreCase(termino);
    }
    
    // Método de análisis de negocio
    public long contarProductosStockBajo() {
        return productoRepository.countProductosStockBajo();
    }
    
    private void generarAlertaStockMinimo(Producto producto) {
        // Lógica para notificaciones/alertas
        log.warn("ALERTA: Stock mínimo alcanzado para producto: {}", 
                producto.getNombre());
    }
}
```

### **🗄️ CAPA DE ACCESO A DATOS (Repositories)**
**Responsabilidad:** Abstraer el acceso a la base de datos y proporcionar operaciones CRUD.

```java
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    // Operaciones CRUD básicas heredadas de JpaRepository:
    // save(), findById(), findAll(), deleteById(), etc.
    
    // Consultas personalizadas
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    
    @Query("SELECT p FROM Producto p WHERE p.cantidadStock <= p.stockMinimo")
    List<Producto> findProductosStockBajo();
    
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.cantidadStock <= p.stockMinimo")
    long countProductosStockBajo();
}
```

### **🏛️ CAPA DE ENTIDADES/MODELO (Models)**
**Responsabilidad:** Representar las entidades del dominio y mapear con la base de datos.

```java
@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String nombre;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    
    @Column(name = "cantidad_stock", nullable = false)
    private Integer cantidadStock;
    
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 5;
    
    @Enumerated(EnumType.STRING)
    private CategoriaProducto categoria;
    
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
    
    @CreationTimestamp
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    // Constructores, getters, setters
    // Métodos de negocio específicos de la entidad
    
    public boolean esStockBajo() {
        return this.cantidadStock <= this.stockMinimo;
    }
    
    public void reducirStock(Integer cantidad) {
        if (cantidad > this.cantidadStock) {
            throw new IllegalArgumentException("Stock insuficiente");
        }
        this.cantidadStock -= cantidad;
    }
}
```

---

## 🔗 4. CONEXIÓN A BASE DE DATOS

### **Configuración de PostgreSQL**
**Archivo:** `src/main/resources/application.properties`

```properties
# CONFIGURACION DE BASE DE DATOS POSTGRESQL
spring.datasource.url=jdbc:postgresql://localhost:5432/dbsaga
spring.datasource.username=postgres
spring.datasource.password=123456789
spring.datasource.driver-class-name=org.postgresql.Driver

# CONFIGURACION JPA/HIBERNATE
spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
spring.jpa.database=postgresql
```

### **Dependencias en pom.xml**

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Driver PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

---

## 📊 5. DIAGRAMA DE ARQUITECTURA

```
┌─────────────────────────────────────────────────────┐
│                  NAVEGADOR WEB                      │
│             (Cliente - Frontend)                    │
└─────────────────┬───────────────────────────────────┘
                  │ HTTP Requests/Responses
                  │
┌─────────────────▼───────────────────────────────────┐
│              CAPA DE PRESENTACIÓN                   │
│                 (Controllers)                       │
│  ┌─────────────┬─────────────┬─────────────────────┐│
│  │HomeCtroller │InventarioCtrl│   AuthController    ││
│  │             │             │                     ││
│  └─────────────┴─────────────┴─────────────────────┘│
└─────────────────┬───────────────────────────────────┘
                  │ Inyección de Dependencias
                  │
┌─────────────────▼───────────────────────────────────┐
│           CAPA DE LÓGICA DE NEGOCIO                 │
│                   (Services)                        │
│  ┌─────────────┬─────────────┬─────────────────────┐│
│  │ProductoSvc  │ UsuarioSvc  │   ClienteService    ││
│  │             │             │                     ││
│  └─────────────┴─────────────┴─────────────────────┘│
└─────────────────┬───────────────────────────────────┘
                  │ Spring Data JPA
                  │
┌─────────────────▼───────────────────────────────────┐
│            CAPA DE ACCESO A DATOS                   │
│                 (Repositories)                      │
│  ┌─────────────┬─────────────┬─────────────────────┐│
│  │ProductoRepo │ UsuarioRepo │   ClienteRepo       ││
│  │             │             │                     ││
│  └─────────────┴─────────────┴─────────────────────┘│
└─────────────────┬───────────────────────────────────┘
                  │ JDBC/Hibernate
                  │
┌─────────────────▼───────────────────────────────────┐
│                BASE DE DATOS                        │
│                  PostgreSQL                         │
│  ┌─────────────┬─────────────┬─────────────────────┐│
│  │  productos  │  usuarios   │     clientes        ││
│  │   pedidos   │movimientos  │    proveedores      ││
│  └─────────────┴─────────────┴─────────────────────┘│
└─────────────────────────────────────────────────────┘
```

---

## ✅ 6. VENTAJAS DE LA ARQUITECTURA IMPLEMENTADA

### **🎯 Patrón MVC:**
- **Separación clara de responsabilidades**
- **Facilita el mantenimiento y escalabilidad**
- **Permite desarrollo en paralelo de diferentes capas**
- **Reutilización de componentes**

### **🗃️ Patrón DAO:**
- **Abstracción del acceso a datos**
- **Facilita cambios en la base de datos**
- **Mejora la testabilidad del código**
- **Reutilización de operaciones CRUD**

### **🏢 Arquitectura por Capas:**
- **Modularidad y organización del código**
- **Fácil localización de funcionalidades**
- **Mantenimiento simplificado**
- **Escalabilidad horizontal y vertical**

---

## 🛠️ 7. TECNOLOGÍAS UTILIZADAS

| Capa | Tecnología | Propósito |
|------|------------|-----------|
| **Presentación** | Thymeleaf + Bootstrap 5 | Motor de plantillas y UI responsivo |
| **Controladores** | Spring MVC | Gestión de peticiones HTTP |
| **Lógica de Negocio** | Spring Boot + Spring Core | Inyección de dependencias y servicios |
| **Acceso a Datos** | Spring Data JPA | ORM y repositorios |
| **Base de Datos** | PostgreSQL | Sistema de gestión de BD relacional |
| **Seguridad** | Spring Security | Autenticación y autorización |

---

## 📝 8. CONCLUSIONES

La implementación de los patrones **MVC**, **DAO** y **Arquitectura por Capas** en el Sistema de Gestión de Inventario Saga Falabella ha resultado en:

1. **Código bien estructurado y mantenible**
2. **Separación clara de responsabilidades**
3. **Facilidad para realizar pruebas unitarias**
4. **Escalabilidad del sistema**
5. **Reutilización de componentes**
6. **Flexibilidad para futuros cambios**

El sistema cumple exitosamente con los requerimientos arquitectónicos solicitados, proporcionando una base sólida para la gestión eficiente del inventario empresarial.

---

**Desarrollado por:** Christopher Lincoln Rafaile Naupay  
**Framework:** Spring Boot 3.5.2  
**Base de Datos:** PostgreSQL 16.4  
**Fecha:** Junio 2025
