# Sistema de Reservas de Citas 📅

## 📖 Descripción General
Sistema completo de gestión de reservas de citas con generación automática de códigos de confirmación, validaciones avanzadas de horarios, cálculo de duraciones y control de disponibilidad. 

Este proyecto introduce el uso de **DTOs complejos** con lógica de negocio avanzada y validaciones cruzadas para asegurar la integridad de los datos antes de llegar a la capa de servicio.

---

## 📊 Modelo de Datos

### Entidad: `Appointment`
Representa la cita física en la base de datos.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | Long | PK autoincremental |
| `nombreCliente` | String | Nombre completo del cliente |
| `email` | String | Email de contacto |
| `telefono` | String | Teléfono de contacto |
| `fecha` | LocalDate | Fecha de la cita |
| `horaInicio` | LocalTime | Hora de inicio |
| `horaFin` | LocalTime | Hora de finalización |
| `servicio` | String | Tipo de servicio (ej: "Consulta") |
| `estado` | EstadoCita | Enum (PENDIENTE, CONFIRMADA, etc.) |
| `precio` | BigDecimal | Precio del servicio |
| `notas` | String | Notas adicionales (opcional) |
| `codigoConfirmacion`| String | Código único (ej: "APT-A3F9") |
| `createdAt` | LocalDateTime| Timestamp de creación |
| `updatedAt` | LocalDateTime| Timestamp de última actualización |

### Enum: `EstadoCita`
* **PENDIENTE:** Cita creada, pendiente de confirmación.
* **CONFIRMADA:** Cliente confirmó la cita.
* **CANCELADA:** Cita cancelada.
* **COMPLETADA:** Servicio prestado.

---

## 🔄 DTOs del Sistema

1.  **AppointmentRequestDTO**: Para creación y actualización. Incluye validaciones `@NotBlank`, `@Email`, `@FutureOrPresent` y validaciones lógicas (Hora fin > inicio).
2.  **AppointmentResponseDTO**: Devuelve la información completa incluyendo ID, estado, código y la duración calculada en minutos.
3.  **AppointmentConfirmationDTO**: Respuesta optimizada post-creación con un mensaje amigable y el código de gestión.
4.  **AvailabilityDTO**: Estructura para reportar los huecos libres y ocupados en una fecha específica.

---

## 🛣️ Endpoints REST
**Base URL:** `/api/v1/citas`

| Método | Endpoint | Body | Descripción |
| :--- | :--- | :--- | :--- |
| **POST** | `/` | `AppointmentRequestDTO` | Crear nueva cita |
| **GET** | `/` | - | Listar todas las citas |
| **GET** | `/{id}` | - | Obtener cita por ID |
| **GET** | `/codigo/{codigo}` | - | Buscar por código de confirmación |
| **GET** | `/cliente/email/{email}` | - | Listar citas de un cliente |
| **GET** | `/estado/{estado}` | - | Filtrar por estado |
| **GET** | `/availability/{fecha}` | - | Ver disponibilidad para una fecha |
| **PUT** | `/{id}` | `AppointmentRequestDTO` | Actualizar cita |
| **PATCH** | `/{id}/confirmar` | - | Confirmar cita |
| **PATCH** | `/{id}/cancelar` | - | Cancelar cita |
| **PATCH** | `/{id}/completar` | - | Marcar como completada |
| **DELETE** | `/{id}` | - | Eliminar cita (204 No Content) |

---

## 🔐 Reglas de Negocio

### Validaciones de Horarios
* **Horario laboral:** Solo se permiten citas entre **08:00 y 20:00**.
* **Duración:** Mínimo 15 min / Máximo 8 horas.
* **No solapamiento:** El sistema impide agendar si el bloque horario choca con una cita `CONFIRMADA` o `PENDIENTE`.
* **Anticipación:** Las citas deben reservarse con al menos **2 horas** de antelación.

### Generación de Código
* **Formato:** `APT-XXXX` (Donde X es alfanumérico aleatorio).
* **Unicidad:** El código es único y se genera automáticamente en la persistencia inicial.

### Transiciones de Estado
* `PENDIENTE` ➔ `CONFIRMADA` o `CANCELADA`.
* `CONFIRMADA` ➔ `CANCELADA` o `COMPLETADA`.
* **Restricción:** No se puede operar sobre citas `CANCELADAS` o `COMPLETADAS`.

---

## 📚 Conceptos Clave Implementados

### 1. Validaciones Cruzadas en DTOs
```java
@AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
private boolean isHoraFinValid() {
    return horaFin != null && horaInicio != null && horaFin.isAfter(horaInicio);
}

### 2. Generación de Códigos Únicos
```java
String codigo = "APT-" + UUID.randomUUID()
    .toString()
    .substring(0, 4)
    .toUpperCase();

### 3. Consultas de Solapamiento (JPA)
```java
@Query("SELECT a FROM Appointment a WHERE a.fecha = :fecha " +
       "AND a.estado IN ('PENDIENTE', 'CONFIRMADA') " +
       "AND ((a.horaInicio < :horaFin AND a.horaFin > :horaInicio))")
List<Appointment> findOverlappingAppointments(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin);
