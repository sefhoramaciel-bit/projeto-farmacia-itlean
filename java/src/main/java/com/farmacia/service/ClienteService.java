package com.farmacia.service;

import com.farmacia.domain.dto.ClienteRequest;
import com.farmacia.domain.dto.ClienteResponse;
import com.farmacia.domain.entity.Cliente;
import com.farmacia.exception.BusinessException;
import com.farmacia.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private LogService logService;

    @Transactional
    public ClienteResponse create(ClienteRequest request) {
        // Valida nome obrigatório
        String nomeTrimmed = request.getNome() != null ? request.getNome().trim() : "";
        if (nomeTrimmed.isEmpty()) {
            throw new BusinessException("O campo Nome é obrigatório, por favor preencha.");
        }
        
        // Valida CPF obrigatório e único
        String cpfTrimmed = request.getCpf() != null ? request.getCpf().trim() : "";
        if (cpfTrimmed.isEmpty()) {
            throw new BusinessException("O campo CPF é obrigatório, por favor preencha.");
        }
        if (clienteRepository.existsByCpf(cpfTrimmed)) {
            throw new BusinessException("O CPF informado já existe, por favor alterar.");
        }
        
        // Valida data de nascimento obrigatória
        if (request.getDataNascimento() == null) {
            throw new BusinessException("O campo Data de Nascimento é obrigatório, por favor preencha.");
        }
        
        // Valida email obrigatório e válido
        String emailTrimmed = request.getEmail() != null ? request.getEmail().trim() : "";
        if (emailTrimmed.isEmpty()) {
            throw new BusinessException("O campo E-mail é obrigatório, por favor preencha.");
        }
        // Valida formato de email básico
        if (!emailTrimmed.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BusinessException("O e-mail informado é inválido, por favor alterar.");
        }
        if (clienteRepository.existsByEmail(emailTrimmed)) {
            throw new BusinessException("O email informado já existe, por favor alterar.");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(nomeTrimmed);
        cliente.setCpf(cpfTrimmed);
        cliente.setTelefone(request.getTelefone());
        cliente.setEmail(emailTrimmed);
        cliente.setEndereco(request.getEndereco());
        cliente.setDataNascimento(request.getDataNascimento());

        cliente = clienteRepository.save(cliente);
        return toResponse(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> findAll() {
        return clienteRepository.findAll().stream()
                .sorted((c1, c2) -> c1.getNome().compareToIgnoreCase(c2.getNome()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClienteResponse findById(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado, por favor selecione um cliente válido."));
        return toResponse(cliente);
    }

    @Transactional
    public ClienteResponse update(UUID id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado, por favor selecione um cliente válido."));

        // Valida nome obrigatório
        String nomeTrimmed = request.getNome() != null ? request.getNome().trim() : "";
        if (nomeTrimmed.isEmpty()) {
            throw new BusinessException("O campo Nome é obrigatório, por favor preencha.");
        }
        
        // Valida CPF obrigatório e único
        String cpfTrimmed = request.getCpf() != null ? request.getCpf().trim() : "";
        if (cpfTrimmed.isEmpty()) {
            throw new BusinessException("O campo CPF é obrigatório, por favor preencha.");
        }
        if (!cliente.getCpf().equals(cpfTrimmed) && clienteRepository.existsByCpf(cpfTrimmed)) {
            throw new BusinessException("O CPF informado já existe, por favor alterar.");
        }
        
        // Valida data de nascimento obrigatória
        if (request.getDataNascimento() == null) {
            throw new BusinessException("O campo Data de Nascimento é obrigatório, por favor preencha.");
        }
        
        // Valida email obrigatório e válido
        String emailTrimmed = request.getEmail() != null ? request.getEmail().trim() : "";
        if (emailTrimmed.isEmpty()) {
            throw new BusinessException("O campo E-mail é obrigatório, por favor preencha.");
        }
        // Valida formato de email básico
        if (!emailTrimmed.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BusinessException("O e-mail informado é inválido, por favor alterar.");
        }
        if (!cliente.getEmail().equalsIgnoreCase(emailTrimmed) && clienteRepository.existsByEmail(emailTrimmed)) {
            throw new BusinessException("O email informado já existe, por favor alterar.");
        }

        cliente.setNome(nomeTrimmed);
        cliente.setCpf(cpfTrimmed);
        cliente.setTelefone(request.getTelefone());
        cliente.setEmail(emailTrimmed);
        cliente.setEndereco(request.getEndereco());
        cliente.setDataNascimento(request.getDataNascimento());

        cliente = clienteRepository.save(cliente);
        return toResponse(cliente);
    }

    @Transactional
    public String delete(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado, por favor selecione um cliente válido."));
        
        String nomeCliente = cliente.getNome();
        clienteRepository.deleteById(id);
        
        // Registra log com data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"nome\":\"%s\",\"data\":\"%s\"}", nomeCliente, dataFormatada);
        logService.registrarLog("DELETE", "CLIENTE", id, 
                "Cliente deletado: " + nomeCliente, detalhes);
        
        return String.format("Cliente '%s' deletado com sucesso.", nomeCliente);
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getEndereco(),
                cliente.getDataNascimento(),
                cliente.getCreatedAt()
        );
    }
}



