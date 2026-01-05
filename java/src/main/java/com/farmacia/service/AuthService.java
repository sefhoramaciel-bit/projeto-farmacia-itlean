package com.farmacia.service;

import com.farmacia.domain.dto.LoginRequest;
import com.farmacia.domain.dto.LoginResponse;
import com.farmacia.domain.dto.UsuarioResponse;
import com.farmacia.domain.entity.Usuario;
import com.farmacia.repository.UsuarioRepository;
import com.farmacia.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private LogService logService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        UsuarioResponse usuarioResponse = new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getAvatarUrl(),
                usuario.getCreatedAt()
        );

        // Registra log de login
        logService.registrarLogLogin(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                String.format("Login realizado: %s (%s)", usuario.getNome(), usuario.getEmail())
        );

        return new LoginResponse(token, "Bearer", usuarioResponse);
    }
}




