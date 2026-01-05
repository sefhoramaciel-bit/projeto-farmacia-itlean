package com.farmacia.service;

import com.farmacia.domain.dto.MedicamentoRequest;
import com.farmacia.domain.dto.MedicamentoResponse;
import com.farmacia.domain.dto.CategoriaResponse;
import com.farmacia.domain.entity.Medicamento;
import com.farmacia.domain.entity.Categoria;
import com.farmacia.exception.BusinessException;
import com.farmacia.repository.MedicamentoRepository;
import com.farmacia.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicamentoService {

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private LogService logService;

    @Autowired
    private AlertaService alertaService;

    @Autowired
    private com.farmacia.repository.ItemVendaRepository itemVendaRepository;

    @Transactional
    public MedicamentoResponse create(MedicamentoRequest request) {
        return create(request, null);
    }

    @Transactional
    public MedicamentoResponse create(MedicamentoRequest request, List<MultipartFile> files) {
        String nomeTrimmed = request.getNome() != null ? request.getNome().trim() : "";
        if (nomeTrimmed.isEmpty()) {
            throw new BusinessException("O campo Nome é obrigatório, por favor preencha.");
        }
        if (medicamentoRepository.findByNome(nomeTrimmed).isPresent()) {
            throw new BusinessException("O nome do medicamento já existe, por favor alterar.");
        }
        
        // Valida preço
        if (request.getPreco() == null || request.getPreco().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O preço é obrigatório e deve ser maior que zero, por favor alterar.");
        }
        
        if (request.getQuantidadeEstoque() == null || request.getQuantidadeEstoque() < 0) {
            throw new BusinessException("A quantidade em estoque é obrigatória e não pode ser negativa, por favor alterar.");
        }
        
        // Valida data de validade (obrigatória e deve ser futura)
        if (request.getValidade() == null) {
            throw new BusinessException("A data de validade é obrigatória, por favor preencha.");
        }
        if (!request.getValidade().isAfter(LocalDate.now())) {
            throw new BusinessException("A data de validade deve ser futura, por favor alterar.");
        }

        Medicamento medicamento = new Medicamento();
        medicamento.setNome(request.getNome());
        medicamento.setDescricao(request.getDescricao());
        medicamento.setPreco(request.getPreco());
        medicamento.setQuantidadeEstoque(request.getQuantidadeEstoque());
        medicamento.setValidade(request.getValidade());
        medicamento.setAtivo(request.getAtivo() != null ? request.getAtivo() : true);

        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new BusinessException("Categoria não encontrada, por favor selecione uma categoria válida."));
            medicamento.setCategoria(categoria);
        }

        // Processa imagens se fornecidas
        if (files != null && !files.isEmpty()) {
            List<String> imageUrls = imageService.uploadMedicamentoImages(files);
            medicamento.setImagens(imageUrls);
        } else {
            throw new BusinessException("É necessário incluir pelo menos 1 imagem do medicamento.");
        }

        medicamento = medicamentoRepository.save(medicamento);
        medicamentoRepository.flush(); // Garante que o medicamento seja persistido antes de gerar alertas
        
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("💊 MedicamentoService.create() - PROCESSANDO ALERTAS");
        System.out.println("💊 Medicamento ID: " + medicamento.getId());
        System.out.println("💊 Medicamento Nome: " + medicamento.getNome());
        System.out.println("💊 Ativo: " + medicamento.getAtivo());
        System.out.println("💊 Validade: " + medicamento.getValidade());
        System.out.println("💊 Quantidade Estoque: " + medicamento.getQuantidadeEstoque());
        
        // Atualiza alertas após criar um novo medicamento
        System.out.println("💊 MedicamentoService: Medicamento criado, atualizando alertas...");
        System.out.println("💊 MedicamentoService: Chamando gerarAlertasManual()...");
        alertaService.gerarAlertasManual();
        System.out.println("💊 MedicamentoService: Alertas atualizados");
        System.out.println("💊 MedicamentoService.create() - ALERTAS PROCESSADOS");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        
        return toResponse(medicamento);
    }

    @Transactional(readOnly = true)
    public List<MedicamentoResponse> findAll() {
        return medicamentoRepository.findAll().stream()
                .sorted((m1, m2) -> m1.getNome().compareToIgnoreCase(m2.getNome()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicamentoResponse> findActive() {
        return medicamentoRepository.findByAtivoTrue().stream()
                .sorted((m1, m2) -> m1.getNome().compareToIgnoreCase(m2.getNome()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicamentoResponse findById(UUID id) {
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Medicamento não encontrado, por favor selecione um medicamento válido."));
        return toResponse(medicamento);
    }

    @Transactional
    public MedicamentoResponse updateStatus(UUID id, Boolean ativo) {
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Medicamento não encontrado, por favor selecione um medicamento válido."));
        
        String statusAnterior = medicamento.getAtivo() ? "ativo" : "inativo";
        String statusNovo = ativo ? "ativo" : "inativo";
        
        medicamento.setAtivo(ativo);
        medicamento = medicamentoRepository.save(medicamento);
        medicamentoRepository.flush(); // Garante que a mudança de status seja persistida antes de processar alertas
        
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("💊 MedicamentoService.updateStatus() - PROCESSANDO ALERTAS");
        System.out.println("💊 Medicamento ID: " + id);
        System.out.println("💊 Medicamento Nome: " + medicamento.getNome());
        System.out.println("💊 Status Anterior: " + statusAnterior);
        System.out.println("💊 Status Novo: " + statusNovo);
        System.out.println("💊 Ativo: " + ativo);
        System.out.println("💊 Validade: " + medicamento.getValidade());
        System.out.println("💊 Quantidade Estoque: " + medicamento.getQuantidadeEstoque());
        
        // Se o medicamento foi inativado, marca todos os seus alertas como lidos
        // Isso garante que alertas de medicamentos inativos não apareçam no painel de controle
        if (!ativo) {
            System.out.println("💊 MedicamentoService: Medicamento INATIVADO, marcando todos os alertas como lidos");
            alertaService.marcarTodosAlertasComoLidos(id);
            System.out.println("💊 MedicamentoService: Alertas marcados como lidos");
        } else {
            // Se o medicamento foi reativado, remove todos os alertas antigos (lidos ou não) 
            // e regenera os alertas para permitir que novos alertas sejam criados se necessário
            System.out.println("💊 MedicamentoService: Medicamento REATIVADO, removendo alertas antigos e regenerando...");
            System.out.println("💊 MedicamentoService: Chamando removerTodosAlertasDoMedicamento(" + id + ")...");
            alertaService.removerTodosAlertasDoMedicamento(id);
            System.out.println("💊 MedicamentoService: Alertas antigos removidos, chamando gerarAlertasManual()...");
            alertaService.gerarAlertasManual();
            System.out.println("💊 MedicamentoService: Alertas regenerados");
        }
        System.out.println("💊 MedicamentoService.updateStatus() - ALERTAS PROCESSADOS");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        
        // Registra log com data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"statusAnterior\":\"%s\",\"statusNovo\":\"%s\",\"data\":\"%s\"}", statusAnterior, statusNovo, dataFormatada);
        logService.registrarLog("UPDATE", "MEDICAMENTO", medicamento.getId(), 
                String.format("Status do medicamento '%s' alterado de %s para %s", medicamento.getNome(), statusAnterior, statusNovo), 
                detalhes);
        
        return toResponse(medicamento);
    }

    @Transactional
    public MedicamentoResponse update(UUID id, MedicamentoRequest request) {
        return update(id, request, null);
    }

    @Transactional
    public MedicamentoResponse update(UUID id, MedicamentoRequest request, List<MultipartFile> files) {
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Medicamento não encontrado, por favor selecione um medicamento válido."));

        // Valida nome único apenas se o nome estiver sendo alterado
        if (!medicamento.getNome().equalsIgnoreCase(request.getNome().trim())) {
            // Se o nome está mudando, verifica se já existe outro medicamento com esse nome
            medicamentoRepository.findByNome(request.getNome().trim())
                    .ifPresent(medicamentoExistente -> {
                        // Só lança erro se for um medicamento diferente (ID diferente)
                        if (!medicamentoExistente.getId().equals(id)) {
                            throw new BusinessException("O nome do medicamento já existe, por favor alterar.");
                        }
                    });
        }
        
        // Valida preço
        if (request.getPreco() == null || request.getPreco().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O preço é obrigatório e deve ser maior que zero, por favor alterar.");
        }
        
        if (request.getQuantidadeEstoque() == null || request.getQuantidadeEstoque() < 0) {
            throw new BusinessException("A quantidade em estoque é obrigatória e não pode ser negativa, por favor alterar.");
        }
        
        // Valida data de validade (obrigatória e deve ser futura)
        if (request.getValidade() == null) {
            throw new BusinessException("A data de validade é obrigatória, por favor preencha.");
        }
        if (!request.getValidade().isAfter(LocalDate.now())) {
            throw new BusinessException("A data de validade deve ser futura, por favor alterar.");
        }

        medicamento.setNome(request.getNome());
        medicamento.setDescricao(request.getDescricao());
        medicamento.setPreco(request.getPreco());
        medicamento.setQuantidadeEstoque(request.getQuantidadeEstoque());
        medicamento.setValidade(request.getValidade());
        medicamento.setAtivo(request.getAtivo() != null ? request.getAtivo() : true);

        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new BusinessException("Categoria não encontrada, por favor selecione uma categoria válida."));
            medicamento.setCategoria(categoria);
        }

        // Processa imagens se fornecidas
        if (files != null && !files.isEmpty()) {
            // Remove imagens antigas do sistema de arquivos
            if (medicamento.getImagens() != null && !medicamento.getImagens().isEmpty()) {
                imageService.deleteImages(medicamento.getImagens());
            }
            // Faz upload das novas imagens
            List<String> imageUrls = imageService.uploadMedicamentoImages(files);
            medicamento.setImagens(imageUrls);
        }
        // Se não há novas imagens e não há imagens existentes, valida que precisa de pelo menos 1
        else if (medicamento.getImagens() == null || medicamento.getImagens().isEmpty()) {
            throw new BusinessException("É necessário incluir pelo menos 1 imagem do medicamento.");
        }

        medicamento = medicamentoRepository.save(medicamento);
        medicamentoRepository.flush(); // Garante que as mudanças sejam persistidas antes de gerar alertas
        
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("💊 MedicamentoService.update() - PROCESSANDO ALERTAS");
        System.out.println("💊 Medicamento ID: " + id);
        System.out.println("💊 Medicamento Nome: " + medicamento.getNome());
        System.out.println("💊 Ativo: " + medicamento.getAtivo());
        System.out.println("💊 Validade: " + medicamento.getValidade());
        System.out.println("💊 Quantidade Estoque: " + medicamento.getQuantidadeEstoque());
        
        // Atualiza alertas após atualizar um medicamento
        System.out.println("💊 MedicamentoService: Medicamento atualizado, atualizando alertas...");
        System.out.println("💊 MedicamentoService: Chamando gerarAlertasManual()...");
        alertaService.gerarAlertasManual();
        System.out.println("💊 MedicamentoService: Alertas atualizados");
        System.out.println("💊 MedicamentoService.update() - ALERTAS PROCESSADOS");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        
        // Registra log com data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"nome\":\"%s\",\"preco\":%.2f,\"quantidadeEstoque\":%d,\"ativo\":%s,\"data\":\"%s\"}", 
                medicamento.getNome(), medicamento.getPreco(), medicamento.getQuantidadeEstoque(), medicamento.getAtivo(), dataFormatada);
        logService.registrarLog("UPDATE", "MEDICAMENTO", medicamento.getId(), 
                "Medicamento atualizado: " + medicamento.getNome(), detalhes);
        
        return toResponse(medicamento);
    }

    @Transactional
    public String delete(UUID id) {
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Medicamento não encontrado, por favor selecione um medicamento válido."));
        
        String nomeMedicamento = medicamento.getNome();
        UUID medicamentoId = medicamento.getId();
        
        // Verifica se o medicamento já foi vendido (soft delete recomendado)
        boolean foiVendido = itemVendaRepository.existsByMedicamentoId(medicamentoId);
        if (foiVendido) {
            long quantidadeVendas = itemVendaRepository.countByMedicamentoId(medicamentoId);
            throw new BusinessException(
                String.format("Não é possível excluir o medicamento '%s' pois ele já foi vendido (%d venda(s)). Recomenda-se inativar o medicamento ao invés de excluí-lo.", 
                    nomeMedicamento, quantidadeVendas)
            );
        }
        
        // CRÍTICO: Marca todos os alertas do medicamento como lidos ANTES de excluir
        // Isso garante que os alertas não apareçam mais no painel de controle
        System.out.println("💊 MedicamentoService.delete() - INÍCIO");
        System.out.println("💊 Medicamento ID: " + medicamentoId);
        System.out.println("💊 Medicamento Nome: " + nomeMedicamento);
        System.out.println("💊 MedicamentoService.delete() - Marcando TODOS os alertas como lidos...");
        alertaService.marcarTodosAlertasComoLidos(medicamentoId);
        System.out.println("💊 MedicamentoService.delete() - Alertas marcados como lidos com sucesso");
        System.out.println("💊 MedicamentoService.delete() - Flush para garantir persistência dos alertas...");
        medicamentoRepository.flush(); // Garante que todas as mudanças anteriores sejam persistidas
        
        // Remove imagens do sistema de arquivos
        if (medicamento.getImagens() != null && !medicamento.getImagens().isEmpty()) {
            imageService.deleteImages(medicamento.getImagens());
        }
        
        // Agora sim, exclui o medicamento
        System.out.println("💊 MedicamentoService.delete() - Excluindo medicamento...");
        medicamentoRepository.deleteById(medicamentoId);
        medicamentoRepository.flush(); // Garante que a exclusão seja persistida
        System.out.println("💊 MedicamentoService.delete() - Medicamento excluído com sucesso");
        
        // Registra log com data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"nome\":\"%s\",\"data\":\"%s\"}", nomeMedicamento, dataFormatada);
        logService.registrarLog("DELETE", "MEDICAMENTO", medicamentoId, 
                "Medicamento deletado: " + nomeMedicamento, detalhes);
        
        System.out.println("💊 MedicamentoService.delete() - FIM");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        
        return String.format("Medicamento '%s' deletado com sucesso.", nomeMedicamento);
    }

    @Transactional
    public MedicamentoResponse uploadImagens(UUID id, List<MultipartFile> files) {
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Medicamento não encontrado, por favor selecione um medicamento válido."));

        // Remove imagens antigas do sistema de arquivos
        if (medicamento.getImagens() != null && !medicamento.getImagens().isEmpty()) {
            imageService.deleteImages(medicamento.getImagens());
        }

        // Faz upload das novas imagens
        List<String> imageUrls = imageService.uploadMedicamentoImages(files);
        medicamento.setImagens(imageUrls);

        medicamento = medicamentoRepository.save(medicamento);
        return toResponse(medicamento);
    }

    @Transactional
    public MedicamentoResponse removerImagens(UUID id) {
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Medicamento não encontrado, por favor selecione um medicamento válido."));

        // Remove imagens do sistema de arquivos
        if (medicamento.getImagens() != null && !medicamento.getImagens().isEmpty()) {
            imageService.deleteImages(medicamento.getImagens());
        }

        medicamento.setImagens(new ArrayList<>());
        medicamento = medicamentoRepository.save(medicamento);
        return toResponse(medicamento);
    }

    private MedicamentoResponse toResponse(Medicamento medicamento) {
        CategoriaResponse categoriaResponse = null;
        if (medicamento.getCategoria() != null) {
            categoriaResponse = new CategoriaResponse(
                    medicamento.getCategoria().getId(),
                    medicamento.getCategoria().getNome(),
                    medicamento.getCategoria().getDescricao(),
                    medicamento.getCategoria().getCreatedAt()
            );
        }

        return new MedicamentoResponse(
                medicamento.getId(),
                medicamento.getNome(),
                medicamento.getDescricao(),
                medicamento.getPreco(),
                medicamento.getQuantidadeEstoque(),
                medicamento.getValidade(),
                medicamento.getAtivo(),
                categoriaResponse,
                medicamento.getImagens() != null ? medicamento.getImagens() : new ArrayList<>(),
                medicamento.getCreatedAt()
        );
    }
}




