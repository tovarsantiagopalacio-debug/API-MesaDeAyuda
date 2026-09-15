package com.helpdesk.api.config;

import com.helpdesk.api.entity.*;
import com.helpdesk.api.repository.RefreshTokenRepository;
import com.helpdesk.api.repository.TicketRepository;
import com.helpdesk.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Usuario admin = usuarioRepository.save(Usuario.builder()
                .nombre("Admin")
                .email("admin@helpdesk.com")
                .password(passwordEncoder.encode("admin123"))
                .rol(Rol.ADMIN)
                .build());

        Usuario soporte = usuarioRepository.save(Usuario.builder()
                .nombre("Soporte")
                .email("soporte@helpdesk.com")
                .password(passwordEncoder.encode("soporte123"))
                .rol(Rol.SOPORTE)
                .build());

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Usuario")
                .email("usuario@helpdesk.com")
                .password(passwordEncoder.encode("usuario123"))
                .rol(Rol.USUARIO)
                .build());

        Ticket ticket1 = Ticket.builder()
                .titulo("Problema con el servidor")
                .descripcion("El servidor principal no responde a las peticiones HTTP")
                .prioridad(Prioridad.ALTA)
                .creadoPor(usuario)
                .build();
        ticketRepository.save(ticket1);

        Ticket ticket2 = Ticket.builder()
                .titulo("Pantalla azul en Windows")
                .descripcion("Al iniciar sesión aparece pantalla azul con error 0x0000007B")
                .prioridad(Prioridad.MEDIA)
                .creadoPor(usuario)
                .build();
        ticketRepository.save(ticket2);

        Ticket ticket3 = Ticket.builder()
                .titulo("Actualizar software")
                .descripcion("Necesito actualizar el paquete Office a la última versión")
                .prioridad(Prioridad.BAJA)
                .creadoPor(usuario)
                .build();
        ticketRepository.save(ticket3);

        Ticket vencido = Ticket.builder()
                .titulo("Ticket vencido de prueba")
                .descripcion("Este ticket tiene SLA vencido porque fue creado con fecha pasada")
                .prioridad(Prioridad.ALTA)
                .creadoPor(usuario)
                .build();

        vencido.setCreadoEn(LocalDateTime.now().minusHours(6));
        vencido.setSlaVenceEn(LocalDateTime.now().minusHours(2));
        vencido.setEstado(EstadoTicket.ABIERTO);
        ticketRepository.save(vencido);

        System.out.println("=== Datos de prueba cargados ===");
        System.out.println("Admin: admin@helpdesk.com / admin123");
        System.out.println("Soporte: soporte@helpdesk.com / soporte123");
        System.out.println("Usuario: usuario@helpdesk.com / usuario123");
    }
}
