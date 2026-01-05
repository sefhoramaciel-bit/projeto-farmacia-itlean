-- Adicionar coluna estoque_total na tabela movimentacoes_estoque
ALTER TABLE movimentacoes_estoque ADD COLUMN IF NOT EXISTS estoque_total INTEGER;

-- Comentário na coluna
COMMENT ON COLUMN movimentacoes_estoque.estoque_total IS 'Quantidade total de estoque após esta movimentação (para auditoria e rastreabilidade)';

-- Atualizar registros existentes (se houver)
-- Para registros existentes, precisaríamos calcular retroativamente, mas como não temos como saber o saldo exato,
-- deixamos como NULL. Novos registros sempre terão o valor correto.
-- Se necessário, pode-se fazer um script de migração de dados separado.






