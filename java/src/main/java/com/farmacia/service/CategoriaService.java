package com.farmacia.service;

import com.farmacia.domain.dto.CategoriaRequest;
import com.farmacia.domain.dto.CategoriaResponse;
import com.farmacia.domain.entity.Categoria;
import com.farmacia.exception.BusinessException;
import com.farmacia.repository.CategoriaRepository;
import com.farmacia.repository.MedicamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private LogService logService;

    @Transactional
    public CategoriaResponse create(CategoriaRequest request) {
        String nomeTrimmed = request.getNome() != null ? request.getNome().trim() : "";
        if (nomeTrimmed.isEmpty()) {
            throw new BusinessException("O campo Nome é obrigatório, por favor preencha.");
        }
        
        if (categoriaRepository.existsByNome(nomeTrimmed)) {
            throw new BusinessException("O nome da categoria já existe, por favor alterar.");
        }

        Categoria categoria = new Categoria();
        categoria.setNome(nomeTrimmed);
        categoria.setDescricao(request.getDescricao());

        categoria = categoriaRepository.save(categoria);
        
        // Registra log com data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"nome\":\"%s\",\"descricao\":\"%s\",\"data\":\"%s\"}", 
                categoria.getNome(), categoria.getDescricao() != null ? categoria.getDescricao() : "", dataFormatada);
        logService.registrarLog("CREATE", "CATEGORIA", categoria.getId(), 
                "Categoria criada: " + categoria.getNome(), detalhes);
        
        return toResponse(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> findAll() {
        return categoriaRepository.findAll().stream()
                .sorted((c1, c2) -> c1.getNome().compareToIgnoreCase(c2.getNome()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoriaResponse findById(UUID id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada, por favor selecione uma categoria válida."));
        return toResponse(categoria);
    }

    @Transactional
    public CategoriaResponse update(UUID id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada, por favor selecione uma categoria válida."));

        String nomeTrimmed = request.getNome() != null ? request.getNome().trim() : "";
        if (nomeTrimmed.isEmpty()) {
            throw new BusinessException("O campo Nome é obrigatório, por favor preencha.");
        }

        if (!categoria.getNome().equalsIgnoreCase(nomeTrimmed) && categoriaRepository.existsByNome(nomeTrimmed)) {
            throw new BusinessException("O nome da categoria já existe, por favor alterar.");
        }

        categoria.setNome(nomeTrimmed);
        categoria.setDescricao(request.getDescricao());

        categoria = categoriaRepository.save(categoria);
        
        // Registra log com data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"nome\":\"%s\",\"descricao\":\"%s\",\"data\":\"%s\"}", 
                categoria.getNome(), categoria.getDescricao() != null ? categoria.getDescricao() : "", dataFormatada);
        logService.registrarLog("UPDATE", "CATEGORIA", categoria.getId(), 
                "Categoria atualizada: " + categoria.getNome(), detalhes);
        
        return toResponse(categoria);
    }

    @Transactional
    public String delete(UUID id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada, por favor selecione uma categoria válida."));
        
        String nomeCategoria = categoria.getNome();
        
        // Verifica se há medicamentos vinculados a esta categoria
        if (medicamentoRepository.existsByCategoriaId(id)) {
            long quantidadeMedicamentos = medicamentoRepository.countByCategoriaId(id);
            throw new BusinessException(
                String.format("Não é possível excluir a categoria '%s' pois ela está vinculada a %d medicamento(s). " +
                    "Remova ou altere a categoria dos medicamentos antes de excluir.", 
                    nomeCategoria, 
                    quantidadeMedicamentos)
            );
        }
        
        categoriaRepository.delete(categoria);
        
        // Registra log com data
        LocalDateTime dataHora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = dataHora.format(formatter);
        String detalhes = String.format("{\"nome\":\"%s\",\"data\":\"%s\"}", nomeCategoria, dataFormatada);
        logService.registrarLog("DELETE", "CATEGORIA", id, 
                "Categoria deletada: " + nomeCategoria, detalhes);
        
        return String.format("Categoria '%s' deletada com sucesso.", nomeCategoria);
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getCreatedAt()
        );
    }
}




