package com.felop.reservasCitas.controller;

import com.felop.reservasCitas.dto.CitaConfirmacionDTO;
import com.felop.reservasCitas.dto.CitaRequestDTO;
import com.felop.reservasCitas.dto.CitaResponseDTO;
import com.felop.reservasCitas.dto.DisponibilidadCitaDTO;
import com.felop.reservasCitas.model.EstadoCita;
import com.felop.reservasCitas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1/citas")
@RequiredArgsConstructor
public class CitaController {

    /*Service de citas iyectado por constructor
     * final garantiza inmutabilidad y RequiredArgsConstructor genera el contructor*/
    private final CitaService citaService;

    // ====== operaciones crud ======

    /*Crear una nueva cita
     * POST /api/v1/citas
     * @Valid activa las validaciones del bean validation del DTO
     *
     * si las validaciones fallan se lanza un MethodArgumentNotValidException
     * que es capturado y manejado por el GlobalExceptionHandler */
    @PostMapping
    public ResponseEntity<CitaConfirmacionDTO> createCita(@Valid @RequestBody CitaRequestDTO dto) {

        CitaConfirmacionDTO created = citaService.createCita(dto);

        //201 created: rrecurso creado exitosamente
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /*Obtiene todas la citas del sistema
     *
     * GET /api/v1/citas
     *
     * 200 ok: con lista de citas (vacia si no existe ninguna)*/
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> getAllCitas() {

        List<CitaResponseDTO> citas = citaService.getAllCitas();

        return ResponseEntity.ok(citas);
    }

    /*Obtiene una cita por su ID
     *
     * GET /api/v1/citas/{id}
     *
     * @PathVariable extrae el id de la url
     *
     * AppointmentNotFoundException si no existe (manejada por GlobalExceptionHandler)*/
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> getCitaById(@PathVariable Long id) {
        CitaResponseDTO cita = citaService.getCitaById(id);
        return ResponseEntity.ok(cita);
    }

    /*Actualiza una cita existente
     *
     * PUT /api/v1/citas/{id}
     *
     * PUT se utiliza para actualizacion completa (todos los campos)
     *el codigo de confirmacion y el estado no se actualizan aqui
     * tienen endpoints diferentes
     *
     * si las validaciones fallan se lanza un MethodArgumentNotValidException
     * que es capturado y manejado por el GlobalExceptionHandler*/
    @PutMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> updateCita(@PathVariable Long id, @Valid @RequestBody CitaRequestDTO dto) {
        CitaResponseDTO updated = citaService.updateCita(id, dto);
        return ResponseEntity.ok(updated);
    }

    /*Elimina una cita
     *
     * DELETE /api/v1/citas/{id}
     *
     * 204 No Content (eliminación exitosa sin cuerpo de respuesta)*/
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCita(@PathVariable Long id) {
        citaService.deleteCita(id);

        return ResponseEntity.noContent().build();
    }

    // ====== busquedas y filtros ======

    /*Busca una cita por su codigo de confirmacion
     *
     * GET /api/v1/citas/codigo/{codigo}
     *
     * util para clientes que consultan su cita sin necesidad de ID
     *
     * 200 ok con datos de la cita*/
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<CitaResponseDTO> getCitaByCodigo(@PathVariable String codigo) {

        CitaResponseDTO cita = citaService.getCitaByCodigo(codigo);
        return ResponseEntity.ok(cita);
    }

    /*obtiene las citas de un cliente por su email
     *
     * GET /api/v1/citas/cliente/email/{email}
     *
     * 200 OK con lista de citas del cliente*/
    @GetMapping("/cliente/email/{email}")
    public ResponseEntity<List<CitaResponseDTO>> getCitasByEmail(@PathVariable String email) {
        List<CitaResponseDTO> citas = citaService.getCitasByEmail(email);
        return ResponseEntity.ok(citas);
    }

    /*Filtra citas por estado
     *
     * GET /api/v1/citas/estado/{estado}
     *
     * Spring convierte automáticamente el String a enum EstadoCita.
     * Si el valor no es válido, lanza 400 Bad Request.
     *
     * 200 OK con lista de citas con ese estado*/
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<CitaResponseDTO>> getCitasByEstado(@PathVariable EstadoCita estado) {
        List<CitaResponseDTO> citas = citaService.getCitasByEstado(estado);
        return ResponseEntity.ok(citas);
    }

    /*Obtiene todas las citas de una fecha especifica
     *
     * GET /api/v1/citas/fecha/{fecha}
     *
     * @DateTimeFormat indica el formato ISO de fecha yyyy-MM-dd
     * Spring parsea automaticamente el String a LocalDate
     *
     * 200 OK con lista de citas de ese día*/
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<CitaResponseDTO>> getCitasByFecha(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<CitaResponseDTO> citas = citaService.getCitasByFecha(fecha);
        return ResponseEntity.ok(citas);
    }

    /*Obtiee la disponibilidad de horarios para una fecha
     *
     * GET /api/v1/citas/disponibilidad/{fecha}
     *
     * devuelve slots de 30m entre 8:00 y 20:00
     * marcando cuales estan disponibles y cuales ocupados
     *
     * 200 OK con DTO de disponibilidad */
    @GetMapping("/disponibilidad/{fecha}")
    public ResponseEntity<DisponibilidadCitaDTO> getDisponibilidad(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        DisponibilidadCitaDTO disponibilidad = citaService.getDisponibilidad(fecha);
        return ResponseEntity.ok(disponibilidad);
    }

    // ====== transiciones de estado ======

    /*Confirma una cita: pendiente -> confirmada
     *
     * PATCH /api/v1/citas/{id}/confirmar
     * PATCH para actualizaciones parciales y no del recurso completo
     *
     * Solo funciona si la cita está en estado PENDIENTE.
     * Si no, el Service lanza InvalidStateTransitionException
     *
     * 200 OK con datos actualizados (estado CONFIRMADA)*/
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<CitaResponseDTO> confirmarCita(@PathVariable Long id) {
        CitaResponseDTO confirmed = citaService.confirmarCita(id);
        return ResponseEntity.ok(confirmed);
    }

    /*Cancela una cita
     *
     *PATCH /api/v1/citas/{id}/cancelar
     *
     * transiciones validas:
     * pendiente -> cancelada
     * confirmada -> cancelada
     *
     * no permite cancelar citas completas
     *
     * 200 OK con datos actualizados (estado CANCELADA) */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(@PathVariable Long id) {
        CitaResponseDTO cancelled = citaService.cancelarCita(id);
        return ResponseEntity.ok(cancelled);
    }

    /*Marca una cita como completada: confirmada -> completada
     *
     *PATCH /api/v1/citas/{id}/completar
     *solo funciona si la cita esta confirmada
     * y este es un estado final, asi que no permite mas cambios
     *
     * 200 OK con datos actualizados (estado COMPLETADA) */
    @PatchMapping("/{id}/completar")
    public ResponseEntity<CitaResponseDTO> completarCita(@PathVariable Long id){
        CitaResponseDTO completed = citaService.completarCita(id);
        return ResponseEntity.ok(completed);
    }


}
