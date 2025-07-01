package com.sagafalabella.inventario.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.sagafalabella.inventario.model.Actividad;
import com.sagafalabella.inventario.model.Pedido;
import com.sagafalabella.inventario.model.Producto;

/**
 * Servicio para exportar reportes en diferentes formatos
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private PedidoService pedidoService;
    
    @Autowired
    private ActividadService actividadService;

    /**
     * Exportar reporte de productos a PDF
     */    public byte[] exportarProductosPDF() throws IOException, DocumentException {
        logger.info("Iniciando exportación de productos a PDF");
        
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        logger.debug("Exportando {} productos a PDF", productos.size());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        // Fuentes
        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

        // Título del documento
        Paragraph title = new Paragraph("REPORTE DE PRODUCTOS - SAGA FALABELLA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Fecha de generación
        Paragraph fecha = new Paragraph("Fecha de generación: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont);
        fecha.setAlignment(Element.ALIGN_CENTER);
        document.add(fecha);
        document.add(new Paragraph(" "));

        // Estadísticas generales
        Paragraph statsTitle = new Paragraph("ESTADÍSTICAS GENERALES", headerFont);
        document.add(statsTitle);        document.add(new Paragraph("Total de productos: " + productos.size(), normalFont));        document.add(new Paragraph("Productos activos: " + productos.stream()
                .mapToInt(p -> Boolean.TRUE.equals(p.getActivo()) ? 1 : 0).sum(), normalFont));
        
        long stockTotal = productos.stream()
                .mapToLong(p -> p.getStockActual() != null ? p.getStockActual().longValue() : 0L)
                .sum();
        document.add(new Paragraph("Stock total: " + stockTotal + " unidades", normalFont));
        document.add(new Paragraph(" "));

        // Tabla de productos
        Paragraph tableTitle = new Paragraph("LISTADO DE PRODUCTOS", headerFont);
        document.add(tableTitle);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 2, 2, 1, 1});

        // Headers
        table.addCell(new PdfPCell(new Phrase("ID", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Nombre", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Categoría", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Precio", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Stock", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Estado", headerFont)));

        // Datos
        for (Producto producto : productos) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(producto.getIdproducto()), normalFont)));
            table.addCell(new PdfPCell(new Phrase(producto.getNombre(), normalFont)));
            table.addCell(new PdfPCell(new Phrase(producto.getCategoria() != null ? 
                    producto.getCategoria() : "Sin categoría", normalFont)));
            table.addCell(new PdfPCell(new Phrase("S/ " + 
                    (producto.getPrecio() != null ? producto.getPrecio().toString() : "0.00"), normalFont)));            table.addCell(new PdfPCell(new Phrase(
                    Objects.toString(producto.getStockActual(), "0"), normalFont)));
            table.addCell(new PdfPCell(new Phrase(Boolean.TRUE.equals(producto.getActivo()) ? "Activo" : "Inactivo", normalFont)));
        }

        document.add(table);

        // Pie de página
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("Reporte generado por Sistema de Inventario Saga Falabella", normalFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);        document.close();
        
        logger.info("Exportación PDF completada exitosamente");
        return baos.toByteArray();
    }

    /**
     * Exportar reporte de productos a Excel
     */    public byte[] exportarProductosExcel() throws IOException {
        logger.info("Iniciando exportación de productos a Excel");
        
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        logger.debug("Exportando {} productos a Excel", productos.size());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Productos");// Estilo para headers
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Código", "Nombre", "Descripción", "Categoría", "Precio", "Stock", "Stock Mínimo", "Estado"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos
        int rowNum = 1;
        for (Producto producto : productos) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(producto.getIdproducto());
            row.createCell(1).setCellValue(producto.getCodigoProducto() != null ? producto.getCodigoProducto() : "");
            row.createCell(2).setCellValue(producto.getNombre());
            row.createCell(3).setCellValue(producto.getDescripcion() != null ? producto.getDescripcion() : "");
            row.createCell(4).setCellValue(producto.getCategoria() != null ? producto.getCategoria() : "Sin categoría");            row.createCell(5).setCellValue(producto.getPrecio() != null ? 
                    producto.getPrecio().doubleValue() : 0.0);            row.createCell(6).setCellValue(producto.getStockActual() != null ? 
                    producto.getStockActual().doubleValue() : 0.0);
            row.createCell(7).setCellValue(producto.getStockMinimo() != null ? 
                    producto.getStockMinimo().doubleValue() : 0.0);
            row.createCell(8).setCellValue(Boolean.TRUE.equals(producto.getActivo()) ? "Activo" : "Inactivo");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Información adicional en una segunda hoja
        Sheet statsSheet = workbook.createSheet("Estadísticas");
        Row statsRow1 = statsSheet.createRow(0);
        statsRow1.createCell(0).setCellValue("Estadísticas del Inventario");
        
        Row statsRow2 = statsSheet.createRow(2);
        statsRow2.createCell(0).setCellValue("Total de productos:");
        statsRow2.createCell(1).setCellValue(productos.size());
        
        Row statsRow3 = statsSheet.createRow(3);
        statsRow3.createCell(0).setCellValue("Productos activos:");
        statsRow3.createCell(1).setCellValue(productos.stream().mapToInt(p -> Boolean.TRUE.equals(p.getActivo()) ? 1 : 0).sum());        Row statsRow4 = statsSheet.createRow(4);
        statsRow4.createCell(0).setCellValue("Stock total:");        long stockTotal = productos.stream()
                .mapToLong(p -> p.getStockActual() != null ? p.getStockActual().longValue() : 0L)
                .sum();
        statsRow4.createCell(1).setCellValue(stockTotal);
        
        Row statsRow5 = statsSheet.createRow(5);
        statsRow5.createCell(0).setCellValue("Fecha de generación:");
        statsRow5.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));        statsSheet.autoSizeColumn(0);
        statsSheet.autoSizeColumn(1);        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        
        logger.info("Exportación Excel completada exitosamente");
        return baos.toByteArray();
        }
    }

    // ==================== EXPORTACIÓN DE PEDIDOS ====================
    
    /**
     * Exportar pedidos a PDF
     */
    public byte[] exportarPedidosPDF() throws IOException, DocumentException {
        logger.info("Iniciando exportación de pedidos a PDF");
        
        List<Pedido> pedidos = pedidoService.obtenerTodosPaginado(0, 1000, "fechaPedido", "desc").getContent();
        
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Título
        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("Reporte de Pedidos", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Información del reporte
        com.itextpdf.text.Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        Paragraph info = new Paragraph("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), infoFont);
        info.setAlignment(Element.ALIGN_RIGHT);
        info.setSpacingAfter(20);
        document.add(info);
        
        // Tabla de pedidos
        PdfPTable table = new PdfPTable(6); // 6 columnas
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        
        // Encabezados
        com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        
        PdfPCell header1 = new PdfPCell(new Phrase("Número", headerFont));
        header1.setBackgroundColor(BaseColor.DARK_GRAY);
        header1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header1);
        
        PdfPCell header2 = new PdfPCell(new Phrase("Fecha", headerFont));
        header2.setBackgroundColor(BaseColor.DARK_GRAY);
        header2.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header2);
        
        PdfPCell header3 = new PdfPCell(new Phrase("Estado", headerFont));
        header3.setBackgroundColor(BaseColor.DARK_GRAY);
        header3.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header3);
        
        PdfPCell header4 = new PdfPCell(new Phrase("Tipo Entrega", headerFont));
        header4.setBackgroundColor(BaseColor.DARK_GRAY);
        header4.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header4);
        
        PdfPCell header5 = new PdfPCell(new Phrase("Total", headerFont));
        header5.setBackgroundColor(BaseColor.DARK_GRAY);
        header5.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header5);
        
        PdfPCell header6 = new PdfPCell(new Phrase("Observaciones", headerFont));
        header6.setBackgroundColor(BaseColor.DARK_GRAY);
        header6.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header6);
        
        // Datos
        com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
        
        for (Pedido pedido : pedidos) {
            table.addCell(new Phrase(Objects.toString(pedido.getNumeroPedido(), ""), cellFont));
            table.addCell(new Phrase(pedido.getFechaPedido() != null ? 
                pedido.getFechaPedido().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "", cellFont));
            table.addCell(new Phrase(Objects.toString(pedido.getEstado(), ""), cellFont));
            table.addCell(new Phrase(Objects.toString(pedido.getTipoEntrega(), ""), cellFont));
            table.addCell(new Phrase(pedido.getTotal() != null ? pedido.getTotal().toString() : "0.00", cellFont));
            table.addCell(new Phrase(Objects.toString(pedido.getObservaciones(), ""), cellFont));
        }
        
        document.add(table);
        
        // Estadísticas
        Paragraph stats = new Paragraph("\nEstadísticas:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
        document.add(stats);
        
        Paragraph totalPedidos = new Paragraph("Total de pedidos: " + pedidos.size());
        document.add(totalPedidos);
        
        document.close();
        
        logger.info("Exportación de pedidos a PDF completada exitosamente");
        return baos.toByteArray();
    }
    
    /**
     * Exportar pedidos a Excel
     */
    public byte[] exportarPedidosExcel() throws IOException {
        logger.info("Iniciando exportación de pedidos a Excel");
        
        List<Pedido> pedidos = pedidoService.obtenerTodosPaginado(0, 1000, "fechaPedido", "desc").getContent();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Pedidos");
            
            // Estilo para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Número", "Fecha Pedido", "Estado", "Tipo Entrega", "Total", "Descuento", "Observaciones"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos
            int rowNum = 1;
            for (Pedido pedido : pedidos) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(Objects.toString(pedido.getNumeroPedido(), ""));
                row.createCell(1).setCellValue(pedido.getFechaPedido() != null ? 
                    pedido.getFechaPedido().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
                row.createCell(2).setCellValue(Objects.toString(pedido.getEstado(), ""));
                row.createCell(3).setCellValue(Objects.toString(pedido.getTipoEntrega(), ""));
                row.createCell(4).setCellValue(pedido.getTotal() != null ? pedido.getTotal().doubleValue() : 0.0);
                row.createCell(5).setCellValue(pedido.getDescuento() != null ? pedido.getDescuento().doubleValue() : 0.0);
                row.createCell(6).setCellValue(Objects.toString(pedido.getObservaciones(), ""));
            }
            
            // Auto-ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Hoja de estadísticas
            Sheet statsSheet = workbook.createSheet("Estadísticas");
            Row statsRow1 = statsSheet.createRow(0);
            statsRow1.createCell(0).setCellValue("Estadísticas de Pedidos");
            
            Row statsRow2 = statsSheet.createRow(2);
            statsRow2.createCell(0).setCellValue("Total de pedidos:");
            statsRow2.createCell(1).setCellValue(pedidos.size());
            
            Row statsRow3 = statsSheet.createRow(3);
            statsRow3.createCell(0).setCellValue("Fecha de generación:");
            statsRow3.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            statsSheet.autoSizeColumn(0);
            statsSheet.autoSizeColumn(1);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            
            logger.info("Exportación de pedidos a Excel completada exitosamente");
            return baos.toByteArray();
        }
    }
    
    // ==================== EXPORTACIÓN DE ACTIVIDADES ====================
    
    /**
     * Exportar actividades a PDF
     */
    public byte[] exportarActividadesPDF() throws IOException, DocumentException {
        logger.info("Iniciando exportación de actividades a PDF");
        
        List<Actividad> actividades = actividadService.obtenerTodosPaginado(0, 1000, "fechaActividad", "desc").getContent();
        
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Título
        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("Log de Actividades del Sistema", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Información del reporte
        com.itextpdf.text.Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        Paragraph info = new Paragraph("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), infoFont);
        info.setAlignment(Element.ALIGN_RIGHT);
        info.setSpacingAfter(20);
        document.add(info);
        
        // Tabla de actividades
        PdfPTable table = new PdfPTable(6); // 6 columnas
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        
        // Encabezados
        com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        
        String[] headers = {"Fecha", "Usuario", "Acción", "Tipo", "Nivel", "Descripción"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        
        // Datos
        com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
        
        for (Actividad actividad : actividades) {
            table.addCell(new Phrase(actividad.getFechaActividad() != null ? 
                actividad.getFechaActividad().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "", cellFont));
            table.addCell(new Phrase(actividad.getNombreUsuario() != null ? actividad.getNombreUsuario() : "Sistema", cellFont));
            table.addCell(new Phrase(Objects.toString(actividad.getAccion(), ""), cellFont));
            table.addCell(new Phrase(Objects.toString(actividad.getTipoActividad(), ""), cellFont));
            table.addCell(new Phrase(Objects.toString(actividad.getNivel(), ""), cellFont));
            table.addCell(new Phrase(Objects.toString(actividad.getDescripcion(), ""), cellFont));
        }
        
        document.add(table);
        
        // Estadísticas
        Paragraph stats = new Paragraph("\nEstadísticas:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
        document.add(stats);
        
        Paragraph totalActividades = new Paragraph("Total de actividades: " + actividades.size());
        document.add(totalActividades);
        
        document.close();
        
        logger.info("Exportación de actividades a PDF completada exitosamente");
        return baos.toByteArray();
    }
    
    /**
     * Exportar actividades a Excel
     */
    public byte[] exportarActividadesExcel() throws IOException {
        logger.info("Iniciando exportación de actividades a Excel");
        
        List<Actividad> actividades = actividadService.obtenerTodosPaginado(0, 1000, "fechaActividad", "desc").getContent();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Actividades");
            
            // Estilo para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Fecha", "Usuario", "Acción", "Tipo", "Nivel", "Entidad", "Descripción", "IP", "Resultado"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos
            int rowNum = 1;
            for (Actividad actividad : actividades) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(actividad.getFechaActividad() != null ? 
                    actividad.getFechaActividad().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "");
                row.createCell(1).setCellValue(actividad.getNombreUsuario() != null ? actividad.getNombreUsuario() : "Sistema");
                row.createCell(2).setCellValue(Objects.toString(actividad.getAccion(), ""));
                row.createCell(3).setCellValue(Objects.toString(actividad.getTipoActividad(), ""));
                row.createCell(4).setCellValue(Objects.toString(actividad.getNivel(), ""));
                row.createCell(5).setCellValue(Objects.toString(actividad.getEntidad(), ""));
                row.createCell(6).setCellValue(Objects.toString(actividad.getDescripcion(), ""));
                row.createCell(7).setCellValue(Objects.toString(actividad.getDireccionIp(), ""));
                row.createCell(8).setCellValue(Objects.toString(actividad.getResultado(), ""));
            }
            
            // Auto-ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Hoja de estadísticas
            Sheet statsSheet = workbook.createSheet("Estadísticas");
            Row statsRow1 = statsSheet.createRow(0);
            statsRow1.createCell(0).setCellValue("Estadísticas de Actividades");
            
            Row statsRow2 = statsSheet.createRow(2);
            statsRow2.createCell(0).setCellValue("Total de actividades:");
            statsRow2.createCell(1).setCellValue(actividades.size());
            
            Row statsRow3 = statsSheet.createRow(3);
            statsRow3.createCell(0).setCellValue("Fecha de generación:");
            statsRow3.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            statsSheet.autoSizeColumn(0);
            statsSheet.autoSizeColumn(1);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            
            logger.info("Exportación de actividades a Excel completada exitosamente");
            return baos.toByteArray();
        }
    }
}
