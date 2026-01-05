-- Adicionar coluna data_nascimento na tabela clientes
-- Primeiro adiciona como nullable para permitir dados existentes
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS data_nascimento DATE;

-- Comentário na coluna
COMMENT ON COLUMN clientes.data_nascimento IS 'Data de nascimento do cliente (obrigatória para validação de idade mínima de 18 anos)';

