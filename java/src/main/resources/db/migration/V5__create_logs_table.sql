-- Tabela de logs/auditoria
CREATE TABLE IF NOT EXISTS logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_operacao VARCHAR(20) NOT NULL,
    tipo_entidade VARCHAR(50) NOT NULL,
    entidade_id UUID NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    detalhes TEXT,
    usuario_id UUID NOT NULL,
    usuario_nome VARCHAR(255) NOT NULL,
    usuario_email VARCHAR(255) NOT NULL,
    data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Comentários nas colunas
COMMENT ON COLUMN logs.tipo_operacao IS 'Tipo de operação: CREATE, UPDATE, DELETE, LOGIN';
COMMENT ON COLUMN logs.tipo_entidade IS 'Tipo de entidade: USUARIO, MEDICAMENTO, CATEGORIA, CLIENTE, VENDA, ESTOQUE, LOGIN';
COMMENT ON COLUMN logs.entidade_id IS 'ID da entidade afetada pela operação';
COMMENT ON COLUMN logs.descricao IS 'Descrição da operação realizada';
COMMENT ON COLUMN logs.detalhes IS 'Detalhes adicionais da operação (JSON ou texto)';
COMMENT ON COLUMN logs.usuario_id IS 'ID do usuário que realizou a operação';
COMMENT ON COLUMN logs.usuario_nome IS 'Nome do usuário que realizou a operação';
COMMENT ON COLUMN logs.usuario_email IS 'Email do usuário que realizou a operação';
COMMENT ON COLUMN logs.data_hora IS 'Data e hora da operação';

-- Índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_logs_tipo_operacao ON logs(tipo_operacao);
CREATE INDEX IF NOT EXISTS idx_logs_tipo_entidade ON logs(tipo_entidade);
CREATE INDEX IF NOT EXISTS idx_logs_usuario_id ON logs(usuario_id);
CREATE INDEX IF NOT EXISTS idx_logs_data_hora ON logs(data_hora DESC);






