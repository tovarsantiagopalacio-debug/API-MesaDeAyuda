package com.helpdesk.api.controller;

import com.helpdesk.api.dto.CambioEstadoRequest;
import com.helpdesk.api.dto.TicketRequest;
import com.helpdesk.api.dto.TicketResponse;
import com.helpdesk.api.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> crear(@Valid @RequestBody TicketRequest request,
                                                 Authentication authentication) {
        TicketResponse response = ticketService.crear(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mios")
    public ResponseEntity<List<TicketResponse>> listarMios(Authentication authentication) {
        List<TicketResponse> tickets = ticketService.listarMios(authentication.getName());
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/vencidos")
    @PreAuthorize("hasAnyRole('SOPORTE', 'ADMIN')")
    public ResponseEntity<List<TicketResponse>> listarVencidos() {
        List<TicketResponse> tickets = ticketService.listarVencidos();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> obtener(@PathVariable Long id,
                                                   Authentication authentication) {
        String rol = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USUARIO")
                .replace("ROLE_", "");

        TicketResponse response = ticketService.obtener(id, authentication.getName(), rol);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SOPORTE', 'ADMIN')")
    public ResponseEntity<List<TicketResponse>> listarTodos() {
        List<TicketResponse> tickets = ticketService.listarTodos();
        return ResponseEntity.ok(tickets);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('SOPORTE', 'ADMIN')")
    public ResponseEntity<TicketResponse> cambiarEstado(@PathVariable Long id,
                                                         @Valid @RequestBody CambioEstadoRequest request) {
        TicketResponse response = ticketService.cambiarEstado(id, request);
        return ResponseEntity.ok(response);
    }
}
