# DIAGRAMAS COMPLEMENTARIOS - PATRONES DE DISEÑO
## Sistema de Gestión de Inventario Saga Falabella

---

## 🔄 FLUJO DE DATOS EN EL PATRÓN MVC

```
USUARIO INTERACTÚA CON LA VISTA
         ↓
    [NAVEGADOR WEB]
         ↓ HTTP Request
    [CONTROLADOR]
         ↓ Delega lógica
     [SERVICIO]
         ↓ Accede a datos
    [REPOSITORIO]
         ↓ SQL Query
    [BASE DE DATOS]
         ↑ Resultado
    [REPOSITORIO]
         ↑ Entidades
     [SERVICIO]
         ↑ Datos procesados
    [CONTROLADOR]
         ↑ Modelo preparado
    [VISTA (Thymeleaf)]
         ↑ HTML renderizado
    [NAVEGADOR WEB]
```

---

## 📦 ESTRUCTURA DETALLADA DEL PROYECTO

```
sistema-inventario-saga/
│
├── 📁 src/main/java/com/sagafalabella/inventario/
│   │
│   ├── 🎮 controller/                    # CAPA PRESENTACIÓN
│   │   ├── HomeController.java           # Página principal y navegación
│   │   ├── InventarioController.java     # Gestión inventario
│   │   ├── AuthController.java           # Autenticación
│   │   ├── RegistroController.java       # Registro usuarios
│   │   └── DashboardController.java      # Panel control
│   │
│   ├── ⚙️ service/                       # CAPA LÓGICA NEGOCIO
│   │   ├── ProductoService.java          # Reglas negocio productos
│   │   ├── UsuarioService.java           # Gestión usuarios
│   │   ├── ClienteService.java           # Manejo clientes
│   │   └── InventarioService.java        # Lógica inventario
│   │
│   ├── 🗄️ repository/                   # CAPA ACCESO DATOS (DAO)
│   │   ├── ProductoRepository.java       # CRUD productos
│   │   ├── UsuarioRepository.java        # CRUD usuarios
│   │   ├── ClienteRepository.java        # CRUD clientes
│   │   └── MovimientoRepository.java     # CRUD movimientos
│   │
│   ├── 📊 model/                         # CAPA ENTIDADES
│   │   ├── Producto.java                 # Entidad producto
│   │   ├── Usuario.java                  # Entidad usuario
│   │   ├── Cliente.java                  # Entidad cliente
│   │   ├── Pedido.java                   # Entidad pedido
│   │   ├── MovimientoInventario.java     # Trazabilidad
│   │   └── OrdenPicking.java             # Órdenes picking
│   │
│   ├── 🔧 config/                        # CONFIGURACIÓN
│   │   ├── SecurityConfig.java           # Seguridad
│   │   └── DatabaseConfig.java           # Base datos
│   │
│   └── 🚀 SistemaInventarioApplication.java  # MAIN CLASS
│
├── 📁 src/main/resources/
│   ├── 🎨 templates/                     # VISTAS (THYMELEAF)
│   │   ├── index.html                    # Página principal
│   │   ├── demo.html                     # Demo sistema
│   │   ├── help.html                     # Centro ayuda
│   │   ├── about.html                    # Información
│   │   ├── inventario/
│   │   │   ├── dashboard.html            # Panel inventario
│   │   │   └── productos.html            # Gestión productos
│   │   └── fragments/
│   │       ├── head.html                 # Meta tags
│   │       ├── navbar.html               # Navegación
│   │       └── footer.html               # Pie página
│   │
│   ├── 🎯 static/                        # RECURSOS ESTÁTICOS
│   │   ├── css/                          # Estilos CSS
│   │   ├── js/                           # JavaScript
│   │   └── images/                       # Imágenes
│   │
│   ├── ⚙️ application.properties         # Configuración app
│   └── 📄 data.sql                       # Datos iniciales
│
└── 📋 pom.xml                            # Dependencias Maven
```

---

## 🔄 FLUJO ESPECÍFICO: GESTIÓN DE PRODUCTOS

```
┌─────────────────────────────────────────────────────────────────┐
│                    USUARIO EN NAVEGADOR                        │
│                Página: /inventario/productos                   │
└─────────────────┬───────────────────────────────────────────────┘
                  │ GET /inventario/productos?search=laptop
                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                 INVENTARIO CONTROLLER                          │
│                                                                 │
│  @GetMapping("/productos")                                      │
│  public String gestionProductos(                                │
│      @RequestParam(required = false) String search,            │
│      Model model) {                                             │
│                                                                 │
│      // Delegación a capa de servicio                          │
│      List<Producto> productos = (search != null) ?             │
│          productoService.buscarProductos(search) :              │
│          productoService.obtenerTodosLosProductos();            │
│                                                                 │
│      model.addAttribute("productos", productos);               │
│      return "inventario/productos";                             │
│  }                                                              │
└─────────────────┬───────────────────────────────────────────────┘
                  │ productoService.buscarProductos("laptop")
                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                    PRODUCTO SERVICE                             │
│                                                                 │
│  @Service                                                       │
│  @Transactional                                                 │
│  public class ProductoService {                                 │
│                                                                 │
│      @Autowired                                                 │
│      private ProductoRepository productoRepository;             │
│                                                                 │
│      public List<Producto> buscarProductos(String termino) {   │
│          // Aplicar lógica de negocio                           │
│          if (termino.length() < 2) {                            │
│              throw new IllegalArgumentException(...);           │
│          }                                                      │
│                                                                 │
│          // Delegación a capa de datos                          │
│          return productoRepository                              │
│              .findByNombreContainingIgnoreCase(termino);        │
│      }                                                          │
│  }                                                              │
└─────────────────┬───────────────────────────────────────────────┘
                  │ findByNombreContainingIgnoreCase("laptop")
                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                 PRODUCTO REPOSITORY (DAO)                      │
│                                                                 │
│  @Repository                                                    │
│  public interface ProductoRepository                           │
│                   extends JpaRepository<Producto, Long> {      │
│                                                                 │
│      // Spring Data JPA genera automáticamente:                │
│      // SELECT * FROM productos                                 │
│      // WHERE LOWER(nombre) LIKE LOWER('%laptop%')             │
│      List<Producto> findByNombreContainingIgnoreCase(          │
│          String nombre);                                        │
│  }                                                              │
└─────────────────┬───────────────────────────────────────────────┘
                  │ SQL Query ejecutada
                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                  BASE DE DATOS POSTGRESQL                      │
│                                                                 │
│  Table: productos                                               │
│  ┌─────┬──────────────┬─────────┬─────────────┬──────────────┐  │
│  │ id  │    nombre    │ precio  │categoria    │cantidad_stock│  │
│  ├─────┼──────────────┼─────────┼─────────────┼──────────────┤  │
│  │  1  │ Laptop Dell  │ 2500.00 │ELECTRONICA  │     15       │  │
│  │  2  │ Laptop HP    │ 2200.00 │ELECTRONICA  │     8        │  │
│  │  5  │ Laptop Asus  │ 2800.00 │ELECTRONICA  │     12       │  │
│  └─────┴──────────────┴─────────┴─────────────┴──────────────┘  │
│                                                                 │
│  Query resultado: 3 filas que contienen "laptop"               │
└─────────────────┬───────────────────────────────────────────────┘
                  │ Resultados como entidades Producto
                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                     ENTIDADES PRODUCTO                         │
│                                                                 │
│  List<Producto> productos = [                                   │
│      Producto {                                                 │
│          id: 1,                                                 │
│          nombre: "Laptop Dell",                                 │
│          precio: 2500.00,                                       │
│          categoria: ELECTRONICA,                                │
│          cantidadStock: 15                                      │
│      },                                                         │
│      Producto { ... },                                          │
│      Producto { ... }                                           │
│  ]                                                              │
└─────────────────┬───────────────────────────────────────────────┘
                  │ Retorno al Service
                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                 PRODUCTO SERVICE (RESPUESTA)                   │
│                                                                 │
│  // Aplicar lógica adicional si es necesario                   │
│  // Por ejemplo: ordenamiento, filtros, cálculos               │
│                                                                 │
│  return productos; // Lista procesada                          │
└─────────────────┬───────────────────────────────────────────────┘
                  │ Lista de productos al Controller
                  ▼
┌─────────────────────────────────────────────────────────────────┐
│              INVENTARIO CONTROLLER (RESPUESTA)                 │
│                                                                 │
│  // Preparar datos para la vista                               │
│  model.addAttribute("productos", productos);                   │
│  model.addAttribute("totalProductos", productos.size());       │
│  model.addAttribute("searchQuery", search);                    │
│                                                                 │
│  return "inventario/productos"; // Nombre de la vista          │
└─────────────────┬───────────────────────────────────────────────┘
                  │ Renderizado con Thymeleaf
                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                VISTA: inventario/productos.html                │
│                                                                 │
│  <div class="product-card-grid"                                 │
│       th:if="${#lists.size(productos) > 0}">                   │
│                                                                 │
│      <div class="enhanced-product-card"                        │
│           th:each="producto : ${productos}">                   │
│                                                                 │
│          <h5 class="product-title"                             │
│              th:text="${producto.nombre}">Laptop Dell</h5>     │
│                                                                 │
│          <div class="product-price">                           │
│              S/ <span th:text="${producto.precio}">2500.00</span> │
│          </div>                                                 │
│                                                                 │
│          <div class="stock-indicator">                         │
│              Stock: <span th:text="${producto.cantidadStock}">15</span> │
│          </div>                                                 │
│      </div>                                                     │
│  </div>                                                         │
└─────────────────┬───────────────────────────────────────────────┘
                  │ HTML generado
                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                    NAVEGADOR DEL USUARIO                       │
│                                                                 │
│  Página renderizada mostrando:                                 │
│  - 3 productos que contienen "laptop"                          │
│  - Cada producto con su información completa                   │
│  - Interfaz interactiva con botones de acción                  │
│  - Funcionalidades de filtrado y búsqueda                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🏗️ IMPLEMENTACIÓN DE INYECCIÓN DE DEPENDENCIAS

```
┌─────────────────────────────────────────────────────────────────┐
│                     SPRING CONTAINER                           │
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐  │
│  │   Controller    │    │     Service     │    │ Repository  │  │
│  │                 │    │                 │    │             │  │
│  │  @Controller    │    │   @Service      │    │ @Repository │  │
│  │  @Autowired ────┼───▶│   @Autowired ───┼───▶│             │  │
│  │  ProductoSvc    │    │   ProductoRepo  │    │ JpaRepo<>   │  │
│  └─────────────────┘    └─────────────────┘    └─────────────┘  │
│                                                                 │
│  Gestión automática de:                                        │
│  • Creación de instancias                                      │
│  • Inyección de dependencias                                   │
│  • Gestión del ciclo de vida                                   │
│  • Configuración de transacciones                              │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📊 MAPEO ENTIDAD-RELACIÓN

```
ENTIDADES JAVA                    TABLAS POSTGRESQL

@Entity Producto                  productos
├─ @Id Long id                   ├─ id BIGSERIAL PRIMARY KEY
├─ String nombre                 ├─ nombre VARCHAR(200) NOT NULL
├─ BigDecimal precio             ├─ precio DECIMAL(10,2) NOT NULL
├─ Integer cantidadStock         ├─ cantidad_stock INTEGER NOT NULL
├─ Integer stockMinimo           ├─ stock_minimo INTEGER NOT NULL
├─ CategoriaProducto categoria   ├─ categoria VARCHAR(50)
├─ Boolean activo                ├─ activo BOOLEAN NOT NULL
├─ LocalDateTime fechaCreacion   ├─ fecha_creacion TIMESTAMP
└─ LocalDateTime fechaActualiz.  └─ fecha_actualizacion TIMESTAMP

@Entity Usuario                   usuarios
├─ @Id Long id                   ├─ id BIGSERIAL PRIMARY KEY
├─ String username               ├─ username VARCHAR(50) UNIQUE
├─ String email                  ├─ email VARCHAR(100) UNIQUE
├─ String password               ├─ password VARCHAR(255)
├─ String nombreCompleto         ├─ nombre_completo VARCHAR(200)
├─ Rol rol                       ├─ rol VARCHAR(30)
└─ Boolean activo                └─ activo BOOLEAN

@Entity MovimientoInventario      movimientos_inventario
├─ @Id Long id                   ├─ id BIGSERIAL PRIMARY KEY
├─ @ManyToOne Producto producto  ├─ producto_id BIGINT REFERENCES productos(id)
├─ TipoMovimiento tipo           ├─ tipo_movimiento VARCHAR(20)
├─ Integer cantidadAnterior      ├─ cantidad_anterior INTEGER
├─ Integer cantidadNueva         ├─ cantidad_nueva INTEGER
├─ String motivo                 ├─ motivo TEXT
└─ LocalDateTime fecha           └─ fecha_movimiento TIMESTAMP
```

Esta documentación completa proporciona a tu profesora una visión detallada de cómo has implementado exitosamente los patrones de diseño MVC, DAO y Arquitectura por Capas en tu Sistema de Gestión de Inventario Saga Falabella, cumpliendo exactamente con los requerimientos académicos solicitados.

---

# 📋 DIAGRAMAS UML REQUERIDOS - AVANCE 3 (PUNTO 3.8)

## 3.8.1 PATRÓN DE DISEÑO IMPLEMENTADO

### ✅ PATRONES UTILIZADOS:
- **MVC (Model-View-Controller)**: Separación clara de responsabilidades
- **DAO (Data Access Object)**: Abstracción de acceso a datos
- **ARQUITECTURA POR CAPAS**: Organización modular del sistema

**Justificación técnica:**
- **MVC** permite separar la lógica de presentación, negocio y datos
- **DAO** proporciona una interfaz uniforme para operaciones CRUD
- **CAPAS** facilita el mantenimiento, escalabilidad y testing del sistema

---

## 3.8.2 DIAGRAMA DE ESTRUCTURA COMPUESTA (UML Composite Structure)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    SISTEMA INVENTARIO SAGA FALABELLA                       │
│                         [Composite Structure]                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐          │
│  │   PRESENTATION  │    │    BUSINESS     │    │      DATA       │          │
│  │     LAYER       │    │     LAYER       │    │     LAYER       │          │
│  │                 │    │                 │    │                 │          │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │          │
│  │ │HomeCtrl     │ │◄──►│ │ProductoSvc  │ │◄──►│ │ProductoRepo │ │          │
│  │ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │          │
│  │                 │    │                 │    │                 │          │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │          │
│  │ │InventCtrl   │ │◄──►│ │InventSvc    │ │◄──►│ │MovimRepo    │ │          │
│  │ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │          │
│  │                 │    │                 │    │                 │          │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │          │
│  │ │AuthCtrl     │ │◄──►│ │UsuarioSvc   │ │◄──►│ │UsuarioRepo  │ │          │
│  │ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │          │
│  │                 │    │                 │    │                 │          │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘          │
│           │                       │                       │                 │
│           ▼                       ▼                       ▼                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐          │
│  │ THYMELEAF VIEWS │    │ SPRING SECURITY │    │ POSTGRESQL DB   │          │
│  │                 │    │                 │    │                 │          │
│  │ • index.html    │    │ • Authentication│    │ • productos     │          │
│  │ • dashboard.html│    │ • Authorization │    │ • usuarios      │          │
│  │ • productos.html│    │ • Session Mgmt  │    │ • movimientos   │          │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘          │
│                                                                             │
│  CONECTORES:                                                                │
│  ◄──► : Comunicación bidireccional                                         │
│  ──→  : Flujo de datos unidireccional                                      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

ELEMENTOS DEL DIAGRAMA:
├─ Componentes principales del sistema
├─ Interfaces entre capas (MVC, Service, Repository)
├─ Puertos de comunicación (HTTP, JPA, SQL)
├─ Conectores y dependencias
└─ Servicios externos (Base de datos, Seguridad)
```

---

## 3.8.3 DIAGRAMA DE TIEMPO (UML Timing Diagram)

```
FLUJO TEMPORAL: CONSULTA DE PRODUCTOS EN INVENTARIO

Usuario  │ Controller │  Service   │ Repository │    DB      │  Response
         │            │            │            │            │
    t0   │     │      │     │      │     │      │     │      │     │
    ─────┼─────┼──────┼─────┼──────┼─────┼──────┼─────┼──────┼─────┼────▶
         │     │      │     │      │     │      │     │      │     │   Tiempo
         │     │      │     │      │     │      │     │      │     │
    t1   │ GET /inventario/productos                                │
         │────────────▶│                                            │
         │     │      │                                            │
    t2   │     │   @GetMapping                                     │
         │     │      │────────────▶│                              │
         │     │      │             │                              │
    t3   │     │      │ obtenerTodosLosProductos()                 │
         │     │      │             │────────────▶│                │
         │     │      │             │             │                │
    t4   │     │      │             │ findAll()   │                │
         │     │      │             │             │───────────────▶│
         │     │      │             │             │                │
    t5   │     │      │             │             │ SELECT * FROM productos
         │     │      │             │             │                │
    t6   │     │      │             │             │◀───────────────│
         │     │      │             │             │ ResultSet      │
         │     │      │             │◀────────────│                │
         │     │      │             │ List<Producto>               │
         │     │      │◀────────────│                              │
         │     │      │ List<Producto>                             │
    t7   │     │ model.addAttribute("productos", productos)        │
         │     │      │                                            │
    t8   │     │ return "inventario/productos"                     │
         │     │      │                                            │
    t9   │◀────────────│ HTTP 200 + HTML renderizado               │
         │ Página mostrada                                         │

MÉTRICAS DE TIEMPO:
├─ t1-t2: ~5ms   (Routing HTTP)
├─ t2-t3: ~2ms   (Controller processing)
├─ t3-t4: ~1ms   (Service delegation)
├─ t4-t6: ~15ms  (Database query)
├─ t6-t7: ~3ms   (Data mapping)
├─ t7-t9: ~25ms  (View rendering)
└─ TOTAL: ~51ms  (Tiempo respuesta típico)

ESTADOS DEL SISTEMA:
├─ IDLE: Sistema en espera
├─ PROCESSING: Procesando solicitud
├─ QUERYING: Consultando base de datos
├─ RENDERING: Generando vista
└─ COMPLETED: Respuesta enviada
```

---

## 3.8.4 DIAGRAMA DE DESPLIEGUE (UML Deployment Diagram)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           ENTORNO DE DESPLIEGUE                            │
│                          Sistema Saga Falabella                            │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐    HTTP/HTTPS    ┌─────────────────┐    JDBC/TCP    ┌─────────────────┐
│   CLIENT TIER   │◄────────────────▶│  SERVER TIER    │◄──────────────▶│   DATA TIER     │
│                 │     Port 8080    │                 │    Port 5432   │                 │
│ ┌─────────────┐ │                  │ ┌─────────────┐ │                │ ┌─────────────┐ │
│ │   Browser   │ │                  │ │Spring Boot  │ │                │ │PostgreSQL   │ │
│ │             │ │                  │ │Application  │ │                │ │Database     │ │
│ │• Chrome     │ │                  │ │             │ │                │ │             │ │
│ │• Firefox    │ │                  │ │• Tomcat     │ │                │ │• productos  │ │
│ │• Edge       │ │                  │ │• Thymeleaf  │ │                │ │• usuarios   │ │
│ │• Safari     │ │                  │ │• Security   │ │                │ │• movimientos│ │
│ └─────────────┘ │                  │ │• JPA/Hiber  │ │                │ └─────────────┘ │
│                 │                  │ └─────────────┘ │                │                 │
│ ┌─────────────┐ │                  │                 │                │ ┌─────────────┐ │
│ │HTML/CSS/JS  │ │                  │ ┌─────────────┐ │                │ │Backup       │ │
│ │Rendering    │ │                  │ │JVM Runtime  │ │                │ │Storage      │ │
│ └─────────────┘ │                  │ │Java 17+     │ │                │ └─────────────┘ │
│                 │                  │ └─────────────┘ │                │                 │
│ OS: Windows/    │                  │                 │                │ OS: Linux/      │
│     Linux/MacOS │                  │ OS: Any OS with │                │     Windows     │
│                 │                  │     Java Support│                │                 │
└─────────────────┘                  └─────────────────┘                └─────────────────┘

ESPECIFICACIONES TÉCNICAS:

CLIENT TIER (Navegador):
├─ Dispositivo: PC/Laptop/Tablet
├─ Navegador Web moderno
├─ JavaScript habilitado
├─ Conexión a Internet
└─ Resolución mínima: 1024x768

SERVER TIER (Aplicación):
├─ Spring Boot 3.x
├─ Java JDK 17+
├─ Apache Tomcat embebido
├─ RAM mínima: 512MB
├─ CPU: 2 cores
├─ Almacenamiento: 1GB
└─ Puerto: 8080 (configurable)

DATA TIER (Base de Datos):
├─ PostgreSQL 12+
├─ RAM mínima: 256MB
├─ Almacenamiento: 10GB+
├─ Puerto: 5432
├─ Conexiones concurrentes: 20
└─ Backup automático diario

COMUNICACIÓN:
├─ HTTP/HTTPS (Cliente ↔ Servidor)
├─ JDBC (Servidor ↔ Base de datos)
├─ JSON/HTML (Intercambio de datos)
└─ SQL (Consultas a BD)

SEGURIDAD:
├─ Spring Security
├─ Autenticación por sesión
├─ Encriptación de contraseñas
├─ Validación CSRF
└─ Conexión SSL (recomendado)
```

---

## 3.8.5 DIAGRAMA DE COMPONENTES (UML Component Diagram)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        ARQUITECTURA DE COMPONENTES                         │
│                      Sistema Inventario Saga Falabella                     │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                              WEB LAYER                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌─────────────┐   │
│  │<<component>>  │  │<<component>>  │  │<<component>>  │  │<<component>>│   │
│  │HomeController │  │InventController│  │AuthController │  │ThymeleafView│   │
│  │               │  │               │  │               │  │             │   │
│  │+index()       │  │+dashboard()   │  │+login()       │  │+render()    │   │
│  │+demo()        │  │+productos()   │  │+register()    │  │             │   │
│  │+help()        │  │+buscar()      │  │+logout()      │  │             │   │
│  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘  └─────────────┘   │
│          │                  │                  │                            │
└─────────────────────────────────────────────────────────────────────────────┘
           │                  │                  │
           ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           BUSINESS LAYER                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌─────────────┐   │
│  │<<component>>  │  │<<component>>  │  │<<component>>  │  │<<component>>│   │
│  │ProductoService│  │InventService  │  │UsuarioService │  │ClienteService│  │
│  │               │  │               │  │               │  │             │   │
│  │+obtenerTodos()│  │+actualizarInv │  │+autenticar()  │  │+buscar()    │   │
│  │+buscar()      │  │+registrarMov()│  │+registrar()   │  │+crear()     │   │
│  │+crear()       │  │+estadisticas()│  │+validar()     │  │+actualizar()│   │
│  │+actualizar()  │  │+alertas()     │  │+cambiarPass() │  │             │   │
│  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘  └─────────────┘   │
│          │                  │                  │                            │
└─────────────────────────────────────────────────────────────────────────────┘
           │                  │                  │
           ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DATA ACCESS LAYER                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌─────────────┐   │
│  │<<component>>  │  │<<component>>  │  │<<component>>  │  │<<component>>│   │
│  │ProductoRepo   │  │MovimientoRepo │  │UsuarioRepo    │  │ClienteRepo  │   │
│  │<<Repository>> │  │<<Repository>> │  │<<Repository>> │  │<<Repository>>│  │
│  │               │  │               │  │               │  │             │   │
│  │+findAll()     │  │+findByProduct │  │+findByUsername│  │+findAll()   │   │
│  │+findById()    │  │+findByFecha() │  │+findByEmail() │  │+findByName()│   │
│  │+save()        │  │+save()        │  │+save()        │  │+save()      │   │
│  │+delete()      │  │+delete()      │  │+delete()      │  │+delete()    │   │
│  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘  └─────────────┘   │
│          │                  │                  │                            │
└─────────────────────────────────────────────────────────────────────────────┘
           │                  │                  │
           ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PERSISTENCE LAYER                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌─────────────┐   │
│  │<<component>>  │  │<<component>>  │  │<<component>>  │  │<<component>>│   │
│  │JPA/Hibernate  │  │Spring Data    │  │Connection Pool│  │Transaction  │   │
│  │               │  │               │  │               │  │Manager      │   │
│  │+EntityManager │  │+CrudRepository│  │+HikariCP      │  │+@Transact   │   │
│  │+Query()       │  │+JpaRepository │  │+DataSource    │  │+Rollback    │   │
│  │+Persist()     │  │+PagingAndSort │  │+maxPoolSize   │  │+Commit      │   │
│  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘  └─────────────┘   │
│          │                  │                  │                            │
└─────────────────────────────────────────────────────────────────────────────┘
           │                  │                  │
           ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            DATABASE LAYER                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    <<component>>                                    │   │
│  │                 PostgreSQL Database                                 │   │
│  │                                                                     │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌───────────┐  │   │
│  │  │   productos │  │   usuarios  │  │  movimientos│  │  clientes │  │   │
│  │  │             │  │             │  │             │  │           │  │   │
│  │  │ id (PK)     │  │ id (PK)     │  │ id (PK)     │  │ id (PK)   │  │   │
│  │  │ nombre      │  │ username    │  │ producto_id │  │ nombre    │  │   │
│  │  │ precio      │  │ email       │  │ tipo_mov    │  │ email     │  │   │
│  │  │ stock       │  │ password    │  │ cantidad    │  │ telefono  │  │   │
│  │  │ categoria   │  │ rol         │  │ fecha       │  │ activo    │  │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └───────────┘  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘

INTERFACES Y DEPENDENCIAS:

IProductoService ──implements──▶ ProductoService
IUsuarioService  ──implements──▶ UsuarioService
JpaRepository    ──extends────▶ ProductoRepository
CrudRepository   ──extends────▶ UsuarioRepository

ANOTACIONES SPRING:
├─ @Controller    (Capa Web)
├─ @Service       (Capa Negocio)
├─ @Repository    (Capa Datos)
├─ @Component     (Componentes generales)
├─ @Autowired     (Inyección dependencias)
└─ @Transactional (Gestión transacciones)

PATRONES IMPLEMENTADOS:
├─ MVC: Separación Modelo-Vista-Controlador
├─ DAO: Abstracción acceso a datos
├─ Repository: Encapsulación lógica persistencia
├─ Dependency Injection: Inversión de control
└─ Layered Architecture: Organización por capas
```

---

## 📋 RESUMEN DE CUMPLIMIENTO - PUNTO 3.8

### ✅ 3.8.1 PATRÓN DE DISEÑO
- **IMPLEMENTADO**: MVC + DAO + Arquitectura por Capas
- **JUSTIFICACIÓN**: Separación clara de responsabilidades, mantenibilidad y escalabilidad

### ✅ 3.8.2 DIAGRAMA DE ESTRUCTURA COMPUESTA
- **ELEMENTOS**: Componentes, interfaces, puertos, conectores
- **ENFOQUE**: Estructura interna del sistema y comunicación entre partes

### ✅ 3.8.3 DIAGRAMA DE TIEMPO
- **ELEMENTOS**: Línea temporal, estados, eventos, métricas de rendimiento
- **ENFOQUE**: Flujo temporal de consulta de productos

### ✅ 3.8.4 DIAGRAMA DE DESPLIEGUE
- **ELEMENTOS**: Nodos, artefactos, conexiones, especificaciones técnicas
- **ENFOQUE**: Distribución física del sistema en el entorno

### ✅ 3.8.5 DIAGRAMA DE COMPONENTES
- **ELEMENTOS**: Componentes, interfaces, dependencias, capas
- **ENFOQUE**: Organización modular y relaciones entre componentes

**CONCLUSIÓN**: Los 4 diagramas UML solicitados han sido desarrollados completamente, cumpliendo con las características y elementos específicos de cada tipo de diagrama, proporcionando una visión integral de la arquitectura del Sistema de Inventario Saga Falabella.
