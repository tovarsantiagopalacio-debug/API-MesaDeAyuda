package com.helpdesk.api.dto;

import com.helpdesk.api.entity.EstadoTicket;
import com.helpdesk.api.entity.Prioridad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class TicketResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private Prioridad prioridad;
    private EstadoTicket estado;
    private LocalDateTime creadoEn;
    private LocalDateTime slaVenceEn;
    private boolean vencido;
    private String creadoPor;
}
