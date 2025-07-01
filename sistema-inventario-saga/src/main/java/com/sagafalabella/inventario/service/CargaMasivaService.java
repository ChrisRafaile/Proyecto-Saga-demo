package com.sagafalabella.inventario.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sagafalabella.inventario.model.Producto;

/**
 * Servicio para carga masiva de productos
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Service
@Transactional
public class CargaMasivaService {

    @Autowired
    private ProductoService productoService;

    private final Random random = new Random();

    /**
     * Genera productos de ejemplo para Saga Falabella
     */
    public List<Producto> generarProductosEjemplo() {
        List<Producto> productos = new ArrayList<>();
        
        // Productos de tecnología
        productos.addAll(generarProductosTecnologia());
        
        // Productos de ropa
        productos.addAll(generarProductosRopa());
        
        // Productos de hogar
        productos.addAll(generarProductosHogar());
        
        // Productos de deportes
        productos.addAll(generarProductosDeportes());
        
        // Productos de belleza
        productos.addAll(generarProductosBelleza());
        
        return productos;
    }

    /**
     * Carga masiva de productos generados automáticamente
     */
    public int cargarProductosAutomaticos() {
        System.out.println("=== INICIO CARGA MASIVA ===");
        
        List<Producto> productos = generarProductosEjemplo();
        System.out.println("Productos generados: " + productos.size());
        
        // Filtrar productos que no existan
        List<Producto> productosNuevos = new ArrayList<>();
        for (Producto producto : productos) {
            boolean existe = productoService.existePorCodigo(producto.getCodigoProducto());
            System.out.println("Producto " + producto.getCodigoProducto() + " existe: " + existe);
            if (!existe) {
                productosNuevos.add(producto);
                System.out.println("Agregando producto nuevo: " + producto.getCodigoProducto() + " - " + producto.getNombre());
            }
        }
        
        System.out.println("Productos nuevos a crear: " + productosNuevos.size());
        
        if (!productosNuevos.isEmpty()) {
            List<Producto> productosGuardados = productoService.guardarTodos(productosNuevos);
            System.out.println("Productos guardados exitosamente: " + productosGuardados.size());
        }
        
        System.out.println("=== FIN CARGA MASIVA ===");
        return productosNuevos.size();
    }

    private List<Producto> generarProductosTecnologia() {
        List<Producto> productos = new ArrayList<>();
        
        // Smartphones
        productos.add(crearProducto("SG-001", "Samsung Galaxy S24", 
            "Smartphone Samsung Galaxy S24 256GB", "Tecnología", 
            new BigDecimal("1299.99"), "Samsung", 15, 3));
            
        productos.add(crearProducto("IP-001", "iPhone 15 Pro", 
            "Apple iPhone 15 Pro 256GB", "Tecnología", 
            new BigDecimal("1899.99"), "Apple", 8, 2));
            
        productos.add(crearProducto("HW-001", "Huawei P60 Pro", 
            "Huawei P60 Pro 512GB", "Tecnología", 
            new BigDecimal("1199.99"), "Huawei", 12, 3));

        // Laptops
        productos.add(crearProducto("HP-LAP-001", "HP Pavilion 15", 
            "Laptop HP Pavilion 15 Intel i7 16GB RAM 512GB SSD", "Tecnología", 
            new BigDecimal("1599.99"), "HP", 6, 2));
            
        productos.add(crearProducto("LN-LAP-001", "Lenovo ThinkPad E14", 
            "Laptop Lenovo ThinkPad E14 AMD Ryzen 7 16GB RAM 1TB SSD", "Tecnología", 
            new BigDecimal("1399.99"), "Lenovo", 4, 1));

        // Accesorios
        productos.add(crearProducto("AC-001", "AirPods Pro 2", 
            "Apple AirPods Pro (2da generación)", "Tecnología", 
            new BigDecimal("399.99"), "Apple", 20, 5));

        return productos;
    }

    private List<Producto> generarProductosRopa() {
        List<Producto> productos = new ArrayList<>();
        
        // Ropa masculina
        productos.add(crearProducto("RM-001", "Camisa Formal Blanca", 
            "Camisa formal blanca talla M", "Ropa Hombre", 
            new BigDecimal("79.99"), "Saga", 25, 5));
            
        productos.add(crearProducto("RM-002", "Pantalón Chino Negro", 
            "Pantalón chino negro talla 32", "Ropa Hombre", 
            new BigDecimal("89.99"), "Saga", 30, 8));
            
        productos.add(crearProducto("RM-003", "Polo Casual Azul", 
            "Polo casual azul marino talla L", "Ropa Hombre", 
            new BigDecimal("49.99"), "Saga", 40, 10));

        // Ropa femenina
        productos.add(crearProducto("RF-001", "Blusa Elegante Rosa", 
            "Blusa elegante rosa talla S", "Ropa Mujer", 
            new BigDecimal("69.99"), "Saga", 35, 8));
            
        productos.add(crearProducto("RF-002", "Vestido Casual Flores", 
            "Vestido casual con estampado floral talla M", "Ropa Mujer", 
            new BigDecimal("119.99"), "Saga", 20, 5));
            
        productos.add(crearProducto("RF-003", "Jeans Skinny Azul", 
            "Jeans skinny azul talla 28", "Ropa Mujer", 
            new BigDecimal("99.99"), "Saga", 28, 7));

        return productos;
    }

    private List<Producto> generarProductosHogar() {
        List<Producto> productos = new ArrayList<>();
        
        productos.add(crearProducto("HG-001", "Juego de Sábanas King", 
            "Juego de sábanas algodón King size", "Hogar", 
            new BigDecimal("159.99"), "Saga Home", 15, 3));
            
        productos.add(crearProducto("HG-002", "Almohada Memory Foam", 
            "Almohada viscoelástica memory foam", "Hogar", 
            new BigDecimal("79.99"), "Saga Home", 25, 5));
            
        productos.add(crearProducto("HG-003", "Vajilla 24 Piezas", 
            "Vajilla de porcelana 24 piezas", "Hogar", 
            new BigDecimal("199.99"), "Saga Home", 12, 2));
            
        productos.add(crearProducto("HG-004", "Licuadora Oster", 
            "Licuadora Oster 3 velocidades 1.5L", "Electrodomésticos", 
            new BigDecimal("149.99"), "Oster", 18, 4));

        return productos;
    }

    private List<Producto> generarProductosDeportes() {
        List<Producto> productos = new ArrayList<>();
        
        productos.add(crearProducto("DP-001", "Zapatillas Nike Air Max", 
            "Zapatillas Nike Air Max para running talla 42", "Deportes", 
            new BigDecimal("179.99"), "Nike", 22, 5));
            
        productos.add(crearProducto("DP-002", "Pelota de Fútbol Adidas", 
            "Pelota de fútbol Adidas oficial", "Deportes", 
            new BigDecimal("49.99"), "Adidas", 35, 8));
            
        productos.add(crearProducto("DP-003", "Raqueta de Tenis Wilson", 
            "Raqueta de tenis Wilson Pro Staff", "Deportes", 
            new BigDecimal("299.99"), "Wilson", 8, 2));

        return productos;
    }

    private List<Producto> generarProductosBelleza() {
        List<Producto> productos = new ArrayList<>();
        
        productos.add(crearProducto("BL-001", "Crema Facial L'Oréal", 
            "Crema facial anti-edad L'Oréal 50ml", "Belleza", 
            new BigDecimal("89.99"), "L'Oréal", 30, 6));
            
        productos.add(crearProducto("BL-002", "Perfume Hugo Boss", 
            "Perfume Hugo Boss para hombre 100ml", "Belleza", 
            new BigDecimal("149.99"), "Hugo Boss", 15, 3));
            
        productos.add(crearProducto("BL-003", "Shampoo Pantene", 
            "Shampoo Pantene reparación total 400ml", "Belleza", 
            new BigDecimal("19.99"), "Pantene", 50, 12));

        return productos;
    }

    private Producto crearProducto(String codigo, String nombre, String descripcion, 
                                 String categoria, BigDecimal precio, String marca, 
                                 int stockActual, int stockMinimo) {
        return Producto.builder()
            .codigoProducto(codigo)
            .nombre(nombre)
            .descripcion(descripcion)
            .categoria(categoria)
            .precio(precio)
            .marca(marca)
            .stockActual(stockActual)
            .stockMinimo(stockMinimo)
            .activo(true)
            .ubicacionAlmacen("A" + (random.nextInt(10) + 1) + "-" + (random.nextInt(20) + 1))
            .build();
    }

    /**
     * Obtiene las categorías disponibles
     */
    public List<String> obtenerCategorias() {
        return Arrays.asList(
            "Tecnología", "Ropa Hombre", "Ropa Mujer", "Hogar", 
            "Deportes", "Belleza", "Electrodomésticos"
        );
    }
    
    /**
     * Carga productos desde un archivo CSV
     */
    public int cargarProductosDesdeCSV(org.springframework.web.multipart.MultipartFile archivo) throws Exception {
        List<Producto> productos = new ArrayList<>();
        
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(archivo.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            
            String linea;
            boolean primeraLinea = true;
            int numeroLinea = 0;
            
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                
                // Saltar la línea de cabecera
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }
                
                // Saltar líneas vacías
                if (linea.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    Producto producto = parsearLineaCSV(linea, numeroLinea);
                    if (producto != null) {
                        productos.add(producto);
                    }
                } catch (Exception e) {
                    throw new Exception("Error en línea " + numeroLinea + ": " + e.getMessage());
                }
            }
        }
        
        // Guardar productos en lotes
        int productosCreados = 0;
        for (Producto producto : productos) {
            try {
                // Verificar si ya existe un producto con el mismo código
                if (!productoService.existeCodigoProducto(producto.getCodigoProducto())) {
                    productoService.guardar(producto);
                    productosCreados++;
                } else {
                    System.out.println("Producto ya existe, saltando: " + producto.getCodigoProducto());
                }
            } catch (Exception e) {
                System.err.println("Error al guardar producto " + producto.getCodigoProducto() + ": " + e.getMessage());
            }
        }
        
        return productosCreados;
    }
      /**
     * Parsea una línea del CSV y crea un objeto Producto
     */
    private Producto parsearLineaCSV(String linea, int numeroLinea) throws Exception {
        // Dividir la línea por comas, manejando comillas
        String[] campos = parsearCSV(linea);
        
        if (campos.length < 9) {
            throw new Exception("Línea " + numeroLinea + ": debe tener al menos 9 campos: codigo,nombre,categoria,marca,precio,stock_actual,stock_minimo,descripcion,ubicacion");
        }
        
        try {
            String codigo = campos[0].trim();
            String nombre = campos[1].trim();
            String categoria = campos[2].trim();
            String marca = campos[3].trim();
            BigDecimal precio = new BigDecimal(campos[4].trim());
            int stockActual = Integer.parseInt(campos[5].trim());
            int stockMinimo = Integer.parseInt(campos[6].trim());
            String descripcion = campos[7].trim();
            String ubicacion = campos[8].trim();
              // Validaciones básicas
            if (codigo.isEmpty() || nombre.isEmpty()) {
                throw new Exception("Línea " + numeroLinea + ": Código y nombre son obligatorios");
            }
            
            if (precio.compareTo(BigDecimal.ZERO) < 0) {
                throw new Exception("Línea " + numeroLinea + ": El precio no puede ser negativo");
            }
            
            if (stockActual < 0 || stockMinimo < 0) {
                throw new Exception("Línea " + numeroLinea + ": El stock no puede ser negativo");
            }
            
            return Producto.builder()
                .codigoProducto(codigo)
                .nombre(nombre)
                .categoria(categoria)
                .marca(marca)
                .precio(precio)
                .stockActual(stockActual)
                .stockMinimo(stockMinimo)
                .descripcion(descripcion)
                .ubicacionAlmacen(ubicacion)
                .activo(true)
                .build();
                
        } catch (NumberFormatException e) {
            throw new Exception("Error en formato numérico: " + e.getMessage());
        }
    }
    
    /**
     * Parsea una línea CSV manejando comillas y comas dentro de campos
     */
    private String[] parsearCSV(String linea) {
        List<String> campos = new ArrayList<>();
        boolean dentroComillas = false;
        StringBuilder campoActual = new StringBuilder();
        
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            
            if (c == '"') {
                dentroComillas = !dentroComillas;
            } else if (c == ',' && !dentroComillas) {
                campos.add(campoActual.toString());
                campoActual = new StringBuilder();
            } else {
                campoActual.append(c);
            }
        }
          // Agregar el último campo
        campos.add(campoActual.toString());
        
        return campos.toArray(String[]::new);
    }
}
