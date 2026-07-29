package com.helpdesk.api.repository;

import com.helpdesk.api.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreadoPorIdOrderByCreadoEnDesc(Long creadoPorId);

    @Query("SELECT t FROM Ticket t WHERE t.estado <> 'RESUELTO' AND t.slaVenceEn < CURRENT_TIMESTAMP")
    List<Ticket> findVencidos();

    List<Ticket> findAllByOrderByCreadoEnDesc();
}
