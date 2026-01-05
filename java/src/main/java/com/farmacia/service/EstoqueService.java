package com.farmacia.service;

import com.farmacia.domain.dto.EstoqueOperacaoResponse;
import com.farmacia.domain.dto.EstoqueResponse;
import com.farmacia.domain.entity.Medicamento;
import com.farmacia.domain.entity.MovimentacaoEstoque;
import com.farmacia.domain.enums.TipoMovimentacao;
import com.farmacia.exception.BusinessException;
import com.farmacia.repository.MedicamentoRepository;
import com.farmacia.repository.MovimentacaoEstoqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class EstoqueService {

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private AlertaService alertaService;

    @Transactional
    public EstoqueOperacaoResponse adicionarEstoque(UUID medicamentoId, Integer quantidade, String motivo) {
        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new BusinessException("Medicamento não encontrado, por favor selecione um medicamento válido."));
        
        if (quantidade == null || quantidade <= 0) {
            throw new BusinessException("A quantidade para entrada de estoque deve ser maior que zero, por favor alterar.");
        }
        
        int quantidadeAnterior = medicamento.getQuantidadeEstoque();
        int novaQuantidade = quantidadeAnterior + quantidade;
        medicamento.setQuantidadeEstoque(novaQuantidade);
        medicamentoRepository.save(medicamento);
        medicamentoRepository.flush(); // CRÍTICO: Garante que o estoque atualizado seja persistido ANTES de verificar alertas

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setMedicamentoId(medicamentoId);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setTipo(TipoMovimentacao.ENTRADA);
        movimentacao.setEstoqueTotal(novaQuantidade);
        movimentacao.setMotivo(motivo != null ? motivo : "Entrada de estoque");
        movimentacaoEstoqueRepository.save(movimentacao);

        // Registra log com data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"medicamentoId\":\"%s\",\"quantidade\":%d,\"motivo\":\"%s\",\"estoqueAnterior\":%d,\"estoqueAtual\":%d,\"data\":\"%s\"}", 
                medicamentoId, quantidade, motivo != null ? motivo : "Entrada de estoque", quantidadeAnterior, novaQuantidade, dataFormatada);
        logService.registrarLog("UPDATE", "ESTOQUE", medicamentoId, 
                String.format("Entrada de estoque: %d unidade(s) adicionada(s) ao medicamento '%s'", quantidade, medicamento.getNome()), 
                detalhes);

        // VALIDAÇÃO CRÍTICA: Se o estoque agora está acima ou igual ao limite (>= 10), marca os alertas como lidos IMEDIATAMENTE
        // Isso garante que alertas de estoque baixo desaparecem do painel de controle quando o estoque é aumentado
        final int LIMITE_ESTOQUE_BAIXO = AlertaService.getLimiteEstoqueBaixo();
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("📦 EstoqueService.adicionarEstoque() - INÍCIO");
        System.out.println("📦 Medicamento ID: " + medicamentoId);
        System.out.println("📦 Medicamento Nome: " + medicamento.getNome());
        System.out.println("📦 Quantidade Anterior: " + quantidadeAnterior);
        System.out.println("📦 Quantidade Adicionada: " + quantidade);
        System.out.println("📦 Nova Quantidade: " + novaQuantidade);
        System.out.println("📦 LIMITE_ESTOQUE_BAIXO: " + LIMITE_ESTOQUE_BAIXO);
        System.out.println("📦 Condição (novaQuantidade >= LIMITE): " + (novaQuantidade >= LIMITE_ESTOQUE_BAIXO));
        
        if (novaQuantidade >= LIMITE_ESTOQUE_BAIXO) {
            System.out.println("📦 ✅ Estoque >= " + LIMITE_ESTOQUE_BAIXO + ", VAMOS MARCAR ALERTAS COMO LIDOS!");
            alertaService.marcarAlertasEstoqueBaixoComoLidos(medicamentoId);
            System.out.println("📦 ✅ Alertas marcados como lidos. NÃO chamando verificarEstoqueBaixo() porque estoque não está mais baixo.");
        } else {
            System.out.println("📦 ⚠️ Estoque ainda está baixo (" + novaQuantidade + " < " + LIMITE_ESTOQUE_BAIXO + "), chamando verificarEstoqueBaixo()");
            alertaService.verificarEstoqueBaixo();
        }
        System.out.println("📦 EstoqueService.adicionarEstoque() - FIM");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");

        return new EstoqueOperacaoResponse(
                String.format("Estoque aumentado com sucesso. %d unidade(s) adicionada(s) ao medicamento '%s'. Estoque anterior: %d, Estoque atual: %d", 
                    quantidade, medicamento.getNome(), quantidadeAnterior, novaQuantidade),
                medicamentoId,
                medicamento.getNome(),
                quantidade,
                novaQuantidade,
                "ENTRADA"
        );
    }

    @Transactional
    public EstoqueOperacaoResponse removerEstoque(UUID medicamentoId, Integer quantidade, String motivo) {
        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new BusinessException("Medicamento não encontrado, por favor selecione um medicamento válido."));
        
        if (quantidade == null || quantidade <= 0) {
            throw new BusinessException("A quantidade para saída de estoque deve ser maior que zero, por favor alterar.");
        }

        // Verifica se há estoque suficiente
        int quantidadeAnterior = medicamento.getQuantidadeEstoque();
        if (quantidadeAnterior < quantidade) {
            throw new BusinessException(
                String.format("Estoque insuficiente para o medicamento '%s'. Disponível: %d unidade(s), solicitado: %d unidade(s).", 
                    medicamento.getNome(), quantidadeAnterior, quantidade)
            );
        }
        
        // Atualiza estoque
        int novaQuantidade = quantidadeAnterior - quantidade;
        medicamento.setQuantidadeEstoque(novaQuantidade);
        medicamentoRepository.save(medicamento);
        medicamentoRepository.flush(); // CRÍTICO: Garante que o estoque atualizado seja persistido ANTES de verificar alertas

        // Registra movimentação
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setMedicamentoId(medicamentoId);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setTipo(TipoMovimentacao.SAIDA);
        movimentacao.setEstoqueTotal(novaQuantidade);
        movimentacao.setMotivo(motivo != null ? motivo : "Saída de estoque");
        movimentacaoEstoqueRepository.save(movimentacao);

        // Registra log com data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"medicamentoId\":\"%s\",\"quantidade\":%d,\"motivo\":\"%s\",\"estoqueAnterior\":%d,\"estoqueAtual\":%d,\"data\":\"%s\"}", 
                medicamentoId, quantidade, motivo != null ? motivo : "Saída de estoque", quantidadeAnterior, novaQuantidade, dataFormatada);
        logService.registrarLog("UPDATE", "ESTOQUE", medicamentoId, 
                String.format("Saída de estoque: %d unidade(s) removida(s) do medicamento '%s'", quantidade, medicamento.getNome()), 
                detalhes);

        // Após saída, verifica se precisa gerar alertas de estoque baixo
        // (se o estoque ficou abaixo de 10, cria novos alertas se necessário)
        alertaService.verificarEstoqueBaixo();

        return new EstoqueOperacaoResponse(
                String.format("Estoque diminuído com sucesso. %d unidade(s) removida(s) do medicamento '%s'. Estoque anterior: %d, Estoque atual: %d", 
                    quantidade, medicamento.getNome(), quantidadeAnterior, novaQuantidade),
                medicamentoId,
                medicamento.getNome(),
                quantidade,
                novaQuantidade,
                "SAIDA"
        );
    }

    @Transactional(readOnly = true)
    public EstoqueResponse getEstoqueByMedicamento(UUID medicamentoId) {
        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new BusinessException("Medicamento não encontrado, por favor selecione um medicamento válido."));
        
        return new EstoqueResponse(
                medicamento.getId(),
                medicamento.getNome(),
                medicamento.getQuantidadeEstoque()
        );
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoEstoque> getMovimentacoesByMedicamento(UUID medicamentoId) {
        return movimentacaoEstoqueRepository.findByMedicamentoId(medicamentoId);
    }
}



