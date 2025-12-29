package com.felop.reservasCitas.exceptions;

//Excepcion lanzada cuando se intenta reservar un horario ya ocupado

public class TimeSlotNotAvailableException extends RuntimeException{

//    constructor con mensaje personalizado
    public TimeSlotNotAvailableException(String message){
        super(message);
    }

//    constructor con mensaje y causa raiz
    public TimeSlotNotAvailableException(String message, Throwable cause){
        super(message, cause);
    }
}
