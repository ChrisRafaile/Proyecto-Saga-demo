package com.sagafalabella.inventario.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DiagnosticController {

    @GetMapping("/diagnostic")
    public String diagnostic(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        model.addAttribute("isAuthenticated", auth != null && auth.isAuthenticated());
        model.addAttribute("username", auth != null ? auth.getName() : "null");
        model.addAttribute("authorities", auth != null ? auth.getAuthorities() : "null");
        model.addAttribute("principal", auth != null ? auth.getPrincipal() : "null");
        
        return "diagnostic";
    }
}
