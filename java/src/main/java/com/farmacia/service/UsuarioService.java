package com.farmacia.service;

import com.farmacia.domain.dto.UsuarioRequest;
import com.farmacia.domain.dto.UsuarioResponse;
import com.farmacia.domain.entity.Usuario;
import com.farmacia.exception.BusinessException;
import com.farmacia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ImageService imageService;

    @Autowired
    private LogService logService;

    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        return create(request, null);
    }

    @Transactional
    public UsuarioResponse create(UsuarioRequest request, MultipartFile avatarFile) {
        // Valida nome
        if (request.getNome() == null || request.getNome().trim().isEmpty()) {
            throw new BusinessException("O campo Nome é obrigatório, por favor preencha.");
        }
        
        // Valida email
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BusinessException("O campo Email é obrigatório, por favor preencha.");
        }
        
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("O email informado já existe, por favor alterar.");
        }
        
        // Valida senha
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException("O campo Senha é obrigatório, por favor preencha.");
        }
        if (request.getPassword().trim().length() < 6) {
            throw new BusinessException("O campo Senha deve ter no mínimo 6 caracteres, por favor alterar.");
        }
        
        // Valida perfil (role)
        if (request.getRole() == null) {
            throw new BusinessException("O campo Perfil é obrigatório, por favor selecione um perfil.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRole(request.getRole());

        // Processa avatar se fornecido
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = imageService.uploadAvatar(avatarFile);
            usuario.setAvatarUrl(avatarUrl);
        } else if (request.getAvatarUrl() != null && !request.getAvatarUrl().trim().isEmpty()) {
            // Permite passar avatarUrl diretamente (para compatibilidade)
            usuario.setAvatarUrl(request.getAvatarUrl());
        }

        usuario = usuarioRepository.save(usuario);
        
        // Registra log com data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"nome\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"data\":\"%s\"}", 
                usuario.getNome(), usuario.getEmail(), usuario.getRole(), dataFormatada);
        logService.registrarLog("CREATE", "USUARIO", usuario.getId(), 
                "Usuário criado: " + usuario.getNome(), detalhes);
        
        return toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findAll() {
        return usuarioRepository.findAll().stream()
                .sorted((u1, u2) -> u1.getNome().compareToIgnoreCase(u2.getNome()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse findById(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado, por favor selecione um usuário válido."));
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse update(UUID id, UsuarioRequest request) {
        return update(id, request, null);
    }

    @Transactional
    public UsuarioResponse update(UUID id, UsuarioRequest request, MultipartFile avatarFile) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado, por favor selecione um usuário válido."));

        // Valida nome
        if (request.getNome() == null || request.getNome().trim().isEmpty()) {
            throw new BusinessException("O campo Nome é obrigatório, por favor preencha.");
        }
        
        // Valida email
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BusinessException("O campo Email é obrigatório, por favor preencha.");
        }

        if (!usuario.getEmail().equals(request.getEmail()) && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("O email informado já existe, por favor alterar.");
        }

        // Valida perfil (role) - obrigatório também na atualização
        if (request.getRole() == null) {
            throw new BusinessException("O campo Perfil é obrigatório, por favor selecione um perfil.");
        }

        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (request.getPassword().trim().length() < 6) {
                throw new BusinessException("O campo Senha deve ter no mínimo 6 caracteres, por favor alterar.");
            }
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        usuario.setRole(request.getRole());
        
        // Processa avatar se fornecido
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Remove avatar antigo do sistema de arquivos
            if (usuario.getAvatarUrl() != null && !usuario.getAvatarUrl().isEmpty()) {
                imageService.deleteImage(usuario.getAvatarUrl());
            }
            // Faz upload do novo avatar
            String avatarUrl = imageService.uploadAvatar(avatarFile);
            usuario.setAvatarUrl(avatarUrl);
        } else if (request.getAvatarUrl() != null && !request.getAvatarUrl().trim().isEmpty()) {
            // Permite passar avatarUrl diretamente (para compatibilidade)
            usuario.setAvatarUrl(request.getAvatarUrl());
        }
        // Se não for fornecido avatar nem avatarUrl, mantém o avatar atual (não altera)

        usuario = usuarioRepository.save(usuario);
        
        // Registra log com data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"nome\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"data\":\"%s\"}", 
                usuario.getNome(), usuario.getEmail(), usuario.getRole(), dataFormatada);
        logService.registrarLog("UPDATE", "USUARIO", usuario.getId(), 
                "Usuário atualizado: " + usuario.getNome(), detalhes);
        
        return toResponse(usuario);
    }

    @Transactional
    public String delete(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado, por favor selecione um usuário válido."));
        
        String nomeUsuario = usuario.getNome();
        
        // Remove avatar do sistema de arquivos
        if (usuario.getAvatarUrl() != null && !usuario.getAvatarUrl().isEmpty()) {
            imageService.deleteImage(usuario.getAvatarUrl());
        }
        
        usuarioRepository.deleteById(id);
        
        // Registra log com data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"nome\":\"%s\",\"data\":\"%s\"}", nomeUsuario, dataFormatada);
        logService.registrarLog("DELETE", "USUARIO", id, 
                "Usuário deletado: " + nomeUsuario, detalhes);
        
        return String.format("Usuário '%s' deletado com sucesso.", nomeUsuario);
    }

    @Transactional
    public UsuarioResponse uploadAvatar(UUID id, MultipartFile file) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado, por favor selecione um usuário válido."));

        // Obtém usuário autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Usuario usuarioAutenticado = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado, por favor faça login novamente."));

        // Valida permissão: ADMIN pode alterar qualquer avatar, VENDEDOR apenas o próprio
        if (!usuarioAutenticado.getRole().name().equals("ADMIN") && !usuarioAutenticado.getId().equals(id)) {
            throw new BusinessException("Você não tem permissão para alterar o avatar deste usuário. Você só pode alterar seu próprio avatar.");
        }

        // Remove avatar antigo do sistema de arquivos
        if (usuario.getAvatarUrl() != null && !usuario.getAvatarUrl().isEmpty()) {
            imageService.deleteImage(usuario.getAvatarUrl());
        }

        // Faz upload do novo avatar
        String avatarUrl = imageService.uploadAvatar(file);
        usuario.setAvatarUrl(avatarUrl);

        usuario = usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getAvatarUrl(),
                usuario.getCreatedAt()
        );
    }
}




