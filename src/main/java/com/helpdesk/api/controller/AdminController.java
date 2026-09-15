package com.helpdesk.api.controller;

import com.helpdesk.api.entity.Rol;
import com.helpdesk.api.entity.Usuario;
import com.helpdesk.api.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioRepository usuarioRepository;

    @PostMapping("/soporte")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> asignarSoporte(@Valid @RequestBody AsignarSoporteRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + request.getEmail()));

        usuario.setRol(Rol.SOPORTE);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("mensaje", "Usuario " + usuario.getEmail() + " ascendido a SOPORTE"));
    }

    @Data
    static class AsignarSoporteRequest {
        @NotBlank
        @Email
        private String email;
    }
}
