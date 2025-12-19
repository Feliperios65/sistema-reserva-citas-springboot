package com.felop.reservasCitas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "citas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //nombre del cliente que reserva
    @Column(nullable = false, length = 100)
    private String nombreCliente;

    //email de contacto del cliente
    @Column(nullable = false, length = 20)
    private String email;

    //telefono de contacto del cliente
    @Column(nullable = false)
    private String telefono;

    //Fecha de la cita
    @Column(nullable = false)
    private LocalDate fecha;

    //hora de inicio de la cita
    @Column(nullable = false)
    private LocalTime horaInicio;

    //hora de fin de la cita
    @Column(nullable = false)
    private LocalTime horaFin;

    //tipo de servicio solicitado
    @Column(nullable = false, length = 100)
    private String servicio;

    //estado actual de la cita
    @Enumerated(EnumType.STRING)//almacena el nombre del enum en BD vs ORDINAL que almacenaría el índice numérico
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    //precio del servicio
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;//BigDecimal se usa para evitar errores de precisión en cálculos monetarios

    //notas sobre la cita (opcional)
    @Column(length = 500)
    private String notas;

    //Codigo unico de confimacion de la cita, formato: "APT-XXXX"
    @Column(unique = true, nullable = false, length = 10)
    private String codigoConfirmacion;

    //Timestamp de creacion del registro
    @CreationTimestamp//@CreationTimestamp genera automáticamente la fecha/hora al insertar.
    @Column(nullable = false, updatable = false)//updatable=false previene modificaciones accidentales en actualizaciones.
    private LocalDateTime createdAt;

    //Timestamp de ultima actualizacion del registro
    @UpdateTimestamp//@UpdateTimestamp actualiza automáticamente en cada modificación.
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
