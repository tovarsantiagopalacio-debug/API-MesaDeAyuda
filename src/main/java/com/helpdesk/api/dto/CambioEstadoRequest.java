package com.helpdesk.api.dto;

import com.helpdesk.api.entity.EstadoTicket;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambioEstadoRequest {
    @NotNull
    private EstadoTicket estado;
}
