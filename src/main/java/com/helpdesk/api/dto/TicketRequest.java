package com.helpdesk.api.dto;

import com.helpdesk.api.entity.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketRequest {
    @NotBlank
    private String titulo;

    @NotBlank
    private String descripcion;

    @NotNull
    private Prioridad prioridad;
}
