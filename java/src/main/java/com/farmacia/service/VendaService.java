package com.farmacia.service;

import com.farmacia.domain.dto.*;
import com.farmacia.domain.entity.*;
import com.farmacia.domain.enums.StatusVenda;
import com.farmacia.domain.enums.TipoMovimentacao;
import com.farmacia.exception.BusinessException;
import com.farmacia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private AlertaService alertaService;

    @Transactional
    public VendaResponse create(VendaRequest request) {
        // Valida cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new BusinessException("Cliente não encontrado, por favor selecione um cliente válido."));

        // Valida idade do cliente (deve ter mais de 18 anos)
        if (cliente.getDataNascimento() == null) {
            throw new BusinessException("Cliente não possui data de nascimento cadastrada. Não é possível realizar a venda.");
        }

        LocalDate hoje = LocalDate.now();
        Period periodo = Period.between(cliente.getDataNascimento(), hoje);
        int idade = periodo.getYears();

        if (idade < 18) {
            throw new BusinessException(
                String.format("Cliente deve ter mais de 18 anos para realizar compras. Idade atual: %d anos.", idade)
            );
        }

        // Obtém usuário autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado, por favor faça login novamente."));

        // Valida itens e verifica estoque
        BigDecimal valorTotal = BigDecimal.ZERO;
        for (ItemVendaRequest itemRequest : request.getItens()) {
            Medicamento medicamento = medicamentoRepository.findById(itemRequest.getMedicamentoId())
                    .orElseThrow(() -> new BusinessException("Medicamento não encontrado, por favor selecione um medicamento válido."));

            // Verifica se está ativo
            if (!medicamento.getAtivo()) {
                throw new BusinessException("O medicamento '" + medicamento.getNome() + "' está inativo e não pode ser vendido.");
            }

            // Verifica validade
            if (medicamento.getValidade() != null && medicamento.getValidade().isBefore(LocalDate.now())) {
                throw new BusinessException("O medicamento '" + medicamento.getNome() + "' está vencido e não pode ser vendido.");
            }

            // Verifica estoque
            if (medicamento.getQuantidadeEstoque() < itemRequest.getQuantidade()) {
                throw new BusinessException("Estoque insuficiente para o medicamento '" + medicamento.getNome() + "'. Disponível: " + medicamento.getQuantidadeEstoque() + " unidade(s).");
            }

            BigDecimal subtotal = medicamento.getPreco().multiply(BigDecimal.valueOf(itemRequest.getQuantidade()));
            valorTotal = valorTotal.add(subtotal);
        }

        // Cria venda
        Venda venda = new Venda();
        venda.setClienteId(cliente.getId());
        venda.setUsuarioId(usuario.getId());
        venda.setStatus(StatusVenda.CONCLUIDA);
        venda.setValorTotal(valorTotal);
        // Registra explicitamente a data e hora da venda
        venda.setCreatedAt(LocalDateTime.now());

        // Cria itens e atualiza estoque
        for (ItemVendaRequest itemRequest : request.getItens()) {
            Medicamento medicamento = medicamentoRepository.findById(itemRequest.getMedicamentoId())
                    .orElseThrow(() -> new BusinessException("Medicamento não encontrado, por favor selecione um medicamento válido."));

            ItemVenda item = new ItemVenda();
            item.setVenda(venda);
            item.setMedicamentoId(medicamento.getId());
            item.setMedicamentoNome(medicamento.getNome());
            item.setQuantidade(itemRequest.getQuantidade());
            item.setPrecoUnitario(medicamento.getPreco());
            item.setSubtotal(medicamento.getPreco().multiply(BigDecimal.valueOf(itemRequest.getQuantidade())));

            venda.getItens().add(item);

            // Atualiza estoque
            int novaQuantidade = medicamento.getQuantidadeEstoque() - itemRequest.getQuantidade();
            if (novaQuantidade < 0) {
                throw new BusinessException("Erro ao atualizar estoque: a quantidade solicitada para '" + medicamento.getNome() + "' excede o estoque disponível.");
            }
            medicamento.setQuantidadeEstoque(novaQuantidade);
            medicamentoRepository.save(medicamento);

            // Registra movimentação
            MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
            movimentacao.setMedicamentoId(medicamento.getId());
            movimentacao.setQuantidade(itemRequest.getQuantidade());
            movimentacao.setTipo(TipoMovimentacao.SAIDA);
            movimentacao.setEstoqueTotal(novaQuantidade);
            movimentacao.setMotivo("Venda #" + venda.getId());
            movimentacaoEstoqueRepository.save(movimentacao);
        }

        venda = vendaRepository.save(venda);
        
        // Registra log com detalhes dos itens e data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        
        StringBuilder detalhesJson = new StringBuilder();
        detalhesJson.append("{");
        detalhesJson.append("\"status\":\"").append(venda.getStatus()).append("\",");
        detalhesJson.append("\"valorTotal\":").append(venda.getValorTotal()).append(",");
        detalhesJson.append("\"clienteId\":\"").append(venda.getClienteId()).append("\",");
        detalhesJson.append("\"data\":\"").append(dataFormatada).append("\",");
        detalhesJson.append("\"itens\":[");
        
        for (int i = 0; i < venda.getItens().size(); i++) {
            ItemVenda item = venda.getItens().get(i);
            if (i > 0) detalhesJson.append(",");
            detalhesJson.append("{");
            detalhesJson.append("\"medicamentoNome\":\"").append(item.getMedicamentoNome() != null ? item.getMedicamentoNome().replace("\"", "\\\"") : "").append("\",");
            detalhesJson.append("\"quantidade\":").append(item.getQuantidade()).append(",");
            detalhesJson.append("\"precoUnitario\":").append(item.getPrecoUnitario()).append(",");
            detalhesJson.append("\"subtotal\":").append(item.getSubtotal());
            detalhesJson.append("}");
        }
        
        detalhesJson.append("]}");
        
        logService.registrarLog("CREATE", "VENDA", venda.getId(), 
                String.format("Venda criada: R$ %.2f - %d item(s)", venda.getValorTotal(), venda.getItens().size()), 
                detalhesJson.toString());
        
        // Verifica se precisa gerar alertas de estoque baixo após venda
        alertaService.verificarEstoqueBaixo();
        
        return toResponse(venda);
    }

    @Transactional(readOnly = true)
    public List<VendaResponse> findAll() {
        return vendaRepository.findAll().stream()
                .map(this::toResponse)
                .sorted((v1, v2) -> v1.getClienteNome().compareToIgnoreCase(v2.getClienteNome()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendaResponse findById(UUID id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Venda não encontrada"));
        return toResponse(venda);
    }

    @Transactional(readOnly = true)
    public List<VendaResponse> findByClienteId(UUID clienteId) {
        List<Venda> vendas = vendaRepository.findByClienteId(clienteId);
        return vendas.stream()
                .map(this::toResponse)
                .sorted((v1, v2) -> v1.getClienteNome().compareToIgnoreCase(v2.getClienteNome()))
                .collect(Collectors.toList());
    }

    @Transactional
    public String cancelar(UUID id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Venda não encontrada"));

        if (venda.getStatus() != StatusVenda.CONCLUIDA) {
            throw new BusinessException("Apenas vendas concluídas podem ser canceladas");
        }

        // Estorna estoque
        for (ItemVenda item : venda.getItens()) {
            Medicamento medicamento = medicamentoRepository.findById(item.getMedicamentoId())
                    .orElseThrow(() -> new BusinessException("Medicamento não encontrado"));
            medicamento.setQuantidadeEstoque(medicamento.getQuantidadeEstoque() + item.getQuantidade());
            medicamentoRepository.save(medicamento);

            // Registra movimentação de entrada
            MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
            movimentacao.setMedicamentoId(item.getMedicamentoId());
            movimentacao.setQuantidade(item.getQuantidade());
            movimentacao.setTipo(TipoMovimentacao.ENTRADA);
            movimentacao.setEstoqueTotal(medicamento.getQuantidadeEstoque());
            movimentacao.setMotivo("Cancelamento de venda #" + venda.getId());
            movimentacaoEstoqueRepository.save(movimentacao);
        }

        venda.setStatus(StatusVenda.CANCELADA);
        vendaRepository.save(venda);
        
        // Registra log com data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"status\":\"%s\",\"vendaId\":\"%s\",\"valorTotal\":%.2f,\"clienteId\":\"%s\",\"data\":\"%s\"}", 
                venda.getStatus(), id, venda.getValorTotal(), venda.getClienteId(), dataFormatada);
        logService.registrarLog("UPDATE", "VENDA", venda.getId(), 
                String.format("Venda #%s cancelada. Estoque estornado.", id), detalhes);
        
        // Verifica se precisa gerar alertas de estoque baixo após cancelamento (estoque foi estornado)
        alertaService.verificarEstoqueBaixo();
        
        return String.format("Venda #%s cancelada com sucesso. Estoque estornado para todos os medicamentos.", id);
    }

    /**
     * Cria uma venda com status CANCELADA (sem atualizar estoque)
     * Usado quando o usuário cancela uma venda antes de finalizá-la
     */
    @Transactional
    public VendaResponse createCancelada(VendaRequest request) {
        // Valida cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new BusinessException("Cliente não encontrado, por favor selecione um cliente válido."));

        // Obtém usuário autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado, por favor faça login novamente."));

        // Calcula valor total sem validar estoque ou validade (já que é cancelada)
        BigDecimal valorTotal = BigDecimal.ZERO;
        for (ItemVendaRequest itemRequest : request.getItens()) {
            Medicamento medicamento = medicamentoRepository.findById(itemRequest.getMedicamentoId())
                    .orElseThrow(() -> new BusinessException("Medicamento não encontrado, por favor selecione um medicamento válido."));

            BigDecimal subtotal = medicamento.getPreco().multiply(BigDecimal.valueOf(itemRequest.getQuantidade()));
            valorTotal = valorTotal.add(subtotal);
        }

        // Cria venda com status CANCELADA
        Venda venda = new Venda();
        venda.setClienteId(cliente.getId());
        venda.setUsuarioId(usuario.getId());
        venda.setStatus(StatusVenda.CANCELADA);
        venda.setValorTotal(valorTotal);
        // Registra explicitamente a data e hora da venda
        venda.setCreatedAt(LocalDateTime.now());

        // Cria itens SEM atualizar estoque
        for (ItemVendaRequest itemRequest : request.getItens()) {
            Medicamento medicamento = medicamentoRepository.findById(itemRequest.getMedicamentoId())
                    .orElseThrow(() -> new BusinessException("Medicamento não encontrado, por favor selecione um medicamento válido."));

            ItemVenda item = new ItemVenda();
            item.setVenda(venda);
            item.setMedicamentoId(medicamento.getId());
            item.setMedicamentoNome(medicamento.getNome());
            item.setQuantidade(itemRequest.getQuantidade());
            item.setPrecoUnitario(medicamento.getPreco());
            item.setSubtotal(medicamento.getPreco().multiply(BigDecimal.valueOf(itemRequest.getQuantidade())));

            venda.getItens().add(item);
            // NÃO atualiza estoque pois a venda foi cancelada
        }

        venda = vendaRepository.save(venda);
        
        // Registra log com detalhes dos itens e data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        
        StringBuilder detalhesJson = new StringBuilder();
        detalhesJson.append("{");
        detalhesJson.append("\"status\":\"").append(venda.getStatus()).append("\",");
        detalhesJson.append("\"valorTotal\":").append(venda.getValorTotal()).append(",");
        detalhesJson.append("\"clienteId\":\"").append(venda.getClienteId()).append("\",");
        detalhesJson.append("\"data\":\"").append(dataFormatada).append("\",");
        detalhesJson.append("\"itens\":[");
        
        for (int i = 0; i < venda.getItens().size(); i++) {
            ItemVenda item = venda.getItens().get(i);
            if (i > 0) detalhesJson.append(",");
            detalhesJson.append("{");
            detalhesJson.append("\"medicamentoNome\":\"").append(item.getMedicamentoNome() != null ? item.getMedicamentoNome().replace("\"", "\\\"") : "").append("\",");
            detalhesJson.append("\"quantidade\":").append(item.getQuantidade()).append(",");
            detalhesJson.append("\"precoUnitario\":").append(item.getPrecoUnitario()).append(",");
            detalhesJson.append("\"subtotal\":").append(item.getSubtotal());
            detalhesJson.append("}");
        }
        
        detalhesJson.append("]}");
        
        logService.registrarLog("CREATE", "VENDA", venda.getId(), 
                String.format("Venda cancelada: R$ %.2f - %d item(s)", venda.getValorTotal(), venda.getItens().size()), 
                detalhesJson.toString());
        
        return toResponse(venda);
    }

    private VendaResponse toResponse(Venda venda) {
        Cliente cliente = clienteRepository.findById(venda.getClienteId())
                .orElseThrow(() -> new BusinessException("Cliente não encontrado"));
        Usuario usuario = usuarioRepository.findById(venda.getUsuarioId())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        List<ItemVendaResponse> itensResponse = venda.getItens().stream()
                .map(item -> new ItemVendaResponse(
                        item.getId(),
                        item.getMedicamentoId(),
                        item.getMedicamentoNome(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());

        return new VendaResponse(
                venda.getId(),
                venda.getClienteId(),
                cliente.getNome(),
                venda.getUsuarioId(),
                usuario.getNome(),
                venda.getStatus(),
                venda.getValorTotal(),
                itensResponse,
                venda.getCreatedAt()
        );
    }
}

