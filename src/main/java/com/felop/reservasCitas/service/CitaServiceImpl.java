package com.felop.reservasCitas.service;

import com.felop.reservasCitas.dto.CitaConfirmacionDTO;
import com.felop.reservasCitas.dto.CitaRequestDTO;
import com.felop.reservasCitas.dto.CitaResponseDTO;
import com.felop.reservasCitas.dto.DisponibilidadCitaDTO;
import com.felop.reservasCitas.exceptions.CitaNotFoundException;
import com.felop.reservasCitas.exceptions.InvalidStateTransitionException;
import com.felop.reservasCitas.exceptions.InvalidTimeRangeException;
import com.felop.reservasCitas.exceptions.TimeSlotNotAvailableException;
import com.felop.reservasCitas.model.Cita;
import com.felop.reservasCitas.model.EstadoCita;
import com.felop.reservasCitas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

//Implementacion del servicio de gestion de citas
//contiene toda la logica del negocio y sistema

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    //inyeccion por constructor
    private final CitaRepository repository;

    //constantes configuracion del negocio
    private static final LocalTime HORARIO_APERTURA = LocalTime.of(8, 0);
    private static final LocalTime HORARIO_CIERRE = LocalTime.of(20, 0);
    private static final int SLOT_DURACION_MINUTOS = 30;
    private static final long ANTICIPACION_MINIMA_HORAS = 2;

    //==== operaciones CRUD ====

    //crear una nueva cita con validaciones
    @Override
    @Transactional
    public CitaConfirmacionDTO createCita(CitaRequestDTO dto) {
        //validar horario laboral
        validateBusinessHours(dto.getHoraInicio(), dto.getHoraFin());

        //validar que no haya cruce con otras citas
        validateNoOverlap(dto.getFecha(), dto.getHoraInicio(), dto.getHoraFin(), null);

        //validar anticipacion minima (2h)
        validateMinimumAdvance(dto.getFecha(), dto.getHoraInicio());

        //mapear DTO -> entity
        Cita cita = mapToEntity(dto);

        //generaar codigo unico de confirmacion
        cita.setCodigoConfirmacion(generateUniqueConfirmationCode());

        //establecer estado inicial
        cita.setEstado(EstadoCita.PENDIENTE);

        //guardar en bd
        Cita saved = repository.save(cita);

        //mapear entity -> ConfirmationDTO y devolver
        return mapToConfirmationDTO(saved);

    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> getAllCitas() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO getCitaById(Long id) {
        Cita cita = findByIdOrThrow(id);
        return mapToResponseDTO(cita);
    }

    /*
     * Actualiza una cita existente
     *
     * IMPORTANTE: Al validar solapamiento, excluye la propia cita
     * para permitir modificar horarios sin conflicto consigo misma.*/
    @Override
    @Transactional
    public CitaResponseDTO updateCita(Long id, CitaRequestDTO dto) {
        //verificar que la cita existe
        Cita existing = findByIdOrThrow(id);

        //validar nuevo horario
        validateBusinessHours(dto.getHoraInicio(), dto.getHoraFin());

        //validr cruce de horarios (excluyendo la propia cita con ID)
        validateNoOverlap(dto.getFecha(), dto.getHoraInicio(), dto.getHoraFin(), id);

        //validar anticipacion minima
        validateMinimumAdvance(dto.getFecha(), dto.getHoraInicio());

        // Actualizar campos (manteniendo ID, código, estado, timestamps)
        updateEntityFromDTO(existing, dto);

        //guardar cambios
        Cita updated = repository.save(existing);

        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteCita(Long id) {
        //verificar que existe antes de eliminar
        Cita cita = findByIdOrThrow(id);
        repository.delete(cita);
    }

    // ====== busquedas y filtros ======

    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO getCitaByCodigo(String codigo) {
        Cita cita = repository.findByCodigoConfirmacion(codigo)
                .orElseThrow(() -> new CitaNotFoundException(
                        "Cita con codigo " + codigo + " no encontrada"
                ));

        return mapToResponseDTO(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> getCitasByEmail(String email) {

        return repository.findByEmailOrderByFechaDesc(email)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> getCitasByEstado(EstadoCita estado) {

        return repository.findByEstadoOrderByFechaAsc(estado)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> getCitasByFecha(LocalDate fecha) {

        return repository.findByFechaOrderByHoraInicioAsc(fecha)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ====== disponibilidad ======

    /*
     * calcula la disponibilidad de horarios para una fecha*/
    @Override
    @Transactional(readOnly = true)
    public DisponibilidadCitaDTO getDisponibilidad(LocalDate fecha) {
        //genera todos los slots posibles de 8:00 a 20:00 en intervalos de 30m
        List<String> allSlots = generateAllTimeSlots();

        //obtener citas activas (pendiente o confirmada) de la fecha
        List<Cita> citasActivas = repository.findActiveAppointmentsByDate(fecha);

        //determinar slots ocupados por las citas
        List<String> occupiedSlots = new ArrayList<>();
        for (Cita c : citasActivas) {
            occupiedSlots.add(formatTimeRange(c.getHoraInicio(), c.getHoraFin()));
        }

        //calcular slots disponibles = todos -ocupados
        List<String> availableSlots = allSlots.stream()
                .filter(slot -> !isSlotOccupied(slot, citasActivas))
                .collect(Collectors.toList());

        //construir y devolver slots
        return DisponibilidadCitaDTO.builder()
                .fecha(fecha)
                .horariosDisponibles(availableSlots)
                .horariosOcupados(occupiedSlots)
                .totalDisponibles(availableSlots.size())
                .build();
    }

    // ====== transiciones de estado ======

    @Override
    @Transactional
    public CitaResponseDTO confirmarCita(Long id) {
        Cita cita = findByIdOrThrow(id);

        //validar transicion: solo pendiente -> confirmada
        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw new InvalidStateTransitionException(
                    "Solo se pueden confirmas citas en estado PENDIENTE. Estado actual: "
                            + cita.getEstado());
        }

        //cambiar estado
        cita.setEstado(EstadoCita.CONFIRMADA);

        //guardar y devolver
        Cita updated = repository.save(cita);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public CitaResponseDTO cancelarCita(Long id) {
        Cita cita = findByIdOrThrow(id);

        //validar transiciones: Pendiente -> Cancelada o Confirmada -> Cancelada
        if (cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new InvalidStateTransitionException(
                    "No se puede cancelar una cita completada");
        }
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new InvalidStateTransitionException(
                    "La cita ya está cancelada");
        }

        //cambiar estado
        cita.setEstado(EstadoCita.CANCELADA);

        //guardar y devolver
        Cita updated = repository.save(cita);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public CitaResponseDTO completarCita(Long id) {
        Cita cita = findByIdOrThrow(id);

        //validar transicion: solo Confirmada -> Completada
        if (cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new InvalidStateTransitionException(
                    "Solo se pueden completar citas confirmadas. Estado actual: "
                            + cita.getEstado()
            );
        }

        //cambiar estado
        cita.setEstado(EstadoCita.COMPLETADA);

        //guardar y devolver
        Cita updated = repository.save(cita);
        return mapToResponseDTO(updated);
    }

    //    ====Validaciones de negocio====
//    valida que el horario de la cita este dentro del horario laboral (8:00 a 20:00)
    private void validateBusinessHours(LocalTime horaInicio, LocalTime horaFin) {
        if (horaInicio.isBefore(HORARIO_APERTURA) || horaFin.isAfter(HORARIO_CIERRE)) {
            throw new InvalidTimeRangeException(
                    String.format("Las citas deben estar entre %s y %s. Horario solicitado: %s - %s",
                            HORARIO_APERTURA, HORARIO_CIERRE, horaInicio, horaFin)
            );
        }
    }

    /*Valida que no haya cruce de horarios con otras citas activas
     * Usa el query findOverlappingAppointments del repository.
     * Si excludeId no es null, excluye esa cita de la búsqueda (útil para updates).*/
    private void validateNoOverlap(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Long excludeId) {
        List<Cita> overlaping = repository.findOverlappingAppointments(fecha, horaInicio, horaFin);

        //si se esta actualizando se excluye la propia cita
        if (excludeId != null) {
            overlaping = overlaping.stream()
                    .filter(apt -> !apt.getId().equals(excludeId))
                    .collect(Collectors.toList());
        }

        if (!overlaping.isEmpty()) {
            throw new TimeSlotNotAvailableException(
                    String.format("El horario solicitado (%s - %s) ya esta ocupado",
                            horaInicio, horaFin)
            );
        }
    }

    /*Valida que la cita se cree con anticipacion minima
     *
     * Regla: las citas deben crearse con almenos dos horas de antelacion
     *
     * InvalidTimeRangeException si no cumple anticipación mínima*/
    private void validateMinimumAdvance(LocalDate fecha, LocalTime horaInicio) {
        LocalDateTime citaDateTime = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime minimoPermitido = ahora.plusHours(ANTICIPACION_MINIMA_HORAS);

        if (citaDateTime.isBefore(minimoPermitido)) {
            throw new InvalidTimeRangeException(
                    String.format("Las citas deben crearse con al menos %d de anticipacion",
                            ANTICIPACION_MINIMA_HORAS));
        }
    }

    //==== generacion de codigos ====
    /*Genera un codigo unico de confirmacion
     * Formato: APT-XXXX (APT + 4 caracteres alfanuméricos en mayúsculas)
     * Ejemplo: APT-A3F9, APT-K7M2*/
    private String generateUniqueConfirmationCode() {
        String code;
        do {
            // UUID.randomUUID()
            String uuid = UUID.randomUUID().toString().replace("-", "");
            //tomar 4 caracteres
            String sufix = uuid.substring(0, 4).toUpperCase();
            code = "APT-" + sufix;
        } while (repository.existByCodigoConfirmacion(code)); //Repetir si ya existe
        return code;
    }

    // ==== helpers disponibilidad ====

    /* Genera todos los slots de tiempo posibles.
     *
     * De 08:00 a 20:00 en intervalos de 30 minutos = 24 slots.
     * Formato: "HH:mm - HH:mm"*/
    private List<String> generateAllTimeSlots() {
        List<String> slots = new ArrayList<>();
        LocalTime current = HORARIO_APERTURA;

        while (current.isBefore(HORARIO_CIERRE)) {
            LocalTime next = current.plusMinutes(SLOT_DURACION_MINUTOS);
            slots.add(formatTimeRange(current, next));
            current = next;
        }

        return slots;
    }

    /*Verifica si un slot esta ocupado por alguna cita
     * Un slot "08:00 - 08:30" está ocupado si alguna cita solapa con él.*/
    private boolean isSlotOccupied(String slot, List<Cita> citas) {
        //Extraer inicio y fin del slot
        String[] parts = slot.split(" - ");
        LocalTime slotStart = LocalTime.parse(parts[0]);
        LocalTime slotEnd = LocalTime.parse(parts[1]);

        //verificar si alguna cita se cruza con este slot
        for (Cita c : citas) {
            if (c.getHoraInicio().isBefore(slotEnd) &&
                    c.getHoraFin().isAfter(slotStart)) {
                return true; //hay cruce
            }
        }
        return false; //no hay cruce
    }

    //formatea un rango de tiempo como String
    private String formatTimeRange(LocalTime inicio, LocalTime fin) {
        return String.format("%s - %s", inicio, fin);
    }

    // ====== mapeo dto <-> Entity ======
    /*
     * Mapea RequestDTO → Entity (para crear).
     *
     * NO incluye: id, estado, código, timestamps (generados automáticamente).
     */
    private Cita mapToEntity(CitaRequestDTO dto) {
        Cita cita = new Cita();
        cita.setNombreCliente(dto.getNombreCliente());
        cita.setEmail(dto.getEmail());
        cita.setTelefono(dto.getTelefono());
        cita.setFecha(dto.getFecha());
        cita.setHoraInicio(dto.getHoraInicio());
        cita.setHoraFin(dto.getHoraFin());
        cita.setServicio(dto.getServicio());
        cita.setPrecio(dto.getPrecio());
        cita.setNotas(dto.getNotas());

        return cita;
    }

    /*
     * Actualiza entity existente con datos del DTO.
     *
     * Mantiene: id, estado, código, timestamps (manejados por JPA).
     */
    private void updateEntityFromDTO(Cita entity, CitaRequestDTO dto) {
        entity.setNombreCliente(dto.getNombreCliente());
        entity.setEmail(dto.getEmail());
        entity.setTelefono(dto.getTelefono());
        entity.setFecha(dto.getFecha());
        entity.setHoraInicio(dto.getHoraInicio());
        entity.setHoraFin(dto.getHoraFin());
        entity.setServicio(dto.getServicio());
        entity.setPrecio(dto.getPrecio());
        entity.setNotas(dto.getNotas());
        // NO actualizamos: estado, codigoConfirmacion (se manejan por endpoints específicos)
    }

    /*
     * Mapea Entity → ResponseDTO (completo).
     *
     * Incluye cálculo de duracionMinutos.
     */
    private CitaResponseDTO mapToResponseDTO(Cita entity) {
        //calcular duracion en minutos
        long duracion = ChronoUnit.MINUTES.between(
                entity.getHoraInicio(),
                entity.getHoraFin()
        );

        return CitaResponseDTO.builder()
                .id(entity.getId())
                .nombreCliente(entity.getNombreCliente())
                .email(entity.getEmail())
                .telefono(entity.getTelefono())
                .fecha(entity.getFecha())
                .horaInicio(entity.getHoraInicio())
                .horaFin(entity.getHoraFin())
                .servicio(entity.getServicio())
                .estado(entity.getEstado())
                .precio(entity.getPrecio())
                .notas(entity.getNotas())
                .codigoConfirmacion(entity.getCodigoConfirmacion())
                .duracionMinutos(duracion)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /*
     * Mapea Entity → ConfirmationDTO (simplificado con mensaje).
     */
    private CitaConfirmacionDTO mapToConfirmationDTO(Cita entity) {
        //Genera mensaje personalizado
        String mensaje = String.format(
                "Cita reservada con éxito. Código de confirmación: %s. " +
                        "Por favor, confirme su asistencia.",
                entity.getCodigoConfirmacion()
        );

        return CitaConfirmacionDTO.builder()
                .id(entity.getId())
                .codigoConfirmacion(entity.getCodigoConfirmacion())
                .nombreCliente(entity.getNombreCliente())
                .fecha(entity.getFecha())
                .horaInicio(entity.getHoraInicio())
                .horaFin(entity.getHoraFin())
                .servicio(entity.getServicio())
                .estado(entity.getEstado())
                .mensaje(mensaje)
                .build();
    }

    // ====== helpers genericos ======

    /* Busca una cita por ID o lanza excepción si no existe.
     * Método helper para evitar repetir el patrón findById + orElseThrow.
     */
    private Cita findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(
                        "Cita con ID " + id + " no encontrada"));
    }
}
