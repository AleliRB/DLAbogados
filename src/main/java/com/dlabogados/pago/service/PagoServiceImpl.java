package com.dlabogados.pago.service;

import com.dlabogados.caso.entity.Caso;
import com.dlabogados.caso.repository.CasoRepository;
import com.dlabogados.cita.entity.Cita;
import com.dlabogados.cita.repository.CitaRepository;
import com.dlabogados.exception.BadRequestException;
import com.dlabogados.exception.ResourceNotFoundException;
import com.dlabogados.historial.service.HistorialActividadService;
import com.dlabogados.pago.dto.MercadoPagoPreferenceRequest;
import com.dlabogados.pago.dto.MercadoPagoPreferenceResponse;
import com.dlabogados.pago.dto.PagoCreateRequest;
import com.dlabogados.pago.dto.PagoResponse;
import com.dlabogados.pago.dto.ResumenPagoCasoResponse;
import com.dlabogados.pago.entity.EstadoPago;
import com.dlabogados.pago.entity.MetodoPago;
import com.dlabogados.pago.entity.Pago;
import com.dlabogados.pago.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final CasoRepository casoRepository;
    private final CitaRepository citaRepository;
    private final HistorialActividadService historialActividadService;

    @Value("${app.payment.mercadopago.public-key:TEST-df839120-testkey}")
    private String mpPublicKey;

    @Value("${app.payment.success-url:http://localhost:4200/cliente/citas?payment=success}")
    private String successUrl;

    @Value("${app.payment.failure-url:http://localhost:4200/cliente/citas?payment=failure}")
    private String failureUrl;

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerPorCaso(Long casoId) {
        return pagoRepository.findByCasoId(casoId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerTodos(String estado) {
        List<Pago> pagos;
        if (estado != null && !estado.trim().isEmpty()) {
            try {
                EstadoPago est = EstadoPago.valueOf(estado.toUpperCase());
                pagos = pagoRepository.findByEstadoPago(est);
            } catch (IllegalArgumentException e) {
                pagos = pagoRepository.findAllByOrderByFechaRegistroDesc();
            }
        } else {
            pagos = pagoRepository.findAllByOrderByFechaRegistroDesc();
        }
        return pagos.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerPorCliente(Long clienteId) {
        return pagoRepository.findByClienteIdOrderByFechaRegistroDesc(clienteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenPagoCasoResponse obtenerResumenSaldoCaso(Long casoId) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado con id: " + casoId));

        BigDecimal totalPagado = pagoRepository.sumPagosPagadosByCasoId(casoId);
        BigDecimal saldoPendiente = caso.getMontoAcordado().subtract(totalPagado);
        if (saldoPendiente.compareTo(BigDecimal.ZERO) < 0) {
            saldoPendiente = BigDecimal.ZERO;
        }

        double porcentaje = 0.0;
        if (caso.getMontoAcordado().compareTo(BigDecimal.ZERO) > 0) {
            porcentaje = totalPagado.multiply(BigDecimal.valueOf(100))
                    .divide(caso.getMontoAcordado(), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        List<PagoResponse> historial = obtenerPorCaso(casoId);

        return ResumenPagoCasoResponse.builder()
                .casoId(caso.getId())
                .casoCodigo(caso.getCodigo())
                .montoAcordado(caso.getMontoAcordado())
                .totalPagado(totalPagado)
                .saldoPendiente(saldoPendiente)
                .porcentajePagado(porcentaje)
                .historialPagos(historial)
                .build();
    }

    @Override
    @Transactional
    public PagoResponse registrarPago(Long casoId, PagoCreateRequest request) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado con id: " + casoId));

        if (request.getEstadoPago() == EstadoPago.PAGADO) {
            BigDecimal totalActual = pagoRepository.sumPagosPagadosByCasoId(casoId);
            BigDecimal nuevoTotal = totalActual.add(request.getMonto());
            if (nuevoTotal.compareTo(caso.getMontoAcordado()) > 0) {
                throw new BadRequestException(String.format("El monto a pagar (S/ %.2f) excede el saldo pendiente (S/ %.2f)",
                        request.getMonto(), caso.getMontoAcordado().subtract(totalActual)));
            }
        }

        Cita citaAsociada = request.getCitaId() != null ? citaRepository.findById(request.getCitaId()).orElse(null) : null;

        Pago pago = Pago.builder()
                .caso(caso)
                .cita(citaAsociada)
                .cliente(caso.getCliente())
                .monto(request.getMonto())
                .concepto(request.getConcepto() != null && !request.getConcepto().isBlank()
                        ? request.getConcepto()
                        : "Honorarios legales - Caso " + caso.getCodigo())
                .metodoPago(request.getMetodoPago())
                .estadoPago(request.getEstadoPago())
                .numeroOperacion(request.getNumeroOperacion())
                .comprobanteUrl(request.getComprobanteUrl())
                .fechaPago(request.getEstadoPago() == EstadoPago.PAGADO ? LocalDateTime.now() : null)
                .observacion(request.getObservacion())
                .build();

        Pago guardado = pagoRepository.save(pago);

        // Auditoría centralizada
        historialActividadService.registrarActividad(
                caso.getCliente() != null && caso.getCliente().getUsuario() != null ? caso.getCliente().getUsuario().getId() : null,
                "PAGO",
                guardado.getId(),
                "REGISTRO_PAGO",
                "Pago de S/ " + guardado.getMonto() + " (" + guardado.getMetodoPago() + ") registrado para caso " + caso.getCodigo()
        );

        return mapToResponse(guardado);
    }

    @Override
    @Transactional
    public PagoResponse registrarPagoDirecto(PagoCreateRequest request) {
        if (request.getCasoId() != null) {
            return registrarPago(request.getCasoId(), request);
        }
        if (request.getCitaId() != null) {
            Cita cita = citaRepository.findById(request.getCitaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + request.getCitaId()));

            Pago pago = Pago.builder()
                    .caso(cita.getCaso())
                    .cita(cita)
                    .cliente(cita.getCliente())
                    .monto(request.getMonto())
                    .concepto(request.getConcepto() != null && !request.getConcepto().isBlank()
                            ? request.getConcepto()
                            : "Consulta Legal - Cita #" + cita.getId())
                    .metodoPago(request.getMetodoPago())
                    .estadoPago(request.getEstadoPago())
                    .numeroOperacion(request.getNumeroOperacion())
                    .comprobanteUrl(request.getComprobanteUrl())
                    .fechaPago(request.getEstadoPago() == EstadoPago.PAGADO ? LocalDateTime.now() : null)
                    .observacion(request.getObservacion())
                    .build();

            if (request.getEstadoPago() == EstadoPago.PAGADO) {
                cita.setObservaciones((cita.getObservaciones() != null ? cita.getObservaciones() + " | " : "") +
                        "Pago registrado (" + request.getMetodoPago() + " - " + request.getNumeroOperacion() + ")");
                citaRepository.save(cita);
            }

            Pago guardado = pagoRepository.save(pago);

            historialActividadService.registrarActividad(
                    cita.getCliente() != null && cita.getCliente().getUsuario() != null ? cita.getCliente().getUsuario().getId() : null,
                    "PAGO",
                    guardado.getId(),
                    "REGISTRO_PAGO",
                    "Pago de S/ " + guardado.getMonto() + " (" + guardado.getMetodoPago() + ") registrado para Cita #" + cita.getId()
            );

            return mapToResponse(guardado);
        }
        throw new BadRequestException("Debe especificar un caso (casoId) o una cita (citaId) para registrar el pago");
    }

    @Override
    @Transactional
    public PagoResponse anularPago(Long pagoId) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + pagoId));

        pago.setEstadoPago(EstadoPago.ANULADO);
        pago.setObservacion(pago.getObservacion() != null ? pago.getObservacion() + " [ANULADO]" : "Pago Anulado por Administración");

        Pago actualizado = pagoRepository.save(pago);

        // Auditoría
        historialActividadService.registrarActividad(
                null,
                "PAGO",
                actualizado.getId(),
                "ANULACION_PAGO",
                "Pago ID #" + actualizado.getId() + " anulado por administración."
        );

        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public MercadoPagoPreferenceResponse crearPreferenciaPago(MercadoPagoPreferenceRequest request) {
        String prefId = "MP-PREF-" + System.currentTimeMillis();
        String initPoint = successUrl + "&preference_id=" + prefId + "&cita_id=" + (request.getCitaId() != null ? request.getCitaId() : 0);

        return MercadoPagoPreferenceResponse.builder()
                .preferenceId(prefId)
                .initPoint(initPoint)
                .sandboxInitPoint(initPoint)
                .publicKey(mpPublicKey)
                .status("CREATED")
                .externalReference(request.getCitaId() != null ? "CITA-" + request.getCitaId() : "CASO-" + request.getCasoId())
                .build();
    }

    @Override
    @Transactional
    public PagoResponse confirmarPagoOnline(Long citaId, String paymentId, String status) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + citaId));

        boolean aprobado = "approved".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status) || "APROBADO".equalsIgnoreCase(status);

        BigDecimal monto = (cita.getServicio() != null && cita.getServicio().getPrecioReferencial() != null)
                ? cita.getServicio().getPrecioReferencial()
                : new BigDecimal("150.00");

        if (aprobado) {
            cita.setEstado(com.dlabogados.cita.entity.EstadoCita.CONFIRMADA);
            cita.setObservaciones((cita.getObservaciones() != null ? cita.getObservaciones() + " | " : "") + 
                    "Pago confirmado online vía Mercado Pago. Transacción: " + (paymentId != null ? paymentId : "MP-ONLINE"));
            citaRepository.save(cita);

            // Persistir Pago en MySQL vinculado a la cita y al cliente
            Pago pago = Pago.builder()
                    .caso(cita.getCaso())
                    .cita(cita)
                    .cliente(cita.getCliente())
                    .monto(monto)
                    .metodoPago(MetodoPago.TARJETA)
                    .estadoPago(EstadoPago.PAGADO)
                    .numeroOperacion(paymentId != null ? paymentId : "MP-" + System.currentTimeMillis())
                    .fechaPago(LocalDateTime.now())
                    .observacion("Pago online Mercado Pago aprobado para cita legal #" + cita.getId())
                    .build();

            Pago guardado = pagoRepository.save(pago);

            // Auditoría
            historialActividadService.registrarActividad(
                    cita.getCliente() != null && cita.getCliente().getUsuario() != null ? cita.getCliente().getUsuario().getId() : null,
                    "PAGO",
                    guardado.getId(),
                    "CONFIRMACION_PAGO_ONLINE",
                    "Pago online aprobado de S/ " + monto + " para Cita #" + cita.getId()
            );

            return mapToResponse(guardado);
        } else {
            cita.setEstado(com.dlabogados.cita.entity.EstadoCita.CANCELADA);
            cita.setObservaciones((cita.getObservaciones() != null ? cita.getObservaciones() + " | " : "") + "Intento de pago rechazado o fallido.");
            citaRepository.save(cita);

            Pago pagoFallido = Pago.builder()
                    .caso(cita.getCaso())
                    .cita(cita)
                    .cliente(cita.getCliente())
                    .monto(monto)
                    .metodoPago(MetodoPago.TARJETA)
                    .estadoPago(EstadoPago.ANULADO)
                    .numeroOperacion(paymentId != null ? paymentId : "FAIL-" + System.currentTimeMillis())
                    .observacion("Intento de pago en línea rechazado.")
                    .build();
            Pago guardado = pagoRepository.save(pagoFallido);
            return mapToResponse(guardado);
        }
    }

    private PagoResponse mapToResponse(Pago p) {
        String concepto = "Consulta Legal";
        if (p.getCaso() != null) {
            concepto = "Honorario Caso: " + p.getCaso().getCodigo() + " - " + p.getCaso().getTitulo();
        } else if (p.getCita() != null && p.getCita().getServicio() != null) {
            concepto = "Cita: " + p.getCita().getServicio().getNombre();
        }

        String clienteNom = "Cliente DL";
        if (p.getCliente() != null) {
            clienteNom = p.getCliente().getNombres() + " " + p.getCliente().getApellidos();
        } else if (p.getCaso() != null && p.getCaso().getCliente() != null) {
            clienteNom = p.getCaso().getCliente().getNombres() + " " + p.getCaso().getCliente().getApellidos();
        }

        return PagoResponse.builder()
                .id(p.getId())
                .casoId(p.getCaso() != null ? p.getCaso().getId() : null)
                .casoCodigo(p.getCaso() != null ? p.getCaso().getCodigo() : "N/A")
                .citaId(p.getCita() != null ? p.getCita().getId() : null)
                .clienteId(p.getCliente() != null ? p.getCliente().getId() : null)
                .clienteNombre(clienteNom)
                .concepto(concepto)
                .monto(p.getMonto())
                .metodoPago(p.getMetodoPago())
                .estadoPago(p.getEstadoPago())
                .numeroOperacion(p.getNumeroOperacion())
                .comprobanteUrl(p.getComprobanteUrl())
                .fechaPago(p.getFechaPago())
                .fechaRegistro(p.getFechaRegistro())
                .observacion(p.getObservacion())
                .build();
    }
}
