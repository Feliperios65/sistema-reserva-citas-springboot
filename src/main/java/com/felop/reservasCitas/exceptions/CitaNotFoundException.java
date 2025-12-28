package com.felop.reservasCitas.exceptions;

//excepcion lansada cuando no se encuentra una cita solicitada
//se mapea a http 404 not found en el GlobalExceptionHandler
public class CitaNotFoundException extends RuntimeException{

//    Constructor con mensaje personalizado
    public CitaNotFoundException(String message){
        super(message);
    }

//    constructor con mensaje y causa raiz
//    util cuando la excepcion es resultado de otra excepcion
    public CitaNotFoundException(String message, Throwable cause){
        super(message, cause);
    }

}
