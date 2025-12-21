package com.felop.reservasCitas.service.impl;
//Interfaz de servicio para la gestion de citas

import com.felop.reservasCitas.dto.CitaConfirmacionDTO;
import com.felop.reservasCitas.dto.CitaRequestDTO;
import com.felop.reservasCitas.dto.CitaResponseDTO;
import com.felop.reservasCitas.dto.DisponibilidadCitaDTO;
import com.felop.reservasCitas.model.EstadoCita;

import java.time.LocalDate;
import java.util.List;

public interface CitaService {

    //==== operaciones CRUD ====

    //crear nueva cita
    CitaConfirmacionDTO createCita(CitaRequestDTO dto);

    //obtener todas las citas
    List<CitaResponseDTO> getAllCitas();

    //Obtiene una cita por su ID
    CitaRequestDTO getCitaById(Long id);

    //Actualiza los datos de una cita existente
    CitaResponseDTO updateCita(Long id, CitaRequestDTO dto);

    //Elimina una cita existente
    void deleteCita(Long id);

    //==== busquedas y filtros ====

    //busca una cita por su codigo de confirmacion y sin necesidad de id
    CitaResponseDTO getCitaByCodigo(String codigo);

    //obtiene las citas de un cliente por su email
    List<CitaResponseDTO> getCitasByEmail(String email);

    //Filtrar por estado
    List<CitaResponseDTO> getCitasByEstado(EstadoCita estado);

    //obtiene las citas de una fecha especifica
    List<CitaResponseDTO> getCitasByFecha(LocalDate fecha);

    //==== disponibilidad ====

    //calcula y devuelve la disponibilidad de horarios para una fecha
    DisponibilidadCitaDTO getDisponibilidad(LocalDate fecha);

    //==== transiciones de estado ====

    //confirmar cita: pendiente -> confirmada
    CitaResponseDTO confirmarCita(Long id);

    //cancela cita
    CitaResponseDTO cancelarCita(Long id);

    //marcar cita como completada: confirmada -> completada
    CitaResponseDTO completarCita(Long id);


}
