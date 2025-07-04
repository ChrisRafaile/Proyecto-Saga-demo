package com.sagafalabella.inventario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    
    @GetMapping("/test-favicon-simple")
    public String testFaviconSimple() {
        return "test-favicon-simple";
    }
    
    @GetMapping("/test-styles")
    public String testStyles() {
        return "test-styles";
    }
    
    @GetMapping("/test-navbar")
    public String testNavbar() {
        return "test-navbar";
    }
    
    @GetMapping("/test-admin-nav")
    public String testAdminNav() {
        return "test-admin-nav";
    }
    
    @GetMapping("/debug-dropdowns")
    public String debugDropdowns() {
        return "debug-dropdowns";
    }
    
    @GetMapping("/test-dropdowns-simple")
    public String testDropdownsSimple() {
        return "test-dropdowns-simple";
    }
    
    @GetMapping("/test-navbar-limpio")
    public String testNavbarLimpio() {
        return "test-navbar-limpio";
    }
}
