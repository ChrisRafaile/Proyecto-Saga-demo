package com.sagafalabella.inventario.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sagafalabella.inventario.model.Cliente;
import com.sagafalabella.inventario.model.Usuario;
import com.sagafalabella.inventario.service.ClienteService;
import com.sagafalabella.inventario.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * Controlador para registro de nuevos usuarios (clientes y empleados)
 * 
 * @author Christopher Lincoln Rafaile Naupay
 */
@Controller
@RequestMapping("/register")
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClienteService clienteService;

    /**
     * Formulario de registro para clientes externos
     */
    @GetMapping("/cliente")
    public String registroCliente(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("usuario", new Usuario());
        return "auth/register-cliente";
    }

    /**
     * Procesar registro de cliente
     */
    @PostMapping("/cliente")
    public String procesarRegistroCliente(
            @Valid @ModelAttribute("cliente") Cliente cliente,
            BindingResult clienteResult,
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult usuarioResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Validar que no haya errores
        if (clienteResult.hasErrors() || usuarioResult.hasErrors()) {
            model.addAttribute("cliente", cliente);
            model.addAttribute("usuario", usuario);
            return "auth/register-cliente";
        }        try {
            // Configurar el usuario como cliente
            usuario.setRol(Usuario.RolUsuario.CLIENTE);
            usuario.setTipoUsuario(Usuario.TipoUsuario.EXTERNO);

            // Registrar cliente con usuario
            clienteService.registrarClienteConUsuario(cliente, usuario);

            redirectAttributes.addFlashAttribute("successMessage", 
                "¡Registro exitoso! Ya puedes iniciar sesión con tu cuenta.");
            
            return "redirect:/auth/login";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error en el registro: " + e.getMessage());
            model.addAttribute("cliente", cliente);
            model.addAttribute("usuario", usuario);
            return "auth/register-cliente";
        }
    }

    /**
     * Formulario de registro para empleados (solo para administradores)
     */
    @GetMapping("/empleado")
    public String registroEmpleado(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("rolesDisponibles", Usuario.RolUsuario.values());
        return "auth/register-empleado";
    }

    /**
     * Procesar registro de empleado (solo para administradores)
     */
    @PostMapping("/empleado")
    public String procesarRegistroEmpleado(
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("rolesDisponibles", Usuario.RolUsuario.values());
            return "auth/register-empleado";
        }

        try {
            // Configurar como usuario interno
            usuario.setTipoUsuario(Usuario.TipoUsuario.INTERNO);

            // Registrar empleado
            Usuario empleadoRegistrado = usuarioService.registrarEmpleado(usuario);

            redirectAttributes.addFlashAttribute("successMessage", 
                "Empleado registrado exitosamente: " + empleadoRegistrado.getNombreCompleto());
            
            return "redirect:/admin/usuarios";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error en el registro: " + e.getMessage());
            model.addAttribute("usuario", usuario);
            model.addAttribute("rolesDisponibles", Usuario.RolUsuario.values());
            return "auth/register-empleado";
        }
    }

    /**
     * Verificar disponibilidad de username via AJAX
     */
    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsername(@RequestParam String username) {
        return usuarioService.isUsernameDisponible(username);
    }

    /**
     * Verificar disponibilidad de email via AJAX
     */
    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return usuarioService.isEmailDisponible(email);
    }
}
