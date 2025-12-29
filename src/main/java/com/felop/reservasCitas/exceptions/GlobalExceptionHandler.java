package com.felop.reservasCitas.exceptions;

/*Manejador global de excepciones para toda la aplicacion
* @RestControllerAdvice combina @ControllerAdvice + @ResctController:
* captura excepciones lanzadas por cualquier @RestController
* devuelve las respuestas automaticamente como json
* centraliza el manejo de errores en un unico punto*/

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //==== Excepciones del negocio ====

    /*maneja CitaNotFoundException
    * se lanza cuando:
    * GET -> con ID inexistente
    * GET -> con codigo de cita inexistente
    * PUT/PATCH/DELETE sobre una cita inexistete
    *
    * HTTP Status: 404 Not Found*/

    @ExceptionHandler(CitaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCitaNotFound(CitaNotFoundException ex){

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())//Not Found
                .mensaje(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    /*Maneja InvalidTimeRangeException
    *
    * Se lanza cuando:
    * Horario fuera del rango laboral (no entre 08:00 y 20:00)
    * Anticipacion minima no cumplid
    * Otras validacionde de horario en Service
    *
    * HTTP Status: 400 Bad Request*/
    @ExceptionHandler(InvalidTimeRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTimeRange(InvalidTimeRangeException ex){

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())//BAD_REQUEST
                .mensaje(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /*maneja TimeSlotNotAvailableException
    *
    * se lanza cuando:
    * POST -> con horario que solapa con otra cita activa
    * PUT -> actualizando a horario ocupado
    *
    * HTTP Status: 409 conflict*/

    @ExceptionHandler(TimeSlotNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleTimeSlotNotAvailable(TimeSlotNotAvailableException ex){

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())//CONFLICT
                .mensaje(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    /*maneja InvalidStateTransitionException
    *
    * se lanza cuando:
    * PATCH -> confirmar con estado ≠ pendiente
     * PATCH -> cancelar con estado = completada
    * PATCH -> completar con estado ≠  confirmada
    *
    * HTTP Status: 400 Bad Request*/
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransition(InvalidStateTransitionException ex){

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())//BAD_REQUEST
                .mensaje(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    //==== validaciones bean validation ====

    /*maneja errores de validacion Bean Validation (@Valid en el controller)
    *
    * se lanza cuando:
    * POST/PUT con CitaRequestDTO que falla validaciones
    *  @NotBlank, @Email, @Size, @DecimalMin, @AssertTrue, etc. fallan
    *
    * HTTP Status: 400 Bad Request
    *
    * procesamiento:
    * extraer todos los FieldError del BindingResult
    * mapear cada error a su mensaje por defecto
    * devolver lista en el campo "details"*/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex){

        //extraer mensaje de todos los errores de campo
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())//BAD_REQUEST
                .mensaje("Error de validacion en los datos enviados")
                .details(errors)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // ==== Excepcion generica (catch-all) ====

    /*maneja cualquier excepcion no capturada por los handlers especificos
    * actua como red de seguridad par:
    * - Errores inesperados de programación (NullPointerException, etc.)
    * - Errores de infraestructura (BD caída, timeouts, etc.)
    * - Cualquier RuntimeException no prevista
    *
    * HTTP Status: 500 Internal Server Error
    *
    * buenas practicas:
    * - Loguear el error completo con stack trace en servidor
    * - Devolver mensaje genérico al cliente
    * - Incluir timestamp para correlación con logs */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex){

        // En producción, loguear el error completo:
        // log.error("Error inesperado: ", ex);
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())//INTERNAL_SERVER_ERROR
                .mensaje("Ha ocurrido un error inesperado. POr favor, contacte al administrador.")
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}
