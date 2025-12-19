package com.felop.reservasCitas.dto;
//DTO para recibir datos de creacion/actualizacion de citas
//incluye validaciones Bean Validation y personalizadas

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaRequestDTO {

    //nombre del cliente
    //debe tener entre 2 y 100 caracteres
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombreCliente;

    //email del cliente, debe cumplir con el formato de email
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato de email es invalido")
    private String email;

    //telefono del cliente
    //acepta formatos internacionales con @patern
    @NotBlank(message = "El telefono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9\\s-]{9,15}$", message = "El formato del telefono es invalido")
    private String telefono;

    //fecha de la cita
    //@FutureOrPresent permite el día actual o fechas futuras,
    //pero no fechas pasadas
    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser anterior a hoy")
    private LocalDate fecha;

    //hora de inicio de la cita
    //la validacion dentro del horario se realiza en service
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    //hora fin de la cita
    //debe ser posterior a horaInicio (validacion personalizada)
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;

    //servicio solicitado en la cita
    @NotBlank(message = "El servicio es obligatorio")
    @Size(min = 2, max = 100, message = "El servicio debe tener entre 2 y 100 caracteres")
    private String servicio;

    //Precio del servicio
    //@DecimalMin("0.0") permite servicios "gratuitos" pero no precios negativos.
    //inclusive=true significa que 0.0 es válido.
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    private BigDecimal precio;

    //Notas adicionales
    //maximo 500 caracteres
    @Size(max = 500, message = "Las notas no puede exceder 500 caracteres")
    private String notas;

    //==== VALIDACIONES PERSONALIZADAS ====
    //la hora de fin debe ser posterior a la hora de inicio de la cita
    //@AssertTrue se evalúa después de las validaciones individuales (@NotNull)
    //Si horaInicio o horaFin son null, devuelve true para que @NotNull maneje el error
    //return true si la validación pasa, false si falla
    @AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
    public boolean isHoraFinPosterior(){
        if (horaInicio == null || horaFin == null){
            return true;// dejamos que @NotNull maneje los nulls
        }
        return horaFin.isAfter(horaInicio);
    }

    //validacion de hora de la cita
    //tiempo minimo de cita 15 minutos
    //tiempo maximo de cita 8 horas
    //ChronoUnit.MINUTES.between() calcula la diferencia exacta en minutos.
    //return true si la duración está en el rango válido
    @AssertTrue(message = "La duracion de la cita debe estar entre 15 minutos y 8 horas")
    public boolean isDuracionValida(){
        if (horaInicio == null || horaFin == null){
            return true;
        }

        long duracionMinutos = ChronoUnit.MINUTES.between(horaInicio, horaFin);
        return duracionMinutos >= 15 && duracionMinutos <= 480;
    }
}
