package com.felop.reservasCitas.exceptions;

//excepcion lanzada cuando se intenta una transicion de estado invalida
/*transiciones validad:
pendiente -> confirmada
pendiente -> cancelada
confirmada -> cancelada
confirmada -> completada*/

/*transiciones invalidas:
cancelada -> confirmada
cancelada -> completada
completada -> cualquiera
cancelada -> cancelada*/

public class InvalidStateTransitionException extends RuntimeException{

//    constructor mensaje personalizado
    public InvalidStateTransitionException(String message){
        super(message);
    }

//    constructor con mensaje y causa raiz
    public InvalidStateTransitionException(String message, Throwable cause){
        super(message, cause);
    }
}
