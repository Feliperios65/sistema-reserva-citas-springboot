package com.felop.reservasCitas.repository;
//Respositorio para gestion de citas
//proporciona metodos de acceso a datos mediante query methods
//y consultaspersonalizadas con @Query
import com.felop.reservasCitas.model.Cita;
import com.felop.reservasCitas.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    //busca cita por su codigo unico de confirmacion
    Optional<Cita> findByCodigoConfirmacion(String codigo);

    //verifica si existe una cita con el codigo de confirmacion dado
    boolean existByCodigoConfirmacion(String codigo);

    //obtiene todas la citas de un cliente por su email
    List<Cita> findByEmailOrderByFechaDesc(String email);

    //filtra citas por estado
    List<Cita> findByEstadoOrderByFechaAsc(EstadoCita estado);

    //obtiene todas las citas de una fecha especifica
    List<Cita> findByFechaOrderByHoraInicioAsc(LocalDate fecha);

    //detecta si hay cruce de horarios para una nueva cita
    //solo considera citas pendiente o confirmadas
    //las citas canceladas o completadas no bloquean horarios
    @Query("SELECT a FROM Cita a WHERE a.fecha = :fecha " +
            "AND a.estado IN ('PENDIENTE', 'CONFIRMADA') " +
            "AND ((a.horaInicio < :horaFin AND a.horaFin > :horaInicio))")
    List<Cita> findOverlappingAppointments(
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio")LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

    //obtiene todas las citas activas (Pendientes o Completadas) de una fecha en especifico
    @Query("SELECT a FROM Cita a WHERE a.fecha = :fecha" +
            "AND a.estado IN ('PENDIENTE','CONFIRMADA') " +
            "ORDER BY a.horaInicio ASC")
    List<Cita> findActiveAppointmentsByDate(
            @Param("fecha") LocalDate fecha
    );
}
