package com.felop.reservasCitas.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

//DTO para estructurar las respuestas de error de forma organizada
//Da informacion detallada sobre errores al cliente en el formato JSON
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

//    Timestamp del momento en el que ocurrio el error
    private LocalDateTime timestamp;

//    codigo de estado HTTP
    private int status;

//    Nombre descriptivo del error
    private String error;

//    Mensaje descriptivo del error especifico proveniente de la excepcion lanzada
    private String mensaje;

//    path de la peticion que genero el error
    private String path;

//    lista de errores detallados principalmente para errores de validacion
    private List<String> details;

}

