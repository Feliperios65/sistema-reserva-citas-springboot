package com.felop.reservasCitas.model;

public enum EstadoCita {
    //Estado inicial cuando se crea la cita
    PENDIENTE,

    //El cliente confirma su asistencia
    CONFIRMADA,

    //La cita fue cancelada
    CANCELADA,

    //El servicio fue prestado exitosamente
    COMPLETADA
}
