package com.dlabogados.auth.service;

import com.dlabogados.abogado.entity.Abogado;
import com.dlabogados.abogado.repository.AbogadoRepository;
import com.dlabogados.auth.dto.AuthResponse;
import com.dlabogados.auth.dto.LoginRequest;
import com.dlabogados.auth.dto.RegisterClientRequest;
import com.dlabogados.auth.dto.UserResponse;
import com.dlabogados.auth.entity.Rol;
import com.dlabogados.auth.entity.RolNombre;
import com.dlabogados.auth.repository.RolRepository;
import com.dlabogados.cliente.entity.Cliente;
import com.dlabogados.cliente.repository.ClienteRepository;
import com.dlabogados.exception.ConflictException;
import com.dlabogados.exception.ResourceNotFoundException;
import com.dlabogados.security.jwt.JwtUtils;
import com.dlabogados.usuario.entity.Usuario;
import com.dlabogados.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final AbogadoRepository abogadoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        UserResponse userResponse = buildUserResponse(usuario);

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public UserResponse registerClient(RegisterClientRequest registerRequest) {
        if (usuarioRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("El correo ya se encuentra registrado");
        }

        if (clienteRepository.existsByDni(registerRequest.getDni())) {
            throw new ConflictException("El DNI ya se encuentra registrado");
        }

        Rol rolCliente = rolRepository.findByNombre(RolNombre.ROLE_CLIENTE)
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre(RolNombre.ROLE_CLIENTE).build()));

        Usuario usuario = Usuario.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .rol(rolCliente)
                .activo(true)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Cliente cliente = Cliente.builder()
                .usuario(usuarioGuardado)
                .nombres(registerRequest.getNombres())
                .apellidos(registerRequest.getApellidos())
                .dni(registerRequest.getDni())
                .telefono(registerRequest.getTelefono())
                .direccion(registerRequest.getDireccion())
                .build();

        clienteRepository.save(cliente);

        return buildUserResponse(usuarioGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
        return buildUserResponse(usuario);
    }

    private UserResponse buildUserResponse(Usuario usuario) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .rol(usuario.getRol().getNombre().name());

        if (usuario.getRol().getNombre() == RolNombre.ROLE_CLIENTE) {
            Optional<Cliente> optCliente = clienteRepository.findByUsuarioId(usuario.getId());
            if (optCliente.isPresent()) {
                Cliente c = optCliente.get();
                builder.clienteId(c.getId());
                builder.nombres(c.getNombres());
                builder.apellidos(c.getApellidos());
                builder.dni(c.getDni());
            }
        } else if (usuario.getRol().getNombre() == RolNombre.ROLE_ABOGADO) {
            Optional<Abogado> optAbogado = abogadoRepository.findByUsuarioId(usuario.getId());
            if (optAbogado.isPresent()) {
                Abogado a = optAbogado.get();
                builder.abogadoId(a.getId());
                builder.nombres(a.getNombres());
                builder.apellidos(a.getApellidos());
                builder.dni(a.getDni());
            }
        } else {
            builder.nombres("Administrador");
            builder.apellidos("DL Abogados");
        }

        return builder.build();
    }
}
