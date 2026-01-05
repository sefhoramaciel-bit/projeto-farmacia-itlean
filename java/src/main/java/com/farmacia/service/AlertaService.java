package com.farmacia.service;

import com.farmacia.domain.dto.AlertaResponse;
import com.farmacia.domain.entity.Alerta;
import com.farmacia.domain.entity.Medicamento;
import com.farmacia.exception.BusinessException;
import com.farmacia.repository.AlertaRepository;
import com.farmacia.repository.MedicamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AlertaService {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    private static final Integer LIMITE_ESTOQUE_BAIXO = 10;
    private static final Integer DIAS_VALIDADE_PROXIMA = 30;

    public static Integer getLimiteEstoqueBaixo() {
        return LIMITE_ESTOQUE_BAIXO;
    }

    @Scheduled(cron = "0 0 8 * * ?") // Todos os dias às 8h
    @Transactional
    public void gerarAlertas() {
        verificarEstoqueBaixo();
        verificarValidadeProxima();
        verificarMedicamentosVencidos();
    }

    @Transactional
    public void gerarAlertasManual() {
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("🔔 AlertaService.gerarAlertasManual() - INÍCIO");
        System.out.println("🔔 Chamando verificarEstoqueBaixo()...");
        verificarEstoqueBaixo();
        System.out.println("🔔 verificarEstoqueBaixo() concluído");
        System.out.println("🔔 Chamando verificarValidadeProxima()...");
        verificarValidadeProxima();
        System.out.println("🔔 verificarValidadeProxima() concluído");
        System.out.println("🔔 Chamando verificarMedicamentosVencidos()...");
        verificarMedicamentosVencidos();
        System.out.println("🔔 verificarMedicamentosVencidos() concluído");
        System.out.println("🔔 AlertaService.gerarAlertasManual() - FIM");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
    }

    @Transactional
    public void verificarEstoqueBaixo() {
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("🔔 AlertaService.verificarEstoqueBaixo() - INÍCIO");
        System.out.println("🔔 LIMITE_ESTOQUE_BAIXO: " + LIMITE_ESTOQUE_BAIXO);
        
        // Busca todos os medicamentos ativos
        List<Medicamento> todosMedicamentos = medicamentoRepository.findByAtivoTrue();
        System.out.println("🔔 Total de medicamentos ativos: " + todosMedicamentos.size());
        
        // Primeiro, marca como lidos os alertas de medicamentos que agora têm estoque >= LIMITE
        int alertasMarcadosComoLidos = 0;
        for (Medicamento medicamento : todosMedicamentos) {
            if (medicamento.getQuantidadeEstoque() >= LIMITE_ESTOQUE_BAIXO) {
                System.out.println("🔔 Medicamento " + medicamento.getNome() + " tem estoque " + medicamento.getQuantidadeEstoque() + " >= " + LIMITE_ESTOQUE_BAIXO);
                // Se o estoque agora está acima do limite, marca todos os alertas de estoque baixo como lidos
                List<Alerta> alertasNaoLidos = alertaRepository.findByMedicamentoId(medicamento.getId())
                        .stream()
                        .filter(a -> !a.getLido() && "ESTOQUE_BAIXO".equals(a.getTipo()))
                        .collect(Collectors.toList());
                
                System.out.println("🔔   Alertas de estoque baixo não lidos encontrados: " + alertasNaoLidos.size());
                if (!alertasNaoLidos.isEmpty()) {
                    System.out.println("🔔   ✅ Marcando " + alertasNaoLidos.size() + " alerta(s) como lido(s)");
                    for (Alerta alerta : alertasNaoLidos) {
                        System.out.println("🔔     - Marcando Alerta ID: " + alerta.getId() + ", Lido ANTES: " + alerta.getLido());
                        alerta.setLido(true);
                        alertaRepository.save(alerta);
                        alertaRepository.flush(); // Force flush to ensure immediate persistence
                        alertasMarcadosComoLidos++;
                        System.out.println("🔔     - ✅ Alerta ID: " + alerta.getId() + " marcado como LIDO = true e FLUSHED");
                    }
                }
            }
        }
        System.out.println("🔔 Total de alertas marcados como lidos nesta verificação: " + alertasMarcadosComoLidos);
        
        // Depois, cria alertas APENAS para medicamentos com estoque < 10 E que NÃO têm alertas já criados
        // IMPORTANTE: Se um medicamento já teve um alerta (mesmo que lido), não cria novo alerta
        // Isso garante que alertas não reapareçam depois de serem marcados como lidos
        List<Medicamento> medicamentos = todosMedicamentos.stream()
                .filter(m -> m.getQuantidadeEstoque() < LIMITE_ESTOQUE_BAIXO)
                .collect(Collectors.toList());
        System.out.println("🔔 Medicamentos com estoque < " + LIMITE_ESTOQUE_BAIXO + ": " + medicamentos.size());

        for (Medicamento medicamento : medicamentos) {
            System.out.println("🔔 Verificando medicamento: " + medicamento.getNome() + " (Estoque: " + medicamento.getQuantidadeEstoque() + ")");
            // Verifica se já existe alerta NÃO LIDO para este medicamento e tipo
            List<Alerta> todosAlertasMedicamento = alertaRepository.findByMedicamentoId(medicamento.getId());
            boolean existeAlertaNaoLido = todosAlertasMedicamento.stream()
                    .anyMatch(a -> "ESTOQUE_BAIXO".equals(a.getTipo()) && !a.getLido());
            
            System.out.println("🔔   Total de alertas do medicamento (todos os tipos): " + todosAlertasMedicamento.size());
            for (Alerta a : todosAlertasMedicamento) {
                System.out.println("🔔     - Alerta ID: " + a.getId() + ", Tipo: " + a.getTipo() + ", Lido: " + a.getLido());
            }
            System.out.println("🔔   Existe alerta ESTOQUE_BAIXO NÃO LIDO? " + existeAlertaNaoLido);

            if (!existeAlertaNaoLido) {
                // Cria novo alerta se NÃO existe alerta não lido
                // Isso permite que novos alertas sejam criados mesmo se já existiu um alerta lido anteriormente
                System.out.println("🔔   ✅ Criando NOVO alerta para: " + medicamento.getNome());
                Alerta alerta = new Alerta();
                alerta.setMedicamentoId(medicamento.getId());
                alerta.setMedicamentoNome(medicamento.getNome());
                alerta.setTipo("ESTOQUE_BAIXO");
                alerta.setMensagem("Estoque baixo: " + medicamento.getQuantidadeEstoque() + " un.");
                alerta.setLido(false);
                alertaRepository.save(alerta);
                System.out.println("🔔   ✅ Novo alerta criado - ID: " + alerta.getId() + ", Lido: " + alerta.getLido());
            } else {
                System.out.println("🔔   ⚠️ Já existe alerta ESTOQUE_BAIXO não lido para " + medicamento.getNome() + ", NÃO criando novo alerta");
            }
        }
        System.out.println("🔔 AlertaService.verificarEstoqueBaixo() - FIM");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
    }

    @Transactional
    public void verificarValidadeProxima() {
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("🔔 AlertaService.verificarValidadeProxima() - INÍCIO");
        LocalDate hoje = LocalDate.now();
        LocalDate dataLimite = hoje.plusDays(DIAS_VALIDADE_PROXIMA);
        System.out.println("🔔 Data atual: " + hoje);
        System.out.println("🔔 Data limite (hoje + " + DIAS_VALIDADE_PROXIMA + " dias): " + dataLimite);
        System.out.println("🔔 Buscando medicamentos ativos com validade <= " + dataLimite);
        
        List<Medicamento> medicamentos = medicamentoRepository
                .findByValidadeLessThanEqualAndAtivoTrue(dataLimite);
        
        System.out.println("🔔 Total de medicamentos ativos com validade próxima encontrados: " + medicamentos.size());

        for (Medicamento medicamento : medicamentos) {
            System.out.println("🔔 Verificando medicamento: " + medicamento.getNome() + " (ID: " + medicamento.getId() + ", Validade: " + medicamento.getValidade() + ", Ativo: " + medicamento.getAtivo() + ")");
            if (medicamento.getValidade() != null) {
                // Ignora medicamentos já vencidos (serão tratados por verificarMedicamentosVencidos)
                if (medicamento.getValidade().isBefore(hoje)) {
                    System.out.println("🔔   ⚠️ Medicamento já vencido, ignorando para validade próxima");
                    continue;
                }
                
                System.out.println("🔔   Medicamento não vencido, verificando alertas existentes...");
                // Verifica se já existe alerta não lido
                List<Alerta> todosAlertas = alertaRepository.findByMedicamentoId(medicamento.getId());
                System.out.println("🔔   Total de alertas do medicamento: " + todosAlertas.size());
                for (Alerta a : todosAlertas) {
                    System.out.println("🔔     - Alerta ID: " + a.getId() + ", Tipo: " + a.getTipo() + ", Lido: " + a.getLido());
                }
                
                boolean existeAlertaNaoLido = todosAlertas.stream()
                        .anyMatch(a -> !a.getLido() && "VALIDADE_PROXIMA".equals(a.getTipo()));
                System.out.println("🔔   Existe alerta VALIDADE_PROXIMA não lido? " + existeAlertaNaoLido);

                if (!existeAlertaNaoLido) {
                    // Se não existe alerta não lido E o medicamento ainda está ativo e com validade próxima,
                    // cria um novo alerta (mesmo que já tenha existido um alerta lido anteriormente)
                    // Isso permite que alertas reapareçam quando marcados como "visto" mas o medicamento não foi inativado
                    System.out.println("🔔   ✅ Criando NOVO alerta VALIDADE_PROXIMA para: " + medicamento.getNome());
                    Alerta alerta = new Alerta();
                    alerta.setMedicamentoId(medicamento.getId());
                    alerta.setMedicamentoNome(medicamento.getNome());
                    alerta.setTipo("VALIDADE_PROXIMA");
                    alerta.setMensagem("Validade próxima: " + medicamento.getValidade());
                    alerta.setLido(false);
                    alerta = alertaRepository.save(alerta);
                    alertaRepository.flush();
                    System.out.println("🔔   ✅ Alerta criado - ID: " + alerta.getId() + ", Lido: " + alerta.getLido());
                } else {
                    System.out.println("🔔   ⚠️ Já existe alerta VALIDADE_PROXIMA não lido para " + medicamento.getNome() + ", NÃO criando novo");
                }
            } else {
                System.out.println("🔔   ⚠️ Medicamento sem data de validade, ignorando");
            }
        }
        System.out.println("🔔 AlertaService.verificarValidadeProxima() - FIM");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
    }

    @Transactional
    public void verificarMedicamentosVencidos() {
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("🔔 AlertaService.verificarMedicamentosVencidos() - INÍCIO");
        LocalDate hoje = LocalDate.now();
        System.out.println("🔔 Data atual: " + hoje);
        // Busca todos os medicamentos ativos
        List<Medicamento> todosMedicamentos = medicamentoRepository.findByAtivoTrue();
        System.out.println("🔔 Total de medicamentos ativos encontrados: " + todosMedicamentos.size());

        int vencidosEncontrados = 0;
        int alertasCriados = 0;

        for (Medicamento medicamento : todosMedicamentos) {
            System.out.println("🔔 Verificando medicamento: " + medicamento.getNome() + " (ID: " + medicamento.getId() + ", Validade: " + medicamento.getValidade() + ", Ativo: " + medicamento.getAtivo() + ")");
            // Verifica se a validade é anterior à data atual (ou igual, dependendo da regra)
            if (medicamento.getValidade() != null) {
                boolean isVencido = medicamento.getValidade().isBefore(hoje);
                System.out.println("🔔   Validade: " + medicamento.getValidade() + ", Hoje: " + hoje + ", Vencido: " + isVencido);
                
                if (isVencido) {
                    vencidosEncontrados++;
                    System.out.println("🔔   Medicamento VENCIDO, verificando alertas existentes...");
                    List<Alerta> todosAlertas = alertaRepository.findByMedicamentoId(medicamento.getId());
                    System.out.println("🔔   Total de alertas do medicamento: " + todosAlertas.size());
                    for (Alerta a : todosAlertas) {
                        System.out.println("🔔     - Alerta ID: " + a.getId() + ", Tipo: " + a.getTipo() + ", Lido: " + a.getLido());
                    }
                    
                    // Verifica se já existe alerta não lido para este medicamento
                    boolean existeAlertaNaoLido = todosAlertas.stream()
                            .anyMatch(a -> !a.getLido() && "VALIDADE_VENCIDA".equals(a.getTipo()));
                    System.out.println("🔔   Existe alerta VALIDADE_VENCIDA não lido? " + existeAlertaNaoLido);

                    if (!existeAlertaNaoLido) {
                        // Se não existe alerta não lido E o medicamento ainda está ativo e vencido,
                        // cria um novo alerta (mesmo que já tenha existido um alerta lido anteriormente)
                        // Isso permite que alertas reapareçam quando marcados como "visto" mas o medicamento não foi inativado
                        System.out.println("🔔   ✅ Criando NOVO alerta VALIDADE_VENCIDA para: " + medicamento.getNome());
                        Alerta alerta = new Alerta();
                        alerta.setMedicamentoId(medicamento.getId());
                        alerta.setMedicamentoNome(medicamento.getNome());
                        alerta.setTipo("VALIDADE_VENCIDA");
                        alerta.setMensagem("Medicamento vencido em: " + medicamento.getValidade());
                        alerta.setLido(false);
                        alerta = alertaRepository.save(alerta);
                        alertaRepository.flush();
                        alertasCriados++;
                        System.out.println("🔔   ✅ Alerta criado - ID: " + alerta.getId() + ", Lido: " + alerta.getLido());
                    } else {
                        System.out.println("🔔   ⚠️ Já existe alerta VALIDADE_VENCIDA não lido para " + medicamento.getNome() + ", NÃO criando novo");
                    }
                } else {
                    System.out.println("🔔   Medicamento NÃO vencido, ignorando");
                }
            } else {
                System.out.println("🔔   ⚠️ Medicamento sem data de validade (null), ignorando");
            }
        }
        
        System.out.println("🔔 Verificação concluída - Vencidos encontrados: " + vencidosEncontrados + ", Alertas criados: " + alertasCriados);
        System.out.println("🔔 AlertaService.verificarMedicamentosVencidos() - FIM");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> findAll() {
        return alertaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> findNaoLidos() {
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("🔔 AlertaService.findNaoLidos() - INÍCIO");
        List<Alerta> alertasNaoLidos = alertaRepository.findByLidoFalse();
        System.out.println("🔔 Total de alertas NÃO LIDOS (todos os tipos): " + alertasNaoLidos.size());
        
        // Filtra apenas alertas de medicamentos que ainda existem
        List<Alerta> alertasValidos = alertasNaoLidos.stream()
                .filter(alerta -> {
                    boolean medicamentoExiste = medicamentoRepository.existsById(alerta.getMedicamentoId());
                    if (!medicamentoExiste) {
                        System.out.println("🔔   ⚠️ Alerta ID: " + alerta.getId() + " ignorado - medicamento ID: " + alerta.getMedicamentoId() + " não existe mais");
                    }
                    return medicamentoExiste;
                })
                .collect(Collectors.toList());
        
        System.out.println("🔔 Total de alertas válidos (medicamento existe): " + alertasValidos.size());
        for (Alerta a : alertasValidos) {
            System.out.println("🔔   - Alerta ID: " + a.getId() + ", Tipo: " + a.getTipo() + ", Medicamento: " + a.getMedicamentoNome() + ", Lido: " + a.getLido());
        }
        List<AlertaResponse> response = alertasValidos.stream()
                .sorted((a1, a2) -> a1.getMedicamentoNome().compareToIgnoreCase(a2.getMedicamentoNome()))
                .map(this::toResponse)
                .collect(Collectors.toList());
        System.out.println("🔔 Retornando " + response.size() + " alerta(s)");
        System.out.println("🔔 AlertaService.findNaoLidos() - FIM");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        return response;
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> findEstoqueBaixo() {
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("🔔 AlertaService.findEstoqueBaixo() - INÍCIO");
        List<Alerta> alertasNaoLidos = alertaRepository.findByTipoAndLidoFalse("ESTOQUE_BAIXO");
        System.out.println("🔔 Total de alertas ESTOQUE_BAIXO não lidos encontrados: " + alertasNaoLidos.size());
        
        // Filtra apenas alertas de medicamentos que ainda existem
        List<Alerta> alertasValidos = alertasNaoLidos.stream()
                .filter(alerta -> {
                    boolean medicamentoExiste = medicamentoRepository.existsById(alerta.getMedicamentoId());
                    if (!medicamentoExiste) {
                        System.out.println("🔔   ⚠️ Alerta ID: " + alerta.getId() + " ignorado - medicamento ID: " + alerta.getMedicamentoId() + " não existe mais");
                    }
                    return medicamentoExiste;
                })
                .collect(Collectors.toList());
        
        System.out.println("🔔 Total de alertas válidos (medicamento existe): " + alertasValidos.size());
        for (Alerta a : alertasValidos) {
            System.out.println("🔔   - Alerta ID: " + a.getId() + ", Medicamento ID: " + a.getMedicamentoId() + ", Nome: " + a.getMedicamentoNome() + ", Lido: " + a.getLido() + ", Mensagem: " + a.getMensagem());
        }
        List<AlertaResponse> response = alertasValidos.stream()
                .sorted((a1, a2) -> a1.getMedicamentoNome().compareToIgnoreCase(a2.getMedicamentoNome()))
                .map(this::toResponse)
                .collect(Collectors.toList());
        System.out.println("🔔 Retornando " + response.size() + " alerta(s)");
        System.out.println("🔔 AlertaService.findEstoqueBaixo() - FIM");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        return response;
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> findValidadeProxima() {
        List<Alerta> alertasNaoLidos = alertaRepository.findByTipoAndLidoFalse("VALIDADE_PROXIMA");
        // Filtra apenas alertas de medicamentos que ainda existem E estão ativos
        return alertasNaoLidos.stream()
                .filter(alerta -> {
                    boolean medicamentoExiste = medicamentoRepository.existsById(alerta.getMedicamentoId());
                    if (!medicamentoExiste) {
                        return false;
                    }
                    // Verifica se o medicamento está ativo
                    return medicamentoRepository.findById(alerta.getMedicamentoId())
                            .map(med -> med.getAtivo())
                            .orElse(false);
                })
                .sorted((a1, a2) -> a1.getMedicamentoNome().compareToIgnoreCase(a2.getMedicamentoNome()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> findValidadeVencida() {
        List<Alerta> alertasNaoLidos = alertaRepository.findByTipoAndLidoFalse("VALIDADE_VENCIDA");
        // Filtra apenas alertas de medicamentos que ainda existem E estão ativos
        return alertasNaoLidos.stream()
                .filter(alerta -> {
                    boolean medicamentoExiste = medicamentoRepository.existsById(alerta.getMedicamentoId());
                    if (!medicamentoExiste) {
                        return false;
                    }
                    // Verifica se o medicamento está ativo
                    return medicamentoRepository.findById(alerta.getMedicamentoId())
                            .map(med -> med.getAtivo())
                            .orElse(false);
                })
                .sorted((a1, a2) -> a1.getMedicamentoNome().compareToIgnoreCase(a2.getMedicamentoNome()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AlertaResponse marcarComoLido(UUID id) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Alerta não encontrado"));
        alerta.setLido(true);
        alerta = alertaRepository.save(alerta);
        alertaRepository.flush(); // Force flush to ensure immediate persistence
        return toResponse(alerta);
    }

    /**
     * Marca todos os alertas de estoque baixo de um medicamento específico como lidos
     * Usado quando o estoque é aumentado e passa a ficar acima do limite
     * IMPORTANTE: Este método marca TODOS os alertas de estoque baixo do medicamento como lidos,
     * garantindo que eles desapareçam do painel de controle imediatamente
     */
    @Transactional
    public void marcarAlertasEstoqueBaixoComoLidos(UUID medicamentoId) {
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("🔔 AlertaService.marcarAlertasEstoqueBaixoComoLidos() - INÍCIO");
        System.out.println("🔔 Medicamento ID: " + medicamentoId);
        
        List<Alerta> todosAlertas = alertaRepository.findByMedicamentoId(medicamentoId);
        System.out.println("🔔 Total de alertas do medicamento (todos os tipos): " + todosAlertas.size());
        for (Alerta a : todosAlertas) {
            System.out.println("🔔   - Alerta ID: " + a.getId() + ", Tipo: " + a.getTipo() + ", Lido: " + a.getLido() + ", Mensagem: " + a.getMensagem());
        }
        
        List<Alerta> alertasEstoqueBaixoNaoLidos = todosAlertas
                .stream()
                .filter(a -> !a.getLido() && "ESTOQUE_BAIXO".equals(a.getTipo()))
                .collect(Collectors.toList());
        
        System.out.println("🔔 Alertas de estoque baixo NÃO LIDOS encontrados: " + alertasEstoqueBaixoNaoLidos.size());
        
        if (alertasEstoqueBaixoNaoLidos.isEmpty()) {
            System.out.println("🔔 ⚠️ Nenhum alerta de estoque baixo não lido encontrado para marcar como lido");
            System.out.println("🔔 AlertaService.marcarAlertasEstoqueBaixoComoLidos() - FIM");
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            return;
        }
        
        System.out.println("🔔 ✅ Encontrados " + alertasEstoqueBaixoNaoLidos.size() + " alerta(s) para marcar como LIDO(S):");
        for (Alerta alerta : alertasEstoqueBaixoNaoLidos) {
            System.out.println("🔔   - Marcando Alerta ID: " + alerta.getId() + ", Mensagem: " + alerta.getMensagem() + ", Lido ANTES: " + alerta.getLido());
            alerta.setLido(true);
            Alerta alertaSalvo = alertaRepository.save(alerta);
            System.out.println("🔔   - ✅ Alerta ID: " + alertaSalvo.getId() + " salvo com LIDO = " + alertaSalvo.getLido());
        }
        // Force flush to ensure ALL changes are persisted immediately
        alertaRepository.flush();
        System.out.println("🔔 ✅ FLUSH executado - todas as mudanças foram persistidas no banco");
        
        // Verificar novamente após salvar (com uma nova query para garantir que pegamos do banco)
        // IMPORTANTE: Usar findByTipoAndLidoFalse para garantir que não retornamos alertas lidos
        List<Alerta> alertasNaoLidosAposSalvar = alertaRepository.findByTipoAndLidoFalse("ESTOQUE_BAIXO")
                .stream()
                .filter(a -> medicamentoId.equals(a.getMedicamentoId()))
                .collect(Collectors.toList());
        System.out.println("🔔 ✅ VERIFICAÇÃO PÓS-SALVAR (nova query usando findByTipoAndLidoFalse):");
        System.out.println("🔔   Total de alertas ESTOQUE_BAIXO NÃO LIDOS após salvar: " + alertasNaoLidosAposSalvar.size());
        for (Alerta a : alertasNaoLidosAposSalvar) {
            System.out.println("🔔   - Alerta ID: " + a.getId() + ", Lido: " + a.getLido() + ", Mensagem: " + a.getMensagem());
        }
        
        System.out.println("🔔 ✅ Total de " + alertasEstoqueBaixoNaoLidos.size() + " alerta(s) marcado(s) como lido(s) com sucesso");
        System.out.println("🔔 AlertaService.marcarAlertasEstoqueBaixoComoLidos() - FIM");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
    }

    /**
     * Remove todos os alertas de um medicamento específico
     * Usado quando um medicamento é reativado, permitindo que novos alertas sejam criados se necessário
     */
    @Transactional
    public void removerTodosAlertasDoMedicamento(UUID medicamentoId) {
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("🔔 AlertaService.removerTodosAlertasDoMedicamento() - INÍCIO");
        System.out.println("🔔 Medicamento ID: " + medicamentoId);
        
        List<Alerta> todosAlertas = alertaRepository.findByMedicamentoId(medicamentoId);
        System.out.println("🔔 Total de alertas do medicamento a serem removidos: " + todosAlertas.size());
        
        if (todosAlertas.isEmpty()) {
            System.out.println("🔔 ⚠️ Nenhum alerta encontrado para remover");
            System.out.println("🔔 AlertaService.removerTodosAlertasDoMedicamento() - FIM");
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            return;
        }
        
        for (Alerta alerta : todosAlertas) {
            System.out.println("🔔   - Removendo Alerta ID: " + alerta.getId() + ", Tipo: " + alerta.getTipo() + ", Mensagem: " + alerta.getMensagem());
            alertaRepository.delete(alerta);
        }
        alertaRepository.flush();
        System.out.println("🔔 ✅ Total de " + todosAlertas.size() + " alerta(s) removido(s) com sucesso");
        System.out.println("🔔 AlertaService.removerTodosAlertasDoMedicamento() - FIM");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
    }

    /**
     * Marca todos os alertas de um medicamento específico como lidos
     * Usado quando o medicamento é excluído ou inativado, garantindo que seus alertas desapareçam do painel de controle
     */
    @Transactional
    public void marcarTodosAlertasComoLidos(UUID medicamentoId) {
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("🔔 AlertaService.marcarTodosAlertasComoLidos() - INÍCIO");
        System.out.println("🔔 Medicamento ID: " + medicamentoId);
        
        List<Alerta> todosAlertas = alertaRepository.findByMedicamentoId(medicamentoId);
        System.out.println("🔔 Total de alertas do medicamento (todos os tipos): " + todosAlertas.size());
        
        // Marca TODOS os alertas como lidos, independentemente do status atual
        // Isso garante que mesmo se houver algum problema de sincronização, todos serão marcados
        int alertasMarcados = 0;
        for (Alerta alerta : todosAlertas) {
            if (!alerta.getLido()) {
                System.out.println("🔔   - Marcando Alerta ID: " + alerta.getId() + ", Tipo: " + alerta.getTipo() + ", Mensagem: " + alerta.getMensagem() + ", Lido ANTES: " + alerta.getLido());
                alerta.setLido(true);
                alertaRepository.save(alerta);
                alertasMarcados++;
                System.out.println("🔔   - ✅ Alerta ID: " + alerta.getId() + " salvo com LIDO = true");
            } else {
                System.out.println("🔔   - Alerta ID: " + alerta.getId() + " já está marcado como lido, pulando");
            }
        }
        
        // Force flush to ensure ALL changes are persisted immediately
        alertaRepository.flush();
        System.out.println("🔔 ✅ FLUSH executado - todas as mudanças foram persistidas no banco");
        
        System.out.println("🔔 ✅ Total de " + alertasMarcados + " alerta(s) marcado(s) como lido(s) com sucesso");
        System.out.println("🔔 AlertaService.marcarTodosAlertasComoLidos() - FIM");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
    }

    private AlertaResponse toResponse(Alerta alerta) {
        return new AlertaResponse(
                alerta.getId(),
                alerta.getMedicamentoId(),
                alerta.getMedicamentoNome(),
                alerta.getTipo(),
                alerta.getMensagem(),
                alerta.getLido(),
                alerta.getCreatedAt()
        );
    }
}

