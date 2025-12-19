package com.felop.reservasCitas.dto;
//DTO para devolver info completa de una cita a un cliente
//Este DTO se usa en:
//GET /api/appointments/{id}
//GET /api/appointments (lista completa)
//PUT /api/appointments/{id} (tras actualizar)
//PATCH /api/appointments/{id}/confirmar|cancelar|completar

import com.felop.reservasCitas.model.EstadoCita;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaResponseDTO {

    //identificador unico de la cita
    private Long id;

    //nombre del cliente que reserva
    private String nombreCliente;

    //email de contacto del cliente
    private String email;

    //telefono de contacto del cliente
    private String telefono;

    //Fecha de la cita
    private LocalDate fecha;

    //hora de inicio de la cita
    private LocalTime horaInicio;

    //hora de fin de la cita
    private LocalTime horaFin;

    //tipo de servicio solicitado
    private String servicio;

    //estado actual de la cita
    private EstadoCita estado;

    //precio del servicio
    private BigDecimal precio;

    //notas sobre la cita (opcional)
    private String notas;

    //Codigo unico de confimacion de la cita, formato: "APT-XXXX"
    //El cliente puede usar este código para:
    //Confirmar su cita
    //Consultar detalles
    //Cancelar la cita
    private String codigoConfirmacion;

    //duracion de la cita en minutos
    private Long duracionMinutos;

    //Timestamp de creacion del registro
    private LocalDateTime createdAt;

    //Timestamp de ultima actualizacion del registro
    private LocalDateTime updatedAt;
}
