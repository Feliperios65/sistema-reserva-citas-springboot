package com.felop.reservasCitas.dto;
//DTO para respuesta tras crear una cita

import com.felop.reservasCitas.model.EstadoCita;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaConfirmacionDTO {
    //id cita creada
    private Long id;

    //codigo unico de confirmacion para poder gestionar cita
    private String codigoConfirmacion;

    //nombre del cliente
    private String nombreCliente;

    //fecha de la cita creada
    private LocalDate fecha;

    //hora de inicio de la cita
    private LocalTime horaInicio;

    //hora de fin de la cita
    private LocalTime horaFin;

    //servicio solicitado en la cita
    private String servicio;

    //estado inicial de la cita ---> Pendiente despues de la creacion
    private EstadoCita estado;

    //mensaje descriptivo para el cliente
    private String mensaje;

}
