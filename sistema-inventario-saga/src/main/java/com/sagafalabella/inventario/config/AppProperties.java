package com.sagafalabella.inventario.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración de la aplicación
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Upload upload = new Upload();

    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload = upload;
    }

    public static class Upload {
        private String dir = System.getProperty("user.home") + "/saga-uploads";

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }
    }
}
