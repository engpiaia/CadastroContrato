-- Seed inicial para o banco de dados
-- Gera dados de exemplo: parceiros, contratos, cláusulas e usuários
-- As senhas são armazenadas como SHA-256 usando a extensão pgcrypto

-- Habilita pgcrypto para o digest
CREATE EXTENSION IF NOT EXISTS pgcrypto;

BEGIN;

-- Limpa tabelas e reinicia sequências para ambiente de desenvolvimento
TRUNCATE clausulas, contratos, parceiros, usuarios RESTART IDENTITY CASCADE;

-- Inserir parceiro de exemplo
INSERT INTO parceiros (razao_social, cnpj_cpf, endereco, cidade, uf, cep, telefone, email)
VALUES ('Empresa Exemplo LTDA', '12345678000199', 'Rua Exemplo, 100', 'Florianópolis', 'SC', '88000-000', '(48)99999-9999', 'contato@exemplo.com');

-- Criar contrato vinculado ao parceiro inserido e adicionar uma cláusula
WITH p AS (
  SELECT id FROM parceiros WHERE cnpj_cpf = '12345678000199'
), c AS (
  INSERT INTO contratos (parceiro_id, objeto, valor_contrato, multa, data_inicio, data_fim, numero_contrato, descricao, tipo, forma_pagamento, observacoes, status)
  SELECT id, 'Serviço de desenvolvimento de software', 15000.00, 0.00, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'CT-0001', 'Contrato de desenvolvimento e manutenção', 'SERVICO', 'Parcelado', 'Nenhuma', 'ATIVO' FROM p
  RETURNING id
)
INSERT INTO clausulas (contrato_id, numero, descricao)
SELECT id, 1, 'Entrega do MVP em 90 dias' FROM c;

-- Inserir usuários iniciais com senha SHA-256 (texto simples é hashado aqui)
-- Senhas de exemplo: admin123, responsavel123, viewer123
INSERT INTO usuarios (nome, sobrenome, email, senha, tipo_usuario)
VALUES
('Admin', 'Sistema', 'admin@example.com', encode(digest('admin123', 'sha256'), 'hex'), 'ADMIN'),
('Responsavel', 'Empresa', 'responsavel@example.com', encode(digest('responsavel123', 'sha256'), 'hex'), 'RESPONSAVEL'),
('Visualizador', 'Usuario', 'viewer@example.com', encode(digest('viewer123', 'sha256'), 'hex'), 'VISUALIZADOR');

COMMIT;

-- Observação: Este script é para uso em desenvolvimento. Em produção, não use senhas em texto claro no arquivo.
