package com.helpdesk.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTicket estado;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(nullable = false)
    private LocalDateTime slaVenceEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_id", nullable = false)
    private Usuario creadoPor;

    @PrePersist
    protected void onCreate() {
        if (this.creadoEn == null) {
            this.creadoEn = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoTicket.ABIERTO;
        }
        if (this.slaVenceEn == null) {
            this.slaVenceEn = switch (this.prioridad) {
                case ALTA -> this.creadoEn.plusHours(4);
                case MEDIA -> this.creadoEn.plusHours(24);
                case BAJA -> this.creadoEn.plusHours(72);
            };
        }
    }

    public boolean isVencido() {
        return this.estado != EstadoTicket.RESUELTO && LocalDateTime.now().isAfter(this.slaVenceEn);
    }
}
