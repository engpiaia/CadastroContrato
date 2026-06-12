-- Migration para aplicar as novas regras em bancos ja existentes.
-- Execute somente depois de corrigir contratos com numero_contrato nulo ou duplicado.

ALTER TABLE public.contratos
    ALTER COLUMN numero_contrato SET NOT NULL;

ALTER TABLE public.contratos
    ADD CONSTRAINT contratos_numero_contrato_key UNIQUE (numero_contrato);
