package com.dlabogados.caso.service;

import com.dlabogados.abogado.entity.Abogado;
import com.dlabogados.abogado.repository.AbogadoRepository;
import com.dlabogados.caso.dto.*;
import com.dlabogados.caso.entity.Caso;
import com.dlabogados.caso.entity.CasoAbogado;
import com.dlabogados.caso.entity.EstadoCaso;
import com.dlabogados.caso.repository.CasoAbogadoRepository;
import com.dlabogados.caso.repository.CasoRepository;
import com.dlabogados.cliente.entity.Cliente;
import com.dlabogados.cliente.repository.ClienteRepository;
import com.dlabogados.exception.BadRequestException;
import com.dlabogados.exception.ResourceNotFoundException;
import com.dlabogados.pago.repository.PagoRepository;
import com.dlabogados.servicio.entity.ServicioLegal;
import com.dlabogados.servicio.repository.ServicioLegalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CasoServiceImpl implements CasoService {

    private final CasoRepository casoRepository;
    private final CasoAbogadoRepository casoAbogadoRepository;
    private final ClienteRepository clienteRepository;
    private final AbogadoRepository abogadoRepository;
    private final ServicioLegalRepository servicioLegalRepository;
    private final PagoRepository pagoRepository;
    private final com.dlabogados.historial.service.HistorialActividadService historialActividadService;

    @Override
    @Transactional(readOnly = true)
    public List<CasoResponse> obtenerTodos() {
        return casoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CasoResponse> obtenerPorCliente(Long clienteId) {
        return casoRepository.findByClienteId(clienteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CasoResponse> obtenerPorAbogado(Long abogadoId) {
        List<Long> casoIds = casoAbogadoRepository.findCasoIdsByAbogadoId(abogadoId);
        return casoRepository.findAllById(casoIds).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CasoResponse> obtenerPorEstado(EstadoCaso estado) {
        return casoRepository.findByEstado(estado).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CasoResponse obtenerPorId(Long id) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado con id: " + id));
        return mapToResponse(caso);
    }

    @Override
    @Transactional
    public CasoResponse crear(CasoCreateRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + request.getClienteId()));

        ServicioLegal servicio = servicioLegalRepository.findById(request.getServicioId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio legal no encontrado con id: " + request.getServicioId()));

        Caso casoOrigen = null;
        if (request.getCasoOrigenId() != null) {
            casoOrigen = casoRepository.findById(request.getCasoOrigenId()).orElse(null);
        }

        String codigoGenerado = generarCodigoCaso();

        Caso caso = Caso.builder()
                .codigo(codigoGenerado)
                .cliente(cliente)
                .servicioLegal(servicio)
                .casoOrigen(casoOrigen)
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .estado(EstadoCaso.ABIERTO)
                .prioridad(request.getPrioridad())
                .montoAcordado(request.getMontoAcordado())
                .fechaApertura(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        Caso casoGuardado = casoRepository.save(caso);

        if (request.getAbogadoResponsableId() != null) {
            Abogado abogado = abogadoRepository.findById(request.getAbogadoResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Abogado no encontrado con id: " + request.getAbogadoResponsableId()));

            CasoAbogado ca = CasoAbogado.builder()
                    .caso(casoGuardado)
                    .abogado(abogado)
                    .esResponsable(true)
                    .activo(true)
                    .fechaAsignacion(LocalDateTime.now())
                    .observacion("Asignación inicial al crear el caso")
                    .build();

            casoAbogadoRepository.save(ca);
        }

        historialActividadService.registrarActividad(
                cliente.getUsuario() != null ? cliente.getUsuario().getId() : null,
                "CASO",
                casoGuardado.getId(),
                "CREACION_CASO",
                "Expediente " + casoGuardado.getCodigo() + " aperturado: " + casoGuardado.getTitulo()
        );

        return mapToResponse(casoGuardado);
    }

    @Override
    @Transactional
    public CasoResponse actualizar(Long id, CasoUpdateRequest request) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado con id: " + id));

        validarNoCerrado(caso);

        if (request.getServicioId() != null) {
            ServicioLegal servicio = servicioLegalRepository.findById(request.getServicioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Servicio legal no encontrado con id: " + request.getServicioId()));
            caso.setServicioLegal(servicio);
        }

        if (request.getTitulo() != null) caso.setTitulo(request.getTitulo());
        if (request.getDescripcion() != null) caso.setDescripcion(request.getDescripcion());
        if (request.getPrioridad() != null) caso.setPrioridad(request.getPrioridad());
        if (request.getMontoAcordado() != null) caso.setMontoAcordado(request.getMontoAcordado());

        Caso actualizado = casoRepository.save(caso);
        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public CasoResponse cambiarEstado(Long id, CambiarEstadoCasoRequest request) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado con id: " + id));

        validarNoCerrado(caso);

        EstadoCaso anterior = caso.getEstado();
        caso.setEstado(request.getEstado());
        if (request.getEstado() == EstadoCaso.FINALIZADO || request.getEstado() == EstadoCaso.CERRADO) {
            caso.setFechaCierre(LocalDateTime.now());
        }

        Caso actualizado = casoRepository.save(caso);

        historialActividadService.registrarActividad(
                null,
                "CASO",
                actualizado.getId(),
                "CAMBIO_ESTADO_CASO",
                "Expediente " + actualizado.getCodigo() + " cambió de estado: " + anterior + " -> " + request.getEstado() +
                        (request.getObservacion() != null ? " [Obs: " + request.getObservacion() + "]" : "")
        );

        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public CasoResponse asignarAbogado(Long casoId, AsignarAbogadoRequest request) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado con id: " + casoId));

        validarNoCerrado(caso);

        Abogado abogado = abogadoRepository.findById(request.getAbogadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Abogado no encontrado con id: " + request.getAbogadoId()));

        if (Boolean.TRUE.equals(request.getEsResponsable())) {
            // Desmarcar responsable actual
            Optional<CasoAbogado> respActual = casoAbogadoRepository.findByCasoIdAndEsResponsableTrueAndActivoTrue(casoId);
            respActual.ifPresent(ca -> {
                ca.setEsResponsable(false);
                casoAbogadoRepository.save(ca);
            });
        }

        Optional<CasoAbogado> asignacionExistente = casoAbogadoRepository.findByCasoIdAndAbogadoIdAndActivoTrue(casoId, request.getAbogadoId());
        if (asignacionExistente.isPresent()) {
            CasoAbogado ca = asignacionExistente.get();
            ca.setEsResponsable(request.getEsResponsable());
            ca.setObservacion(request.getObservacion());
            casoAbogadoRepository.save(ca);
        } else {
            CasoAbogado nuevaAsignacion = CasoAbogado.builder()
                    .caso(caso)
                    .abogado(abogado)
                    .esResponsable(request.getEsResponsable())
                    .activo(true)
                    .fechaAsignacion(LocalDateTime.now())
                    .observacion(request.getObservacion())
                    .build();
            casoAbogadoRepository.save(nuevaAsignacion);
        }

        return mapToResponse(caso);
    }

    @Override
    @Transactional
    public CasoResponse desasignarAbogado(Long casoId, Long abogadoId) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado con id: " + casoId));

        validarNoCerrado(caso);

        Optional<CasoAbogado> ca = casoAbogadoRepository.findByCasoIdAndAbogadoIdAndActivoTrue(casoId, abogadoId);
        if (ca.isPresent()) {
            CasoAbogado asignacion = ca.get();
            asignacion.setActivo(false);
            asignacion.setFechaFin(LocalDateTime.now());
            casoAbogadoRepository.save(asignacion);
        }

        return mapToResponse(caso);
    }

    private void validarNoCerrado(Caso caso) {
        if (caso.getEstado() == EstadoCaso.FINALIZADO || caso.getEstado() == EstadoCaso.CERRADO) {
            throw new BadRequestException("Los casos en estado FINALIZADO o CERRADO no pueden ser modificados directamente. Debe solicitar una reapertura.");
        }
    }

    private synchronized String generarCodigoCaso() {
        String currentYear = String.valueOf(LocalDate.now().getYear());
        Long count = casoRepository.countByCodigoYear(currentYear);
        return String.format("CAS-%s-%04d", currentYear, count + 1);
    }

    private CasoResponse mapToResponse(Caso c) {
        List<CasoAbogado> asignaciones = casoAbogadoRepository.findByCasoId(c.getId());

        List<CasoAbogadoResponse> abogadosDTO = asignaciones.stream()
                .map(ca -> CasoAbogadoResponse.builder()
                        .id(ca.getId())
                        .abogadoId(ca.getAbogado().getId())
                        .abogadoNombre(ca.getAbogado().getNombres() + " " + ca.getAbogado().getApellidos())
                        .abogadoColegiatura(ca.getAbogado().getNumeroColegiatura())
                        .esResponsable(ca.getEsResponsable())
                        .activo(ca.getActivo())
                        .fechaAsignacion(ca.getFechaAsignacion())
                        .fechaFin(ca.getFechaFin())
                        .observacion(ca.getObservacion())
                        .build())
                .collect(Collectors.toList());

        Long respId = null;
        String respNombre = null;
        Optional<CasoAbogado> responsable = asignaciones.stream()
                .filter(ca -> Boolean.TRUE.equals(ca.getActivo()) && Boolean.TRUE.equals(ca.getEsResponsable()))
                .findFirst();

        if (responsable.isPresent()) {
            respId = responsable.get().getAbogado().getId();
            respNombre = responsable.get().getAbogado().getNombres() + " " + responsable.get().getAbogado().getApellidos();
        }

        BigDecimal totalPagado = pagoRepository.sumPagosPagadosByCasoId(c.getId());
        BigDecimal saldoPendiente = c.getMontoAcordado().subtract(totalPagado);
        if (saldoPendiente.compareTo(BigDecimal.ZERO) < 0) {
            saldoPendiente = BigDecimal.ZERO;
        }

        double porcentaje = 0.0;
        if (c.getMontoAcordado().compareTo(BigDecimal.ZERO) > 0) {
            porcentaje = totalPagado.multiply(BigDecimal.valueOf(100))
                    .divide(c.getMontoAcordado(), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return CasoResponse.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .clienteId(c.getCliente().getId())
                .clienteNombre(c.getCliente().getNombres() + " " + c.getCliente().getApellidos())
                .clienteDni(c.getCliente().getDni())
                .servicioId(c.getServicioLegal().getId())
                .servicioNombre(c.getServicioLegal().getNombre())
                .especialidadNombre(c.getServicioLegal().getEspecialidad().getNombre())
                .casoOrigenId(c.getCasoOrigen() != null ? c.getCasoOrigen().getId() : null)
                .casoOrigenCodigo(c.getCasoOrigen() != null ? c.getCasoOrigen().getCodigo() : null)
                .titulo(c.getTitulo())
                .descripcion(c.getDescripcion())
                .estado(c.getEstado())
                .prioridad(c.getPrioridad())
                .montoAcordado(c.getMontoAcordado())
                .totalPagado(totalPagado)
                .saldoPendiente(saldoPendiente)
                .porcentajePagado(porcentaje)
                .fechaApertura(c.getFechaApertura())
                .fechaCierre(c.getFechaCierre())
                .fechaActualizacion(c.getFechaActualizacion())
                .abogadoResponsableId(respId)
                .abogadoResponsableNombre(respNombre)
                .abogadosAsignados(abogadosDTO)
                .build();
    }
}
