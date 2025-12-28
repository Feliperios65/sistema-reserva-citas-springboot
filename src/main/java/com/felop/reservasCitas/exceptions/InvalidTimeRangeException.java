package com.felop.reservasCitas.exceptions;

//Excepcion lanzada cuando el rango de horario de una cita es invalido

public class InvalidTimeRangeException extends RuntimeException {

//    constructor con mensaje personalizado
    public InvalidTimeRangeException(String message){
        super(message);
    }

//    constructor con mensaje y causa raiz
    public InvalidTimeRangeException(String message, Throwable cause){
        super(message, cause);
    }
}
