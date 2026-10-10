package com.dlabogados.cita.service;

import com.dlabogados.abogado.entity.Abogado;
import com.dlabogados.abogado.repository.AbogadoRepository;
import com.dlabogados.caso.entity.Caso;
import com.dlabogados.caso.entity.CasoAbogado;
import com.dlabogados.caso.entity.EstadoCaso;
import com.dlabogados.caso.entity.PrioridadCaso;
import com.dlabogados.caso.repository.CasoAbogadoRepository;
import com.dlabogados.caso.repository.CasoRepository;
import com.dlabogados.cita.dto.*;
import com.dlabogados.cita.entity.Cita;
import com.dlabogados.cita.entity.EstadoCita;
import com.dlabogados.cita.repository.CitaRepository;
import com.dlabogados.cliente.entity.Cliente;
import com.dlabogados.cliente.repository.ClienteRepository;
import com.dlabogados.disponibilidad.entity.DisponibilidadAbogado;
import com.dlabogados.disponibilidad.repository.DisponibilidadAbogadoRepository;
import com.dlabogados.exception.BadRequestException;
import com.dlabogados.exception.ConflictException;
import com.dlabogados.exception.ResourceNotFoundException;
import com.dlabogados.pago.entity.Pago;
import com.dlabogados.pago.repository.PagoRepository;
import com.dlabogados.servicio.entity.ServicioLegal;
import com.dlabogados.servicio.repository.ServicioLegalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final ClienteRepository clienteRepository;
    private final AbogadoRepository abogadoRepository;
    private final CasoRepository casoRepository;
    private final CasoAbogadoRepository casoAbogadoRepository;
    private final ServicioLegalRepository servicioLegalRepository;
    private final DisponibilidadAbogadoRepository disponibilidadRepository;
    private final PagoRepository pagoRepository;
    private final com.dlabogados.historial.service.HistorialActividadService historialActividadService;

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> obtenerTodas() {
        return citaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> obtenerPorCliente(Long clienteId) {
        return citaRepository.findByClienteId(clienteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> obtenerPorAbogado(Long abogadoId) {
        return citaRepository.findByAbogadoId(abogadoId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> obtenerPorCaso(Long casoId) {
        return citaRepository.findByCasoId(casoId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaOcupadaDTO> obtenerOcupadas(Long abogadoId, LocalDate fecha) {
        List<Cita> citas = citaRepository.findCitasActivasDelDia(abogadoId, fecha);
        return citas.stream()
                .map(c -> new CitaOcupadaDTO(c.getHoraInicio(), c.getHoraFin()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioDisponibleDTO> obtenerHorariosDisponibles(Long abogadoId, LocalDate fecha, Long servicioId) {
        if (fecha == null || fecha.isBefore(LocalDate.now())) {
            return java.util.Collections.emptyList();
        }

        Abogado abogado = abogadoRepository.findById(abogadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Abogado no encontrado con id: " + abogadoId));

        if (Boolean.FALSE.equals(abogado.getActivo()) || (abogado.getUsuario() != null && Boolean.FALSE.equals(abogado.getUsuario().getActivo()))) {
            return java.util.Collections.emptyList();
        }

        if (servicioId != null) {
            ServicioLegal servicio = servicioLegalRepository.findById(servicioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Servicio legal no encontrado con id: " + servicioId));
            Long espId = servicio.getEspecialidad().getId();
            boolean tieneEspecialidad = abogado.getEspecialidades().stream().anyMatch(e -> e.getId().equals(espId));
            if (!tieneEspecialidad) {
                return java.util.Collections.emptyList();
            }
        }

        String diaSemanaSpanish = obtenerDiaSemanaEspañol(fecha);
        if ("DOMINGO".equalsIgnoreCase(diaSemanaSpanish)) {
            return java.util.Collections.emptyList();
        }

        List<DisponibilidadAbogado> disponibilidades = disponibilidadRepository.findByAbogadoIdAndDiaSemanaAndActivoTrue(abogadoId, diaSemanaSpanish);

        List<LocalTime[]> rangosAtencion = new java.util.ArrayList<>();
        if (disponibilidades.isEmpty()) {
            rangosAtencion.add(new LocalTime[]{ LocalTime.of(9, 0), LocalTime.of(18, 0) });
        } else {
            for (DisponibilidadAbogado d : disponibilidades) {
                rangosAtencion.add(new LocalTime[]{ d.getHoraInicio(), d.getHoraFin() });
            }
        }

        List<Cita> citasOcupadas = citaRepository.findCitasActivasDelDia(abogadoId, fecha);
        boolean esHoy = fecha.isEqual(LocalDate.now());
        LocalTime ahora = LocalTime.now();

        List<HorarioDisponibleDTO> resultado = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("hh:mm a", new Locale("es", "ES"));

        for (LocalTime[] rango : rangosAtencion) {
            LocalTime slotInicio = rango[0];
            LocalTime finRango = rango[1];

            while (!slotInicio.plusHours(1).isAfter(finRango)) {
                LocalTime slotFin = slotInicio.plusHours(1);

                if (!esHoy || slotInicio.isAfter(ahora)) {
                    final LocalTime sIni = slotInicio;
                    final LocalTime sFin = slotFin;

                    boolean seSolapa = citasOcupadas.stream().anyMatch(c ->
                        sIni.isBefore(c.getHoraFin()) && sFin.isAfter(c.getHoraInicio())
                    );

                    if (!seSolapa) {
                        String inicioFmt = sIni.format(dtf);
                        String finFmt = sFin.format(dtf);
                        resultado.add(HorarioDisponibleDTO.builder()
                                .horaInicio(sIni)
                                .horaFin(sFin)
                                .horaInicioFormato(inicioFmt)
                                .horaFinFormato(finFmt)
                                .etiqueta(inicioFmt + " - " + finFmt)
                                .build());
                    }
                }

                slotInicio = slotInicio.plusHours(1);
            }
        }

        resultado.sort(java.util.Comparator.comparing(HorarioDisponibleDTO::getHoraInicio));
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponse obtenerPorId(Long id) {
        Cita c = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + id));
        return mapToResponse(c);
    }

    @Override
    @Transactional
    public CitaResponse crear(CitaCreateRequest request) {
        if (request.getFecha() != null && request.getFecha().isBefore(LocalDate.now())) {
            throw new BadRequestException("No se pueden solicitar citas para fechas pasadas");
        }

        Cliente cliente = null;
        if (request.getClienteId() != null && request.getClienteId() > 0) {
            cliente = clienteRepository.findById(request.getClienteId()).orElse(null);
        }

        if (cliente == null) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String email = auth.getName();
                cliente = clienteRepository.findByUsuarioEmail(email).orElse(null);
            }
        }

        if (cliente == null) {
            throw new ResourceNotFoundException("Cliente no encontrado para el agendamiento de cita");
        }

        Abogado abogado = abogadoRepository.findById(request.getAbogadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Abogado no encontrado"));

        ServicioLegal servicio = null;
        if (request.getServicioId() != null) {
            servicio = servicioLegalRepository.findById(request.getServicioId()).orElse(null);
        }

        Caso caso = null;
        if (request.getCasoId() != null) {
            caso = casoRepository.findById(request.getCasoId()).orElse(null);
        }

        if (request.getHoraFin().isBefore(request.getHoraInicio()) || request.getHoraFin().equals(request.getHoraInicio())) {
            throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Regla: Validar disponibilidad configurada del abogado
        validarDisponibilidadAbogado(abogado.getId(), request.getFecha(), request.getHoraInicio(), request.getHoraFin());

        // Regla: Prevenir la doble reserva del abogado
        validarSinSuperposicion(abogado.getId(), request.getFecha(), request.getHoraInicio(), request.getHoraFin(), null);

        Cita cita = Cita.builder()
                .cliente(cliente)
                .abogado(abogado)
                .servicio(servicio)
                .caso(caso)
                .fecha(request.getFecha())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .modalidad(request.getModalidad())
                .motivo(request.getMotivo())
                .estado(EstadoCita.PENDIENTE)
                .enlaceVirtual(request.getEnlaceVirtual())
                .observaciones(request.getObservaciones())
                .build();

        Cita guardada = citaRepository.save(cita);

        historialActividadService.registrarActividad(
                cliente.getUsuario() != null ? cliente.getUsuario().getId() : null,
                "CITA",
                guardada.getId(),
                "CREACION_CITA",
                "Cita registrada para " + guardada.getFecha() + " (" + guardada.getHoraInicio() + " - " + guardada.getHoraFin() + ") con Dr(a). " + abogado.getNombres() + " " + abogado.getApellidos()
        );

        return mapToResponse(guardada);
    }

    @Override
    @Transactional
    public CitaResponse confirmar(Long id, CitaConfirmarRequest request) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        cita.setEstado(EstadoCita.CONFIRMADA);
        if (request.getEnlaceVirtual() != null) cita.setEnlaceVirtual(request.getEnlaceVirtual());
        if (request.getObservaciones() != null) cita.setObservaciones(request.getObservaciones());

        // Regla: Si la cita aún no tiene un expediente legal, se genera automáticamente el Caso
        if (cita.getCaso() == null) {
            generarCasoDesdeCita(cita);
        }

        Cita actualizada = citaRepository.save(cita);

        historialActividadService.registrarActividad(
                cita.getAbogado() != null && cita.getAbogado().getUsuario() != null ? cita.getAbogado().getUsuario().getId() : null,
                "CITA",
                actualizada.getId(),
                "CONFIRMACION_CITA",
                "Cita #" + actualizada.getId() + " confirmada y vinculada a expediente " + (actualizada.getCaso() != null ? actualizada.getCaso().getCodigo() : "N/A")
        );

        return mapToResponse(actualizada);
    }

    @Override
    @Transactional
    public CitaResponse atender(Long id, String observaciones) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        cita.setEstado(EstadoCita.ATENDIDA);
        if (observaciones != null) cita.setObservaciones(observaciones);

        // Asegurar que cuente con su expediente creado y vinculado
        if (cita.getCaso() == null) {
            generarCasoDesdeCita(cita);
        }

        Cita actualizada = citaRepository.save(cita);

        historialActividadService.registrarActividad(
                cita.getAbogado() != null && cita.getAbogado().getUsuario() != null ? cita.getAbogado().getUsuario().getId() : null,
                "CITA",
                actualizada.getId(),
                "ATENCION_CITA",
                "Cita #" + actualizada.getId() + " concluida y marcada como atendida."
        );

        return mapToResponse(actualizada);
    }

    private synchronized Caso generarCasoDesdeCita(Cita cita) {
        if (cita.getCaso() != null) {
            return cita.getCaso();
        }

        String currentYear = String.valueOf(LocalDate.now().getYear());
        Long count = casoRepository.countByCodigoYear(currentYear);
        String codigoGenerado = String.format("CAS-%s-%04d", currentYear, count + 1);

        BigDecimal monto = (cita.getServicio() != null && cita.getServicio().getPrecioReferencial() != null)
                ? cita.getServicio().getPrecioReferencial()
                : new BigDecimal("1500.00");

        String titulo = (cita.getServicio() != null)
                ? cita.getServicio().getNombre() + " - " + cita.getCliente().getNombres() + " " + cita.getCliente().getApellidos()
                : "Patrocinio Legal: " + (cita.getMotivo() != null && !cita.getMotivo().isBlank() ? cita.getMotivo() : "Asesoría Legal");

        Caso nuevoCaso = Caso.builder()
                .codigo(codigoGenerado)
                .cliente(cita.getCliente())
                .servicioLegal(cita.getServicio())
                .titulo(titulo)
                .descripcion("Expediente aperturado a partir de la confirmación de la cita #" + cita.getId() + ". Consulta: " + (cita.getMotivo() != null ? cita.getMotivo() : "Sin especificar"))
                .estado(EstadoCaso.EN_PROCESO)
                .prioridad(PrioridadCaso.MEDIA)
                .montoAcordado(monto)
                .fechaApertura(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        Caso casoGuardado = casoRepository.save(nuevoCaso);

        if (cita.getAbogado() != null) {
            CasoAbogado ca = CasoAbogado.builder()
                    .caso(casoGuardado)
                    .abogado(cita.getAbogado())
                    .esResponsable(true)
                    .activo(true)
                    .fechaAsignacion(LocalDateTime.now())
                    .observacion("Asignación automática al aprobar cita #" + cita.getId())
                    .build();
            casoAbogadoRepository.save(ca);
        }

        cita.setCaso(casoGuardado);

        List<Pago> pagosCita = pagoRepository.findByCitaId(cita.getId());
        for (Pago p : pagosCita) {
            if (p.getCaso() == null) {
                p.setCaso(casoGuardado);
                pagoRepository.save(p);
            }
        }

        historialActividadService.registrarActividad(
                cita.getAbogado() != null && cita.getAbogado().getUsuario() != null ? cita.getAbogado().getUsuario().getId() : null,
                "CASO",
                casoGuardado.getId(),
                "CREACION_CASO_AUTOMATICO",
                "Expediente " + casoGuardado.getCodigo() + " generado automáticamente al aprobar cita #" + cita.getId()
        );

        return casoGuardado;
    }

    @Override
    @Transactional
    public CitaResponse cancelar(Long id, String motivo) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        cita.setEstado(EstadoCita.CANCELADA);
        if (motivo != null) {
            String obs = cita.getObservaciones() != null ? cita.getObservaciones() + " | Motivo cancelación: " + motivo : "Motivo cancelación: " + motivo;
            cita.setObservaciones(obs);
        }

        Cita actualizada = citaRepository.save(cita);

        historialActividadService.registrarActividad(
                null,
                "CITA",
                actualizada.getId(),
                "CANCELACION_CITA",
                "Cita #" + actualizada.getId() + " cancelada. Motivo: " + (motivo != null ? motivo : "Sin motivo")
        );

        return mapToResponse(actualizada);
    }

    @Override
    @Transactional
    public CitaResponse reprogramar(Long id, CitaReprogramarRequest request) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        if (request.getNuevaFecha() != null && request.getNuevaFecha().isBefore(LocalDate.now())) {
            throw new BadRequestException("No se pueden reprogramar citas para fechas pasadas");
        }

        validarDisponibilidadAbogado(cita.getAbogado().getId(), request.getNuevaFecha(), request.getNuevaHoraInicio(), request.getNuevaHoraFin());
        validarSinSuperposicion(cita.getAbogado().getId(), request.getNuevaFecha(), request.getNuevaHoraInicio(), request.getNuevaHoraFin(), id);

        cita.setFecha(request.getNuevaFecha());
        cita.setHoraInicio(request.getNuevaHoraInicio());
        cita.setHoraFin(request.getNuevaHoraFin());
        cita.setEstado(EstadoCita.REPROGRAMADA);

        if (request.getObservaciones() != null) {
            cita.setObservaciones(request.getObservaciones());
        }

        Cita actualizada = citaRepository.save(cita);
        return mapToResponse(actualizada);
    }

    private void validarDisponibilidadAbogado(Long abogadoId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        String diaSemanaSpanish = obtenerDiaSemanaEspañol(fecha);

        if ("DOMINGO".equalsIgnoreCase(diaSemanaSpanish)) {
            throw new BadRequestException("El estudio jurídico y los abogados no atienden los días DOMINGO");
        }

        List<DisponibilidadAbogado> disponibilidades = disponibilidadRepository.findByAbogadoIdAndDiaSemanaAndActivoTrue(abogadoId, diaSemanaSpanish);

        if (disponibilidades.isEmpty()) {
            // Horario de atención estándar para días laborales (08:00 - 19:00) si no hay franja personalizada configurada para el día
            LocalTime inicioEstándar = LocalTime.of(8, 0);
            LocalTime finEstándar = LocalTime.of(19, 0);
            if (horaInicio.isBefore(inicioEstándar) || horaFin.isAfter(finEstándar)) {
                throw new BadRequestException("El horario solicitado está fuera del horario de atención del estudio (08:00 - 19:00)");
            }
            return;
        }

        boolean dentroDeHorario = disponibilidades.stream().anyMatch(d ->
                (!horaInicio.isBefore(d.getHoraInicio())) && (!horaFin.isAfter(d.getHoraFin()))
        );

        if (!dentroDeHorario) {
            throw new BadRequestException("El horario solicitado está fuera del horario de atención del abogado para ese día");
        }
    }

    private void validarSinSuperposicion(Long abogadoId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Long excludeId) {
        List<Cita> solapadas;
        if (excludeId != null) {
            solapadas = citaRepository.findOverlappingCitasExcluding(abogadoId, fecha, horaInicio, horaFin, excludeId);
        } else {
            solapadas = citaRepository.findOverlappingCitas(abogadoId, fecha, horaInicio, horaFin);
        }

        if (!solapadas.isEmpty()) {
            throw new ConflictException("El horario seleccionado ya no se encuentra disponible debido a una reserva confirmada o en proceso. Por favor elija otro horario.");
        }
    }

    private String obtenerDiaSemanaEspañol(LocalDate fecha) {
        String day = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")).toUpperCase();
        day = day.replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U");
        return day;
    }

    private CitaResponse mapToResponse(Cita c) {
        return CitaResponse.builder()
                .id(c.getId())
                .clienteId(c.getCliente().getId())
                .clienteNombre(c.getCliente().getNombres() + " " + c.getCliente().getApellidos())
                .abogadoId(c.getAbogado().getId())
                .abogadoNombre(c.getAbogado().getNombres() + " " + c.getAbogado().getApellidos())
                .servicioId(c.getServicio() != null ? c.getServicio().getId() : null)
                .servicioNombre(c.getServicio() != null ? c.getServicio().getNombre() : null)
                .casoId(c.getCaso() != null ? c.getCaso().getId() : null)
                .casoCodigo(c.getCaso() != null ? c.getCaso().getCodigo() : null)
                .casoTitulo(c.getCaso() != null ? c.getCaso().getTitulo() : null)
                .fecha(c.getFecha())
                .horaInicio(c.getHoraInicio())
                .horaFin(c.getHoraFin())
                .modalidad(c.getModalidad())
                .motivo(c.getMotivo())
                .estado(c.getEstado())
                .enlaceVirtual(c.getEnlaceVirtual())
                .observaciones(c.getObservaciones())
                .fechaCreacion(c.getFechaCreacion())
                .build();
    }
}
