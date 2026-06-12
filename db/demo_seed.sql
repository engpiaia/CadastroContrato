-- Seed de demonstracao para o Contratech.
-- Recria a base com uma massa rica para apresentacoes comerciais.
-- Use somente em ambiente de desenvolvimento/demo.
-- Compatível com Database Client/VS Code: nao usa BEGIN/COMMIT explicitos
-- nem depende da extensao pgcrypto.

TRUNCATE clausulas, contratos, parceiros, usuarios RESTART IDENTITY CASCADE;

INSERT INTO usuarios (nome, sobrenome, email, senha, tipo_usuario)
VALUES
('Admin', 'Sistema', 'admin@example.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN'),
('Responsavel', 'Empresa', 'responsavel@example.com', 'd8e59cdb80633b481f9789c8c3bff7f95223da62225f0a781034c07035f6e76c', 'RESPONSAVEL'),
('Visualizador', 'Usuario', 'viewer@example.com', '65375049b9e4d7cad6c9ba286fdeb9394b28135a3e84136404cfccfdcc438894', 'VISUALIZADOR');

INSERT INTO parceiros (razao_social, cnpj_cpf, endereco, cidade, uf, cep, telefone, email)
VALUES
('Banco do Brasil S.A.', '00000000000191', 'Setor Bancario Sul, Quadra 1, Bloco G', 'Brasilia', 'DF', '70073-901', '(61) 4004-0001', 'contratos@bb.com.br'),
('Petroleo Brasileiro S.A. Petrobras', '33000167000101', 'Av. Republica do Chile, 65', 'Rio de Janeiro', 'RJ', '20031-912', '(21) 3224-4477', 'contratos@petrobras.com.br'),
('Vale S.A.', '33592510000154', 'Praia de Botafogo, 186', 'Rio de Janeiro', 'RJ', '22250-145', '(21) 3485-5000', 'contratos@vale.com'),
('Itau Unibanco S.A.', '60701190000104', 'Praca Alfredo Egydio de Souza Aranha, 100', 'Sao Paulo', 'SP', '04344-902', '(11) 4004-4828', 'contratos@itau.com.br'),
('Banco Bradesco S.A.', '60746948000112', 'Nucleo Cidade de Deus, s/n', 'Osasco', 'SP', '06029-900', '(11) 3684-4011', 'contratos@bradesco.com.br'),
('Banco Santander Brasil S.A.', '90400888000142', 'Av. Presidente Juscelino Kubitschek, 2041', 'Sao Paulo', 'SP', '04543-011', '(11) 3553-3300', 'contratos@santander.com.br'),
('Magazine Luiza S.A.', '47960950000121', 'Rua Voluntarios da Franca, 1465', 'Franca', 'SP', '14400-490', '(16) 3711-1000', 'contratos@magazineluiza.com.br'),
('Embraer S.A.', '07689002000189', 'Av. Brigadeiro Faria Lima, 2170', 'Sao Jose dos Campos', 'SP', '12227-901', '(12) 3927-1000', 'contratos@embraer.com.br'),
('Ambev S.A.', '07526557000100', 'Rua Dr. Renato Paes de Barros, 1017', 'Sao Paulo', 'SP', '04530-001', '(11) 2122-1200', 'contratos@ambev.com.br'),
('WEG S.A.', '84429695000111', 'Av. Prefeito Waldemar Grubba, 3300', 'Jaragua do Sul', 'SC', '89256-900', '(47) 3276-4000', 'contratos@weg.net'),
('B3 S.A. Brasil Bolsa Balcao', '09346601000125', 'Praca Antonio Prado, 48', 'Sao Paulo', 'SP', '01010-901', '(11) 2565-4000', 'contratos@b3.com.br'),
('Natura Cosmeticos S.A.', '71673990000177', 'Av. Alexandre Colares, 1188', 'Sao Paulo', 'SP', '05106-000', '(11) 4446-2000', 'contratos@natura.net'),
('Centrais Eletricas Brasileiras S.A. Eletrobras', '00001180000126', 'Rua da Quitanda, 196', 'Rio de Janeiro', 'RJ', '20091-005', '(21) 2514-5151', 'contratos@eletrobras.com'),
('Localiza Rent a Car S.A.', '16670085000155', 'Av. Bernardo de Vasconcelos, 377', 'Belo Horizonte', 'MG', '31150-000', '(31) 3247-7000', 'contratos@localiza.com'),
('Companhia Siderurgica Nacional', '33042730000104', 'Av. Brigadeiro Faria Lima, 3400', 'Sao Paulo', 'SP', '04538-132', '(11) 3049-7100', 'contratos@csn.com.br'),
('Totvs S.A.', '61585865000151', 'Av. Braz Leme, 1000', 'Sao Paulo', 'SP', '02511-000', '(11) 2099-7100', 'contratos@totvs.com.br'),
('TIM S.A.', '02421421000111', 'Av. Joao Cabral de Mello Neto, 850', 'Rio de Janeiro', 'RJ', '22775-057', '(21) 4109-4167', 'contratos@timbrasil.com.br'),
('Telefonica Brasil S.A.', '02558157000162', 'Av. Engenheiro Luiz Carlos Berrini, 1376', 'Sao Paulo', 'SP', '04571-936', '(11) 3430-3687', 'contratos@telefonica.com.br'),
('Caixa Economica Federal', '00360305000104', 'Setor Bancario Sul, Quadra 4, Lotes 3/4', 'Brasilia', 'DF', '70092-900', '(61) 3206-9000', 'contratos@caixa.gov.br'),
('Empresa Brasileira de Correios e Telegrafos', '34028316000103', 'SBN Quadra 1, Bloco A', 'Brasilia', 'DF', '70002-900', '(61) 2141-7000', 'contratos@correios.com.br');

WITH parceiros_ordenados AS (
    SELECT id, row_number() OVER (ORDER BY id) AS rn
    FROM parceiros
),
contratos_demo AS (
    SELECT
        n,
        p.id AS parceiro_id,
        ('CT-DEMO-2026-' || lpad(n::text, 3, '0')) AS numero_contrato,
        (ARRAY[
            'Implantacao de modulo de gestao contratual',
            'Fornecimento recorrente de licencas corporativas',
            'Consultoria para revisao de processos administrativos',
            'Locacao de infraestrutura e equipamentos de apoio',
            'Suporte tecnico especializado e monitoramento',
            'Integracao de dados entre sistemas internos',
            'Treinamento operacional para equipes regionais',
            'Servico de auditoria e conformidade documental',
            'Manutencao preventiva de ambiente tecnologico',
            'Projeto de melhoria de indicadores executivos'
        ])[((n - 1) % 10) + 1] AS objeto,
        (ARRAY['SERVICO', 'FORNECIMENTO', 'MISTO', 'LOCACAO', 'CONSULTORIA'])[((n - 1) % 5) + 1] AS tipo,
        (ARRAY['A_VISTA', 'PARCELADO', 'MENSAL', 'RECORRENTE'])[((n - 1) % 4) + 1] AS forma_pagamento,
        CASE
            WHEN n % 10 IN (1, 2, 3, 4, 5) THEN 'ATIVO'
            WHEN n % 10 IN (6, 7) THEN 'CONCLUIDO'
            WHEN n % 10 = 8 THEN 'SUSPENSO'
            ELSE 'CANCELADO'
        END AS status,
        (3500 + (n * 1375) + ((n % 7) * 820))::numeric(15,2) AS valor_contrato,
        (250 + ((n % 9) * 175))::numeric(15,2) AS multa
    FROM generate_series(1, 50) AS n
    JOIN parceiros_ordenados p ON p.rn = ((n - 1) % 20) + 1
),
contratos_datas AS (
    SELECT
        *,
        CASE
            WHEN status = 'ATIVO' AND n % 5 = 1 THEN CURRENT_DATE - (150 + n)
            WHEN status = 'ATIVO' AND n % 5 = 2 THEN CURRENT_DATE - (90 + n)
            WHEN status = 'ATIVO' THEN CURRENT_DATE - (30 + n)
            WHEN status = 'CONCLUIDO' THEN CURRENT_DATE - (420 + n)
            WHEN status = 'SUSPENSO' THEN CURRENT_DATE - (180 + n)
            ELSE CURRENT_DATE - (360 + n)
        END AS data_inicio,
        CASE
            WHEN status = 'ATIVO' AND n % 5 = 1 THEN CURRENT_DATE - ((n % 20) + 1)
            WHEN status = 'ATIVO' AND n % 5 = 2 THEN CURRENT_DATE + ((n % 25) + 1)
            WHEN status = 'ATIVO' THEN CURRENT_DATE + (90 + (n % 240))
            WHEN status = 'CONCLUIDO' THEN CURRENT_DATE - (30 + (n % 120))
            WHEN status = 'SUSPENSO' THEN CURRENT_DATE + (45 + (n % 120))
            ELSE CURRENT_DATE - (15 + (n % 180))
        END AS data_fim
    FROM contratos_demo
)
INSERT INTO contratos (
    parceiro_id, objeto, valor_contrato, multa, data_inicio, data_fim,
    numero_contrato, descricao, tipo, forma_pagamento, observacoes, status
)
SELECT
    parceiro_id,
    objeto,
    valor_contrato,
    multa,
    data_inicio,
    data_fim,
    numero_contrato,
    'Contrato demo para ' || lower(objeto) || ', com acompanhamento de prazos, valores, responsaveis e indicadores gerenciais.',
    tipo,
    forma_pagamento,
    'Massa de demonstracao: contrato distribuido entre parceiros reais para apresentar filtros, dashboard, alertas e clausulas.',
    status
FROM contratos_datas
ORDER BY n;

INSERT INTO clausulas (contrato_id, numero, descricao)
SELECT
    c.id,
    s.numero,
    CASE s.numero
        WHEN 1 THEN 'Vigencia contratual definida entre as datas de inicio e fim, com obrigacao de acompanhamento mensal pelo responsavel.'
        WHEN 2 THEN 'Pagamento conforme forma pactuada, sujeito a registro de comprovantes e conciliacao financeira.'
        WHEN 3 THEN 'Nivel de servico minimo, confidencialidade das informacoes e atendimento prioritario para incidentes criticos.'
        ELSE 'Reajuste, multa, rescisao e tratamento de dados pessoais conforme regras comerciais, LGPD e legislacao aplicavel.'
    END
FROM contratos c
CROSS JOIN LATERAL generate_series(1, 2 + (c.id % 3)) AS s(numero)
ORDER BY c.id, s.numero;

DO $$
DECLARE
    total_parceiros integer;
    total_contratos integer;
    total_clausulas integer;
    total_numeros_unicos integer;
BEGIN
    SELECT COUNT(*) INTO total_parceiros FROM parceiros;
    SELECT COUNT(*) INTO total_contratos FROM contratos;
    SELECT COUNT(*) INTO total_clausulas FROM clausulas;
    SELECT COUNT(DISTINCT numero_contrato) INTO total_numeros_unicos FROM contratos;

    IF total_parceiros <> 20 THEN
        RAISE EXCEPTION 'Demo seed invalido: esperado 20 parceiros, encontrado %', total_parceiros;
    END IF;

    IF total_contratos <> 50 THEN
        RAISE EXCEPTION 'Demo seed invalido: esperado 50 contratos, encontrado %', total_contratos;
    END IF;

    IF total_clausulas < 100 THEN
        RAISE EXCEPTION 'Demo seed invalido: esperado ao menos 100 clausulas, encontrado %', total_clausulas;
    END IF;

    IF total_numeros_unicos <> 50 THEN
        RAISE EXCEPTION 'Demo seed invalido: esperado 50 numeros de contrato unicos, encontrado %', total_numeros_unicos;
    END IF;
END $$;
