import PyPDF2
import os

def extract_text_from_pdf(pdf_path, output_path):
    """
    Extrae el texto de un archivo PDF y lo guarda en un archivo de texto.
    """
    try:
        with open(pdf_path, 'rb') as file:
            pdf_reader = PyPDF2.PdfReader(file)
            text = ""
            
            print(f"Extrayendo texto de {len(pdf_reader.pages)} páginas...")
            
            for page_num, page in enumerate(pdf_reader.pages, 1):
                try:
                    page_text = page.extract_text()
                    text += f"\n--- PÁGINA {page_num} ---\n"
                    text += page_text + "\n"
                    print(f"Página {page_num} procesada")
                except Exception as e:
                    print(f"Error en página {page_num}: {e}")
                    text += f"\n--- PÁGINA {page_num} (ERROR) ---\n"
                    text += f"Error al extraer texto: {e}\n"
            
            # Guardar el texto extraído
            with open(output_path, 'w', encoding='utf-8') as output_file:
                output_file.write(text)
            
            print(f"Texto extraído y guardado en: {output_path}")
            return True
            
    except Exception as e:
        print(f"Error al procesar el PDF: {e}")
        return False

if __name__ == "__main__":
    # Rutas de archivos
    pdf_file = "AvanceProy2_RafaileChristopher.pdf"
    output_file = "contenido_pdf_extraido.txt"
    
    # Verificar que el PDF existe
    if os.path.exists(pdf_file):
        success = extract_text_from_pdf(pdf_file, output_file)
        if success:
            print("¡Extracción completada exitosamente!")
        else:
            print("Hubo errores durante la extracción.")
    else:
        print(f"No se encontró el archivo: {pdf_file}")
        print("Archivos en el directorio actual:")
        for file in os.listdir('.'):
            print(f"  - {file}")
