package com.dlabogados.config;

import com.dlabogados.abogado.entity.Abogado;
import com.dlabogados.abogado.repository.AbogadoRepository;
import com.dlabogados.auth.entity.Rol;
import com.dlabogados.auth.entity.RolNombre;
import com.dlabogados.auth.repository.RolRepository;
import com.dlabogados.caso.entity.Caso;
import com.dlabogados.caso.entity.CasoAbogado;
import com.dlabogados.caso.entity.EstadoCaso;
import com.dlabogados.caso.entity.PrioridadCaso;
import com.dlabogados.caso.repository.CasoAbogadoRepository;
import com.dlabogados.caso.repository.CasoRepository;
import com.dlabogados.cita.entity.Cita;
import com.dlabogados.cita.entity.EstadoCita;
import com.dlabogados.cita.entity.ModalidadCita;
import com.dlabogados.cita.repository.CitaRepository;
import com.dlabogados.cliente.entity.Cliente;
import com.dlabogados.cliente.repository.ClienteRepository;
import com.dlabogados.disponibilidad.entity.DisponibilidadAbogado;
import com.dlabogados.disponibilidad.repository.DisponibilidadAbogadoRepository;
import com.dlabogados.especialidad.entity.Especialidad;
import com.dlabogados.especialidad.repository.EspecialidadRepository;
import com.dlabogados.notificacion.entity.Notificacion;
import com.dlabogados.notificacion.entity.TipoNotificacion;
import com.dlabogados.notificacion.repository.NotificacionRepository;
import com.dlabogados.pago.entity.EstadoPago;
import com.dlabogados.pago.entity.MetodoPago;
import com.dlabogados.pago.entity.Pago;
import com.dlabogados.pago.repository.PagoRepository;
import com.dlabogados.servicio.entity.ServicioLegal;
import com.dlabogados.servicio.repository.ServicioLegalRepository;
import com.dlabogados.usuario.entity.Usuario;
import com.dlabogados.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final AbogadoRepository abogadoRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ServicioLegalRepository servicioLegalRepository;
    private final DisponibilidadAbogadoRepository disponibilidadRepository;
    private final CasoRepository casoRepository;
    private final CasoAbogadoRepository casoAbogadoRepository;
    private final CitaRepository citaRepository;
    private final PagoRepository pagoRepository;
    private final NotificacionRepository notificacionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Roles
        Rol rolAdmin = rolRepository.findByNombre(RolNombre.ROLE_ADMIN)
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre(RolNombre.ROLE_ADMIN).build()));
        Rol rolAbogado = rolRepository.findByNombre(RolNombre.ROLE_ABOGADO)
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre(RolNombre.ROLE_ABOGADO).build()));
        Rol rolCliente = rolRepository.findByNombre(RolNombre.ROLE_CLIENTE)
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre(RolNombre.ROLE_CLIENTE).build()));

        // 2. Especialidades
        Especialidad espCivil = especialidadRepository.findByNombre("Derecho Civil")
                .orElseGet(() -> especialidadRepository.save(Especialidad.builder().nombre("Derecho Civil").descripcion("Contratos, propiedades y obligaciones").activo(true).build()));
        Especialidad espPenal = especialidadRepository.findByNombre("Derecho Penal")
                .orElseGet(() -> especialidadRepository.save(Especialidad.builder().nombre("Derecho Penal").descripcion("Defensa y querellas penales").activo(true).build()));
        Especialidad espLaboral = especialidadRepository.findByNombre("Derecho Laboral")
                .orElseGet(() -> especialidadRepository.save(Especialidad.builder().nombre("Derecho Laboral").descripcion("Indemnizaciones y despido arbitrario").activo(true).build()));
        Especialidad espFamilia = especialidadRepository.findByNombre("Derecho de Familia")
                .orElseGet(() -> especialidadRepository.save(Especialidad.builder().nombre("Derecho de Familia").descripcion("Alimentos, divorcios y tenencia").activo(true).build()));

        // 3. Servicios Legales
        ServicioLegal servAlimentos = servicioLegalRepository.findByNombre("Juicio de Alimentos")
                .orElseGet(() -> servicioLegalRepository.save(ServicioLegal.builder().especialidad(espFamilia).nombre("Juicio de Alimentos").descripcion("Demanda de pensión alimenticia").precioReferencial(new BigDecimal("1500.00")).activo(true).build()));

        // 4. Admin User
        if (!usuarioRepository.existsByEmail("admin@dlabogados.com")) {
            usuarioRepository.save(Usuario.builder()
                    .email("admin@dlabogados.com")
                    .password(passwordEncoder.encode("Admin123!"))
                    .rol(rolAdmin)
                    .activo(true)
                    .build());
        }

        // 5. Abogado 1 (Dr. Carlos Mendoza)
        Usuario userAbogado1;
        if (!usuarioRepository.existsByEmail("carlos.mendoza@dlabogados.com")) {
            userAbogado1 = usuarioRepository.save(Usuario.builder()
                    .email("carlos.mendoza@dlabogados.com")
                    .password(passwordEncoder.encode("Abogado123!"))
                    .rol(rolAbogado)
                    .activo(true)
                    .build());
        } else {
            userAbogado1 = usuarioRepository.findByEmail("carlos.mendoza@dlabogados.com").get();
        }

        Abogado abogado1 = abogadoRepository.findByUsuarioId(userAbogado1.getId())
                .orElseGet(() -> abogadoRepository.save(Abogado.builder()
                        .usuario(userAbogado1)
                        .nombres("Carlos")
                        .apellidos("Mendoza Silva")
                        .dni("41235678")
                        .telefono("987123456")
                        .numeroColegiatura("CAL-45892")
                        .perfilProfesional("Especialista en Derecho de Familia con más de 12 años de experiencia procesal.")
                        .activo(true)
                        .especialidades(Set.of(espFamilia, espCivil))
                        .build()));

        // 6. Abogado 2 (Dra. Ana Torres)
        Usuario userAbogado2;
        if (!usuarioRepository.existsByEmail("ana.torres@dlabogados.com")) {
            userAbogado2 = usuarioRepository.save(Usuario.builder()
                    .email("ana.torres@dlabogados.com")
                    .password(passwordEncoder.encode("Abogado123!"))
                    .rol(rolAbogado)
                    .activo(true)
                    .build());
        } else {
            userAbogado2 = usuarioRepository.findByEmail("ana.torres@dlabogados.com").get();
        }

        if (!abogadoRepository.findByUsuarioId(userAbogado2.getId()).isPresent()) {
            abogadoRepository.save(Abogado.builder()
                    .usuario(userAbogado2)
                    .nombres("Ana")
                    .apellidos("Torres Paredes")
                    .dni("45678912")
                    .telefono("976543210")
                    .numeroColegiatura("CAL-51204")
                    .perfilProfesional("Especialista en Litigio Civil y Contratos.")
                    .activo(true)
                    .especialidades(Set.of(espCivil, espLaboral))
                    .build());
        }

        // 7. Cliente 1 (Juan Pérez)
        Usuario userCliente1;
        if (!usuarioRepository.existsByEmail("juan.perez@gmail.com")) {
            userCliente1 = usuarioRepository.save(Usuario.builder()
                    .email("juan.perez@gmail.com")
                    .password(passwordEncoder.encode("Cliente123!"))
                    .rol(rolCliente)
                    .activo(true)
                    .build());
        } else {
            userCliente1 = usuarioRepository.findByEmail("juan.perez@gmail.com").get();
        }

        Cliente cliente1 = clienteRepository.findByUsuarioId(userCliente1.getId())
                .orElseGet(() -> clienteRepository.save(Cliente.builder()
                        .usuario(userCliente1)
                        .nombres("Juan")
                        .apellidos("Pérez López")
                        .dni("71234567")
                        .telefono("998877665")
                        .direccion("Av. Primavera 450, Santiago de Surco, Lima")
                        .build()));

        // 8. Demo Caso
        if (casoRepository.count() == 0) {
            Caso caso1 = casoRepository.save(Caso.builder()
                    .codigo("CAS-2026-0001")
                    .cliente(cliente1)
                    .servicioLegal(servAlimentos)
                    .titulo("Juicio de Alimentos y Régimen de Visitas")
                    .descripcion("Demanda de pensión alimenticia a favor de menor de edad ante el 2do Juzgado de Paz Letrado.")
                    .estado(EstadoCaso.EN_PROCESO)
                    .prioridad(PrioridadCaso.ALTA)
                    .montoAcordado(new BigDecimal("1500.00"))
                    .fechaApertura(LocalDateTime.now().minusDays(10))
                    .fechaActualizacion(LocalDateTime.now())
                    .build());

            casoAbogadoRepository.save(CasoAbogado.builder()
                    .caso(caso1)
                    .abogado(abogado1)
                    .esResponsable(true)
                    .activo(true)
                    .fechaAsignacion(LocalDateTime.now().minusDays(10))
                    .observacion("Asignación de abogado principal de familia")
                    .build());

            citaRepository.save(Cita.builder()
                    .cliente(cliente1)
                    .abogado(abogado1)
                    .caso(caso1)
                    .fecha(LocalDate.now().plusDays(2))
                    .horaInicio(LocalTime.of(10, 0))
                    .horaFin(LocalTime.of(11, 0))
                    .modalidad(ModalidadCita.PRESENCIAL)
                    .motivo("Revisión de liquidación de devengados")
                    .estado(EstadoCita.CONFIRMADA)
                    .observaciones("Presentarse con DNI original en sede central")
                    .build());

            pagoRepository.save(Pago.builder()
                    .caso(caso1)
                    .monto(new BigDecimal("500.00"))
                    .metodoPago(MetodoPago.TRANSFERENCIA)
                    .estadoPago(EstadoPago.PAGADO)
                    .numeroOperacion("TRX-98451230")
                    .fechaPago(LocalDateTime.now().minusDays(5))
                    .observacion("Adelanto del 33% del valor acordado")
                    .build());
        }

        System.out.println(">>> Base de datos de DL Abogados sincronizada y datos semilla verificados exitosamente.");
    }
}
