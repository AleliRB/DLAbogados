package com.dlabogados.abogado.service;

import com.dlabogados.abogado.dto.AbogadoRequest;
import com.dlabogados.abogado.dto.AbogadoResponse;
import com.dlabogados.abogado.entity.Abogado;
import com.dlabogados.abogado.repository.AbogadoRepository;
import com.dlabogados.auth.entity.Rol;
import com.dlabogados.auth.entity.RolNombre;
import com.dlabogados.auth.repository.RolRepository;
import com.dlabogados.especialidad.dto.EspecialidadDTO;
import com.dlabogados.especialidad.entity.Especialidad;
import com.dlabogados.especialidad.repository.EspecialidadRepository;
import com.dlabogados.exception.ConflictException;
import com.dlabogados.exception.ResourceNotFoundException;
import com.dlabogados.usuario.entity.Usuario;
import com.dlabogados.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.dlabogados.servicio.entity.ServicioLegal;
import com.dlabogados.servicio.repository.ServicioLegalRepository;

@Service
@RequiredArgsConstructor
public class AbogadoServiceImpl implements AbogadoService {

    private final AbogadoRepository abogadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ServicioLegalRepository servicioLegalRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<AbogadoResponse> obtenerTodos() {
        return abogadoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbogadoResponse> obtenerActivos() {
        return abogadoRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo()) && Boolean.TRUE.equals(a.getUsuario().getActivo()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbogadoResponse> obtenerPorServicio(Long servicioId) {
        ServicioLegal servicio = servicioLegalRepository.findById(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio legal no encontrado con id: " + servicioId));

        Long especialidadId = servicio.getEspecialidad().getId();
        List<Abogado> abogados = abogadoRepository.findDistinctByEspecialidadesIdAndActivoTrue(especialidadId);
        
        if (abogados.isEmpty()) {
            abogados = abogadoRepository.findByActivoTrue();
        }

        return abogados.stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo()) && Boolean.TRUE.equals(a.getUsuario().getActivo()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AbogadoResponse obtenerPorId(Long id) {
        Abogado abogado = abogadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abogado no encontrado con id: " + id));
        return mapToResponse(abogado);
    }

    @Override
    @Transactional(readOnly = true)
    public AbogadoResponse obtenerPorUsuarioId(Long usuarioId) {
        Abogado abogado = abogadoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Abogado no encontrado para usuario id: " + usuarioId));
        return mapToResponse(abogado);
    }

    @Override
    @Transactional
    public AbogadoResponse registrar(AbogadoRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El correo ya está registrado");
        }
        if (abogadoRepository.existsByDni(request.getDni())) {
            throw new ConflictException("El DNI ya está registrado");
        }
        if (abogadoRepository.existsByNumeroColegiatura(request.getNumeroColegiatura())) {
            throw new ConflictException("El número de colegiatura ya está registrado");
        }

        Rol rolAbogado = rolRepository.findByNombre(RolNombre.ROLE_ABOGADO)
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre(RolNombre.ROLE_ABOGADO).build()));

        String encodedPass = passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "Abogado123!");

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .password(encodedPass)
                .rol(rolAbogado)
                .activo(true)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Set<Especialidad> especialidades = new HashSet<>();
        if (request.getEspecialidadIds() != null && !request.getEspecialidadIds().isEmpty()) {
            especialidades = new HashSet<>(especialidadRepository.findAllById(request.getEspecialidadIds()));
        }

        Abogado abogado = Abogado.builder()
                .usuario(usuarioGuardado)
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .dni(request.getDni())
                .telefono(request.getTelefono())
                .numeroColegiatura(request.getNumeroColegiatura())
                .perfilProfesional(request.getPerfilProfesional())
                .activo(true)
                .especialidades(especialidades)
                .build();

        Abogado abogadoGuardado = abogadoRepository.save(abogado);
        return mapToResponse(abogadoGuardado);
    }

    @Override
    @Transactional
    public AbogadoResponse actualizar(Long id, AbogadoRequest request) {
        Abogado abogado = abogadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abogado no encontrado con id: " + id));

        if (request.getNombres() != null) abogado.setNombres(request.getNombres());
        if (request.getApellidos() != null) abogado.setApellidos(request.getApellidos());
        if (request.getTelefono() != null) abogado.setTelefono(request.getTelefono());
        if (request.getNumeroColegiatura() != null) abogado.setNumeroColegiatura(request.getNumeroColegiatura());
        if (request.getPerfilProfesional() != null) abogado.setPerfilProfesional(request.getPerfilProfesional());

        if (request.getActivo() != null) {
            abogado.setActivo(request.getActivo());
            abogado.getUsuario().setActivo(request.getActivo());
        }

        if (request.getEspecialidadIds() != null) {
            Set<Especialidad> especialidades = new HashSet<>(especialidadRepository.findAllById(request.getEspecialidadIds()));
            abogado.setEspecialidades(especialidades);
        }

        Abogado abogadoActualizado = abogadoRepository.save(abogado);
        return mapToResponse(abogadoActualizado);
    }

    @Override
    @Transactional
    public void cambiarEstadoActivo(Long id, Boolean activo) {
        Abogado abogado = abogadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abogado no encontrado con id: " + id));
        abogado.setActivo(activo);
        abogado.getUsuario().setActivo(activo);
        abogadoRepository.save(abogado);
    }

    private AbogadoResponse mapToResponse(Abogado a) {
        Set<EspecialidadDTO> espDTOs = a.getEspecialidades().stream()
                .map(e -> EspecialidadDTO.builder()
                        .id(e.getId())
                        .nombre(e.getNombre())
                        .descripcion(e.getDescripcion())
                        .activo(e.getActivo())
                        .build())
                .collect(Collectors.toSet());

        return AbogadoResponse.builder()
                .id(a.getId())
                .usuarioId(a.getUsuario().getId())
                .email(a.getUsuario().getEmail())
                .nombres(a.getNombres())
                .apellidos(a.getApellidos())
                .dni(a.getDni())
                .telefono(a.getTelefono())
                .numeroColegiatura(a.getNumeroColegiatura())
                .perfilProfesional(a.getPerfilProfesional())
                .activo(a.getActivo() && a.getUsuario().getActivo())
                .fechaRegistro(a.getFechaRegistro())
                .especialidades(espDTOs)
                .build();
    }
}
