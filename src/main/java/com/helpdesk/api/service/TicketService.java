package com.helpdesk.api.service;

import com.helpdesk.api.dto.CambioEstadoRequest;
import com.helpdesk.api.dto.TicketRequest;
import com.helpdesk.api.dto.TicketResponse;
import com.helpdesk.api.entity.EstadoTicket;
import com.helpdesk.api.entity.Rol;
import com.helpdesk.api.entity.Ticket;
import com.helpdesk.api.entity.Usuario;
import com.helpdesk.api.repository.TicketRepository;
import com.helpdesk.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;

    public TicketResponse crear(TicketRequest request, String emailCreador) {
        Usuario creador = usuarioRepository.findByEmail(emailCreador)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Ticket ticket = Ticket.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .prioridad(request.getPrioridad())
                .creadoPor(creador)
                .build();

        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    public List<TicketResponse> listarMios(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ticketRepository.findByCreadoPorIdOrderByCreadoEnDesc(usuario.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TicketResponse obtener(Long ticketId, String email, String rol) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        Rol userRol = Rol.valueOf(rol);

        boolean esPropietario = ticket.getCreadoPor().getEmail().equals(email);
        boolean esSoporteOAdmin = userRol == Rol.SOPORTE || userRol == Rol.ADMIN;

        if (!esPropietario && !esSoporteOAdmin) {
            throw new AccessDeniedException("No tienes permiso para ver este ticket");
        }

        return mapToResponse(ticket);
    }

    public List<TicketResponse> listarTodos() {
        return ticketRepository.findAllByOrderByCreadoEnDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TicketResponse cambiarEstado(Long ticketId, CambioEstadoRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        ticket.setEstado(request.getEstado());
        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    public List<TicketResponse> listarVencidos() {
        return ticketRepository.findVencidos()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .titulo(ticket.getTitulo())
                .descripcion(ticket.getDescripcion())
                .prioridad(ticket.getPrioridad())
                .estado(ticket.getEstado())
                .creadoEn(ticket.getCreadoEn())
                .slaVenceEn(ticket.getSlaVenceEn())
                .vencido(ticket.isVencido())
                .creadoPor(ticket.getCreadoPor().getNombre())
                .build();
    }
}
