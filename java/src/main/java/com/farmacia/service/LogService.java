package com.farmacia.service;

import com.farmacia.domain.entity.Log;
import com.farmacia.domain.entity.Usuario;
import com.farmacia.repository.LogRepository;
import com.farmacia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class LogService {

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public void registrarLog(String tipoOperacao, String tipoEntidade, UUID entidadeId, String descricao, String detalhes) {
        try {
            // Obtém usuário autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                // Para operações que não têm usuário autenticado (como login antes da autenticação)
                // o log deve ser registrado via registrarLogLogin
                return;
            }

            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElse(null);

            if (usuario == null) {
                return; // Se não encontrar usuário, não registra log
            }

            // Garante que sempre há data nos detalhes
            LocalDateTime dataHora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String dataFormatada = dataHora.format(formatter);
            
            String detalhesComData;
            if (detalhes == null || detalhes.trim().isEmpty()) {
                // Se detalhes for null ou vazio, cria um JSON apenas com a data
                detalhesComData = String.format("{\"data\":\"%s\"}", dataFormatada);
            } else {
                // Verifica se já tem campo "data" nos detalhes
                String detalhesTrimmed = detalhes.trim();
                if (detalhesTrimmed.contains("\"data\"")) {
                    // Já tem data, usa os detalhes como estão
                    detalhesComData = detalhesTrimmed;
                } else {
                    // Não tem data, adiciona
                    // Remove o último } se existir e adiciona a data antes
                    if (detalhesTrimmed.endsWith("}")) {
                        String detalhesSemChave = detalhesTrimmed.substring(0, detalhesTrimmed.length() - 1);
                        // Adiciona vírgula se não estiver vazio e não terminar com vírgula
                        if (!detalhesSemChave.isEmpty() && !detalhesSemChave.endsWith(",") && !detalhesSemChave.endsWith("{")) {
                            detalhesSemChave += ",";
                        }
                        detalhesComData = detalhesSemChave + "\"data\":\"" + dataFormatada + "\"}";
                    } else {
                        // Se não for JSON válido, cria um novo com a data
                        detalhesComData = String.format("{\"detalhes\":%s,\"data\":\"%s\"}", detalhes, dataFormatada);
                    }
                }
            }

            Log log = new Log();
            log.setTipoOperacao(tipoOperacao);
            log.setTipoEntidade(tipoEntidade);
            log.setEntidadeId(entidadeId);
            log.setDescricao(descricao);
            log.setDetalhes(detalhesComData);
            log.setUsuarioId(usuario.getId());
            log.setUsuarioNome(usuario.getNome());
            log.setUsuarioEmail(usuario.getEmail());

            logRepository.save(log);
        } catch (Exception e) {
            // Não lança exceção para não quebrar o fluxo principal
            // Apenas loga o erro (em produção, usar logger apropriado)
            System.err.println("Erro ao registrar log: " + e.getMessage());
        }
    }

    @Transactional
    public void registrarLogLogin(UUID usuarioId, String usuarioNome, String usuarioEmail, String descricao) {
        // Adiciona data nos detalhes do log de login
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"data\":\"%s\"}", dataFormatada);
        
        Log log = new Log();
        log.setTipoOperacao("LOGIN");
        log.setTipoEntidade("LOGIN");
        log.setEntidadeId(usuarioId);
        log.setDescricao(descricao);
        log.setDetalhes(detalhes);
        log.setUsuarioId(usuarioId);
        log.setUsuarioNome(usuarioNome);
        log.setUsuarioEmail(usuarioEmail);
        logRepository.save(log);
    }
}

