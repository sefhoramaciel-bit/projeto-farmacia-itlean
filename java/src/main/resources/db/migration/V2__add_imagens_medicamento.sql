-- Criar tabela para armazenar imagens dos medicamentos
CREATE TABLE IF NOT EXISTS medicamento_imagens (
    medicamento_id UUID NOT NULL,
    imagem_url VARCHAR(500) NOT NULL,
    CONSTRAINT fk_medicamento_imagens FOREIGN KEY (medicamento_id) REFERENCES medicamentos(id) ON DELETE CASCADE,
    PRIMARY KEY (medicamento_id, imagem_url)
);

-- Criar índice para melhor performance
CREATE INDEX IF NOT EXISTS idx_medicamento_imagens_medicamento ON medicamento_imagens(medicamento_id);
