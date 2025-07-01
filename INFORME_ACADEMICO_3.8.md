# 3.8 DISEÑO DEL SISTEMA
## Sistema de Gestión de Inventario Saga Falabella

---

## 3.8.1 Patrón de Diseño

### Implementación de Patrones de Diseño MVC, DAO y Arquitectura por Capas

El sistema implementa una arquitectura robusta basada en tres patrones de diseño fundamentales que garantizan la separación de responsabilidades, mantenibilidad y escalabilidad del software.

#### **Patrón MVC (Modelo-Vista-Controlador)**

**Justificación:** El patrón MVC permite separar la lógica de presentación, la lógica de negocio y el manejo de datos, facilitando el mantenimiento y testing del código.

**Implementación:**
- **Modelo:** Entidades JPA que representan el estado de los datos
- **Vista:** Templates Thymeleaf que manejan la presentación
- **Controlador:** Clases Spring MVC que procesan las peticiones HTTP

```java
// CONTROLADOR - Maneja peticiones HTTP
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
            
        // Preparación del modelo para la vista
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        
        return "inventario/productos"; // Retorna la vista
    }
}

// MODELO - Entidad que representa los datos
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
    
    @Enumerated(EnumType.STRING)
    private CategoriaProducto categoria;
    
    // Constructores, getters y setters
}
```

#### **Patrón DAO (Data Access Object)**

**Justificación:** El patrón DAO encapsula el acceso a los datos, proporcionando una interfaz uniforme para las operaciones CRUD y consultas específicas.

**Implementación con Spring Data JPA:**

```java
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    // Consultas derivadas automáticamente por Spring Data
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    
    List<Producto> findByCategoria(CategoriaProducto categoria);
    
    List<Producto> findByCantidadStockLessThanAndActivoTrue(Integer stockMinimo);
    
    // Consulta personalizada con JPQL
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMin AND :precioMax")
    List<Producto> findByRangoPrecio(
        @Param("precioMin") BigDecimal precioMin,
        @Param("precioMax") BigDecimal precioMax);
        
    // Consulta nativa SQL para operaciones complejas
    @Query(value = "SELECT * FROM productos p WHERE p.cantidad_stock < p.stock_minimo", 
           nativeQuery = true)
    List<Producto> findProductosBajoStock();
}
```

#### **Arquitectura por Capas**

**Justificación:** La separación en capas permite una organización clara del código, facilitando el mantenimiento y la evolución del sistema.

**Estructura implementada:**

1. **Capa de Presentación (Controllers)**
   - Maneja las peticiones HTTP
   - Valida datos de entrada
   - Prepara modelos para las vistas

2. **Capa de Lógica de Negocio (Services)**
   - Implementa las reglas de negocio
   - Gestiona transacciones
   - Coordina operaciones entre repositorios

3. **Capa de Acceso a Datos (Repositories)**
   - Encapsula el acceso a la base de datos
   - Proporciona operaciones CRUD
   - Ejecuta consultas especializadas

4. **Capa de Entidades (Models)**
   - Define la estructura de los datos
   - Mapea entidades a tablas de base de datos
   - Establece relaciones entre entidades

```java
// CAPA DE SERVICIO - Lógica de negocio
@Service
@Transactional
public class ProductoService {
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private MovimientoInventarioRepository movimientoRepository;
    
    public Producto actualizarStock(Long productoId, Integer nuevaCantidad, String motivo) {
        // Validaciones de negocio
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));
            
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        
        // Registrar movimiento para auditoría
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setCantidadAnterior(producto.getCantidadStock());
        movimiento.setCantidadNueva(nuevaCantidad);
        movimiento.setMotivo(motivo);
        movimiento.setFecha(LocalDateTime.now());
        
        // Actualizar stock
        producto.setCantidadStock(nuevaCantidad);
        producto.setFechaActualizacion(LocalDateTime.now());
        
        // Persistir cambios
        movimientoRepository.save(movimiento);
        return productoRepository.save(producto);
    }
}
```

---

## 3.8.2 Diagrama de Estructura Compuesta

### Definición y Propósito
El diagrama de estructura compuesta muestra la estructura interna de una clase y las colaboraciones que esta estructura permite. En nuestro sistema, representa cómo los componentes internos del `InventarioController` colaboran para gestionar las operaciones de inventario.

### Diagrama de Estructura Compuesta: InventarioController

```
┌─────────────────────────────────────────────────────────────────┐
│                    :InventarioController                       │
│                                                                 │
│  ┌─────────────────┐                    ┌─────────────────┐     │
│  │                 │     delegación     │                 │     │
│  │ requestHandler  │◄──────────────────►│ businessLogic   │     │
│  │ :HttpHandler    │     coordinación   │ :ServiceLayer   │     │
│  │                 │                    │                 │     │
│  └─────────────────┘                    └─────────────────┘     │
│           │                                       │             │
│           │ HTTP Request/Response                 │ service     │
│           │                                       │ calls       │
│           ▼                                       ▼             │
│  ┌─────────────────┐                    ┌─────────────────┐     │
│  │                 │                    │                 │     │
│  │ viewRenderer    │                    │ dataAccess      │     │
│  │ :ThymeleafView  │                    │ :RepositoryDAO  │     │
│  │                 │                    │                 │     │
│  └─────────────────┘                    └─────────────────┘     │
│           │                                       │             │
│           │ model binding                         │ entity      │
│           │                                       │ mapping     │
│           ▼                                       ▼             │
│  ┌─────────────────┐                    ┌─────────────────┐     │
│  │                 │                    │                 │     │
│  │ modelData      │◄──────────────────►│ persistenceCtx  │     │
│  │ :ModelMap      │    data flow       │ :EntityManager  │     │
│  │                │                    │                 │     │
│  └─────────────────┘                    └─────────────────┘     │
└─────────────────────────────────────────────────────────────────┘

Conectores:
──────────► : Flujo de datos/control
◄─────────► : Colaboración bidireccional
```

### Elementos del Diagrama:

1. **requestHandler**: Componente que procesa las peticiones HTTP entrantes
2. **businessLogic**: Encapsula la lógica de negocio del inventario
3. **viewRenderer**: Responsable de renderizar las vistas Thymeleaf
4. **dataAccess**: Maneja el acceso a los datos a través de repositorios
5. **modelData**: Almacena los datos del modelo para la vista
6. **persistenceCtx**: Contexto de persistencia JPA

### Flujo de Colaboración:
1. `requestHandler` recibe petición HTTP
2. Delega procesamiento a `businessLogic`
3. `businessLogic` coordina con `dataAccess` para operaciones de datos
4. `dataAccess` interactúa con `persistenceCtx` para persistencia
5. Los datos fluyen de vuelta a `modelData`
6. `viewRenderer` utiliza `modelData` para generar la respuesta

---

## 3.8.3 Diagrama de Tiempo

### Definición y Propósito
El diagrama de tiempo muestra el comportamiento de los objetos a lo largo del tiempo, representando los cambios de estado y las interacciones temporales durante la ejecución de un caso de uso específico.

### Diagrama de Tiempo: Proceso de Actualización de Stock

```
Tiempo (ms) │ UsuarioWeb │ Controller │   Service   │ Repository │   Database
            │            │            │             │            │
    0       │    ●       │            │             │            │
            │    │       │            │             │            │
   10       │────┼──────►│     ●      │             │            │
            │    │       │     │      │             │            │ POST /actualizar-stock
   20       │    │       │─────┼─────►│      ●      │            │
            │    │       │     │      │      │      │            │ actualizarStock()
   30       │    │       │     │      │──────┼─────►│     ●      │
            │    │       │     │      │      │      │     │      │ findById()
   40       │    │       │     │      │      │      │─────┼─────►│  ●
            │    │       │     │      │      │      │     │      │  │ SELECT query
   50       │    │       │     │      │      │      │◄────┼──────│  │
            │    │       │     │      │      │      │     ●      │  │ Producto entity
   60       │    │       │     │      │◄─────┼──────│            │  │
            │    │       │     │      │      ●      │            │  │ validación negocio
   80       │    │       │     │      │──────┼─────►│     ●      │  │
            │    │       │     │      │      │      │     │      │  │ save()
   90       │    │       │     │      │      │      │─────┼─────►│  ●
            │    │       │     │      │      │      │     │      │  │ UPDATE query
  100       │    │       │     │      │      │      │◄────┼──────│  │
            │    │       │     │      │      │      │     ●      │  │ confirmación
  110       │    │       │     │      │◄─────┼──────│            │  │
            │    │       │     │      │      ●      │            │  │ Producto actualizado
  120       │    │       │◄────┼──────│             │            │  │
            │    │       │     ●      │             │            │  │ modelo preparado
  130       │    │◄──────│            │             │            │  │
            │    ●       │            │             │            │  │ HTML response
            │            │            │             │            │
Estado:     │ Esperando  │   Activo   │   Activo    │   Activo   │ Conectada
            │ Navegando  │ Procesando │ Validando   │ Ejecutando │ Transacción
            │ Recibiendo │   Idle     │    Idle     │    Idle    │    Idle

Líneas de Vida:
● : Estado activo
─ : Estado inactivo/esperando
► : Mensaje enviado
◄ : Respuesta recibida
```

### Análisis Temporal:
- **Latencia total**: 130ms para operación completa
- **Tiempo de validación**: 20ms (líneas 60-80)
- **Tiempo de persistencia**: 20ms (líneas 80-100)
- **Tiempo de renderizado**: 10ms (líneas 120-130)

### Estados Críticos:
1. **[0-10ms]**: Interacción del usuario
2. **[10-30ms]**: Routing y delegación
3. **[30-60ms]**: Consulta de datos existentes
4. **[60-80ms]**: Validación de reglas de negocio
5. **[80-100ms]**: Persistencia de cambios
6. **[100-130ms]**: Preparación y envío de respuesta

---

## 3.8.4 Diagrama de Despliegue

### Definición y Propósito
El diagrama de despliegue muestra la configuración de elementos de procesamiento en tiempo de ejecución y los componentes de software que se ejecutan en ellos.

### Arquitectura de Despliegue del Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                        ENTORNO DE PRODUCCIÓN                   │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────┐         ┌─────────────────────┐
│   Cliente Web       │         │   Cliente Móvil     │
│                     │         │                     │
│  ┌─────────────────┐│         │  ┌─────────────────┐│
│  │ Chrome/Firefox  ││         │  │ App Nativa      ││
│  │ JavaScript      ││         │  │ React Native    ││
│  │ Bootstrap CSS   ││         │  │                 ││
│  └─────────────────┘│         │  └─────────────────┘│
└─────────────────────┘         └─────────────────────┘
           │                               │
           │ HTTPS (443)                   │ HTTPS (443)
           │                               │
           └─────────────┬─────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BALANCEADOR DE CARGA                        │
│                         Nginx                                   │
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │ SSL Termination │  │ Load Balancing  │  │ Static Content  │  │
│  │ Certificados    │  │ Round Robin     │  │ CSS/JS/Images   │  │
│  │ Let's Encrypt   │  │ Health Checks   │  │ Caching         │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                         │
                         │ HTTP (8080)
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SERVIDOR DE APLICACIONES                    │
│                      Ubuntu Server 20.04                       │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                   Spring Boot Application                  │ │
│  │                                                             │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │ │
│  │  │ Embedded    │ │ Thymeleaf   │ │ Spring Security     │   │ │
│  │  │ Tomcat      │ │ Templates   │ │ Authentication      │   │ │
│  │  │ (Puerto     │ │ Engine      │ │ & Authorization     │   │ │
│  │  │ 8080)       │ │             │ │                     │   │ │
│  │  └─────────────┘ └─────────────┘ └─────────────────────┘   │ │
│  │                                                             │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │ │
│  │  │ MVC         │ │ Service     │ │ Repository Layer    │   │ │
│  │  │ Controllers │ │ Layer       │ │ Spring Data JPA     │   │ │
│  │  │             │ │ Business    │ │ Hibernate ORM       │   │ │
│  │  │             │ │ Logic       │ │                     │   │ │
│  │  └─────────────┘ └─────────────┘ └─────────────────────┘   │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Especificaciones Hardware:                                    │
│  • CPU: 4 vCores Intel Xeon                                    │
│  • RAM: 8 GB DDR4                                              │
│  • Storage: 100 GB SSD                                         │
│  • OS: Ubuntu Server 20.04 LTS                                 │
│  • JVM: OpenJDK 17                                             │
└─────────────────────────────────────────────────────────────────┘
                         │
                         │ JDBC (5432)
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SERVIDOR DE BASE DE DATOS                   │
│                         PostgreSQL 14                          │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    PostgreSQL Instance                     │ │
│  │                                                             │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │ │
│  │  │ Connection  │ │ Query       │ │ Transaction         │   │ │
│  │  │ Pool        │ │ Optimizer   │ │ Manager             │   │ │
│  │  │ (Max 20)    │ │             │ │ ACID Compliance     │   │ │
│  │  └─────────────┘ └─────────────┘ └─────────────────────┘   │ │
│  │                                                             │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │                  Database Schema                        │ │ │
│  │  │                                                         │ │ │
│  │  │  • productos (tabla principal)                         │ │ │
│  │  │  • usuarios (autenticación)                            │ │ │
│  │  │  • clientes (información comercial)                    │ │ │
│  │  │  • pedidos (órdenes de compra)                         │ │ │
│  │  │  • movimientos_inventario (auditoría)                  │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Especificaciones Hardware:                                    │
│  • CPU: 2 vCores Intel Xeon                                    │
│  • RAM: 4 GB DDR4                                              │
│  • Storage: 200 GB SSD (con replicación)                       │
│  • OS: Ubuntu Server 20.04 LTS                                 │
│  • Backup: Automático cada 6 horas                             │
└─────────────────────────────────────────────────────────────────┘
                         │
                         │ Replicación
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                   SERVIDOR DE RESPALDO                         │
│                    PostgreSQL Replica                          │
│                                                                 │
│  • Replicación en tiempo real                                  │
│  • Failover automático                                         │
│  • Backup incremental diario                                   │
│  • Monitoreo continuo                                          │
└─────────────────────────────────────────────────────────────────┘

PROTOCOLOS DE COMUNICACIÓN:
┌─────────────────┬─────────────────┬─────────────────┬─────────────────┐
│ Componente      │ Protocolo       │ Puerto          │ Descripción     │
├─────────────────┼─────────────────┼─────────────────┼─────────────────┤
│ Cliente ↔ Nginx │ HTTPS/HTTP      │ 443/80          │ Web Traffic     │
│ Nginx ↔ Spring  │ HTTP            │ 8080            │ Reverse Proxy   │
│ Spring ↔ PostgreSQL │ JDBC        │ 5432            │ Database Conn   │
│ Admin ↔ Servers │ SSH             │ 22              │ Remote Access   │
│ Monitoring      │ SNMP            │ 161             │ System Monitor  │
└─────────────────┴─────────────────┴─────────────────┴─────────────────┘
```

### Características del Despliegue:

1. **Alta Disponibilidad**: Replicación de base de datos y balanceador de carga
2. **Seguridad**: HTTPS, SSH, firewall configurado
3. **Escalabilidad**: Posibilidad de añadir más servidores de aplicación
4. **Monitoreo**: Logs centralizados y métricas de rendimiento
5. **Backup**: Estrategia de respaldo automático

---

## 3.8.5 Diagrama de Componentes

### Definición y Propósito
El diagrama de componentes muestra la organización y dependencias entre los componentes de software, incluyendo interfaces, puertos y conectores.

### Arquitectura de Componentes del Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                      SISTEMA INVENTARIO SAGA                   │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                   CAPA DE PRESENTACIÓN                         │
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌──────────────┐ │
│  │                 │    │                 │    │              │ │
│  │ Web Controllers │    │ REST Controllers│    │ View Layer   │ │
│  │                 │    │                 │    │              │ │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌──────────┐ │ │
│  │ │HomeCtrl     │ │    │ │ProductoAPI  │ │    │ │Thymeleaf │ │ │
│  │ │InventarioCtrl│ │    │ │UsuarioAPI  │ │    │ │Templates │ │ │
│  │ │AuthCtrl     │ │◄───┤ │ClienteAPI   │ │◄───┤ │Bootstrap │ │ │
│  │ │RegistroCtrl │ │    │ │DashboardAPI │ │    │ │CSS/JS    │ │ │
│  │ └─────────────┘ │    │ └─────────────┘ │    │ └──────────┘ │ │
│  └─────────────────┘    └─────────────────┘    └──────────────┘ │
│           │                       │                     │       │
│           │ @RequestMapping       │ @RestController     │ Model │
│           │                       │                     │       │
└───────────┼───────────────────────┼─────────────────────┼───────┘
            │                       │                     │
            ▼                       ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                  CAPA DE LÓGICA DE NEGOCIO                     │
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌──────────────┐ │
│  │                 │    │                 │    │              │ │
│  │ Business Services│    │ Security        │    │ Validation   │ │
│  │                 │    │ Services        │    │ Services     │ │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌──────────┐ │ │
│  │ │ProductoSvc  │ │    │ │AuthService  │ │    │ │InputValid│ │ │
│  │ │UsuarioSvc   │ │◄───┤ │SecurityConf │ │◄───┤ │DataValid │ │ │
│  │ │ClienteSvc   │ │    │ │JWTProvider  │ │    │ │BusRules  │ │ │
│  │ │InventarioSvc│ │    │ │UserDetails  │ │    │ │Exception │ │ │
│  │ └─────────────┘ │    │ └─────────────┘ │    │ │Handler   │ │ │
│  └─────────────────┘    └─────────────────┘    │ └──────────┘ │ │
│           │                       │            └──────────────┘ │
│           │ @Service              │ @Component         │        │
│           │ @Transactional        │                    │        │
└───────────┼───────────────────────┼────────────────────┼────────┘
            │                       │                    │
            ▼                       ▼                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                   CAPA DE ACCESO A DATOS                       │
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌──────────────┐ │
│  │                 │    │                 │    │              │ │
│  │ JPA Repositories│    │ Custom          │    │ Database     │ │
│  │                 │    │ Repositories    │    │ Configuration│ │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌──────────┐ │ │
│  │ │ProductoRepo │ │    │ │CustomQuery  │ │    │ │DataSource│ │ │
│  │ │UsuarioRepo  │ │◄───┤ │ReportRepo   │ │◄───┤ │JpaConfig │ │ │
│  │ │ClienteRepo  │ │    │ │AnalyticsRepo│ │    │ │TxManager │ │ │
│  │ │MovimientoRepo│ │    │ │AuditRepo    │ │    │ │ConnPool  │ │ │
│  │ └─────────────┘ │    │ └─────────────┘ │    │ └──────────┘ │ │
│  └─────────────────┘    └─────────────────┘    └──────────────┘ │
│           │                       │                     │       │
│           │ @Repository           │ @Component          │ @Bean │
│           │ JpaRepository<>       │                     │       │
└───────────┼───────────────────────┼─────────────────────┼───────┘
            │                       │                     │
            ▼                       ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                     CAPA DE PERSISTENCIA                       │
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌──────────────┐ │
│  │                 │    │                 │    │              │ │
│  │ Entity Models   │    │ ORM Framework   │    │ Database     │ │
│  │                 │    │                 │    │ Driver       │ │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌──────────┐ │ │
│  │ │Producto     │ │    │ │Hibernate    │ │    │ │PostgreSQL│ │ │
│  │ │Usuario      │ │◄───┤ │EntityMgr    │ │◄───┤ │JDBC      │ │ │
│  │ │Cliente      │ │    │ │SessionFact  │ │    │ │Driver    │ │ │
│  │ │Pedido       │ │    │ │QueryBuilder │ │    │ │42.5.0    │ │ │
│  │ │Movimiento   │ │    │ │CacheL2      │ │    │ └──────────┘ │ │
│  │ └─────────────┘ │    │ └─────────────┘ │    └──────────────┘ │
│  └─────────────────┘    └─────────────────┘            │       │
│           │                       │                     │       │
│           │ @Entity               │ JPA 2.2             │ JDBC  │
│           │ @Table                │                     │       │
└───────────┼───────────────────────┼─────────────────────┼───────┘
            │                       │                     │
            └─────────────┬─────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BASE DE DATOS FÍSICA                        │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    PostgreSQL 14                           │ │
│  │                                                             │ │
│  │  Esquema: sistema_inventario                                │ │
│  │                                                             │ │
│  │  Tablas:                                                    │ │
│  │  ├── productos (id, nombre, precio, stock, categoria...)   │ │
│  │  ├── usuarios (id, username, email, password, rol...)      │ │
│  │  ├── clientes (id, nombre, email, telefono, direccion...)  │ │
│  │  ├── pedidos (id, cliente_id, fecha, estado, total...)     │ │
│  │  ├── movimientos_inventario (id, producto_id, tipo...)     │ │
│  │  └── auditoría y logs del sistema                          │ │
│  │                                                             │ │
│  │  Índices:                                                   │ │
│  │  ├── idx_producto_nombre (productos.nombre)                │ │
│  │  ├── idx_usuario_email (usuarios.email)                    │ │
│  │  ├── idx_pedido_fecha (pedidos.fecha_creacion)             │ │
│  │  └── idx_movimiento_fecha (movimientos.fecha)              │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘

INTERFACES Y CONECTORES:
┌──────────────────┬──────────────────┬─────────────────────────────┐
│ Componente Origen│ Componente Destino│ Interfaz/Protocolo         │
├──────────────────┼──────────────────┼─────────────────────────────┤
│ Controllers      │ Services         │ @Autowired Injection       │
│ Services         │ Repositories     │ @Autowired Injection       │
│ Repositories     │ EntityManager    │ JPA @PersistenceContext    │
│ Hibernate        │ PostgreSQL       │ JDBC Connection Pool       │
│ Thymeleaf        │ Controllers      │ ModelAndView Interface     │
│ Security         │ All Layers       │ @PreAuthorize Annotations  │
└──────────────────┴──────────────────┴─────────────────────────────┘
```

### Análisis de Dependencias:

1. **Acoplamiento Bajo**: Cada capa depende solo de la inmediatamente inferior
2. **Cohesión Alta**: Componentes relacionados agrupados por responsabilidad
3. **Inversión de Dependencias**: Uso de interfaces para abstraer implementaciones
4. **Inyección de Dependencias**: Spring maneja automáticamente las dependencias

### Beneficios de la Arquitectura:

1. **Mantenibilidad**: Fácil modificación de componentes individuales
2. **Testabilidad**: Posibilidad de mockear dependencias
3. **Reutilización**: Componentes reutilizables en diferentes contextos
4. **Escalabilidad**: Posibilidad de distribuir componentes

---

## Conclusiones

La implementación del Sistema de Gestión de Inventario Saga Falabella demuestra la aplicación exitosa de patrones de diseño reconocidos en la industria:

1. **Patrón MVC**: Garantiza separación clara de responsabilidades
2. **Patrón DAO**: Encapsula eficientemente el acceso a datos
3. **Arquitectura por Capas**: Proporciona estructura organizacional robusta

Los diagramas UML presentados (estructura compuesta, tiempo, despliegue y componentes) ofrecen una visión completa de la arquitectura del sistema, facilitando su comprensión, mantenimiento y evolución futura.

Esta documentación cumple con los requerimientos académicos establecidos y proporciona una base sólida para el desarrollo continuo del proyecto.
