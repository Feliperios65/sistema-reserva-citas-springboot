package com.felop.reservasCitas.dto;
//DTO para mostrar la disponibilidad de horarios en una fecha especifica
//Este DTO se usa en:
//GET /api/appointments/availability/{fecha}

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadCitaDTO {

    //fecha consultada
    private LocalDate fecha;

    //lista de horarios disponibles para reervar
    //se representa en bloques de 30 minutos que no tienen citas asignadas
    private List<String> horariosDisponibles;

    //lista de horarios ya ocupados por citas
    //muestra al cliente que franjas estan reservadas
    private List<String> horariosOcupados;

    //contador horarios disponibles
    private Integer totalDisponibles;
}
