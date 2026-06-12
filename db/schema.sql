--
-- PostgreSQL database dump
--

-- Dumped from database version 14.18
-- Dumped by pg_dump version 14.18

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: clausulas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.clausulas (
    id integer NOT NULL,
    contrato_id integer NOT NULL,
    numero integer NOT NULL,
    descricao text NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: clausulas_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.clausulas_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: clausulas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.clausulas_id_seq OWNED BY public.clausulas.id;


--
-- Name: contratos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.contratos (
    id integer NOT NULL,
    parceiro_id integer NOT NULL,
    objeto character varying(300) NOT NULL,
    valor_contrato numeric(15,2) NOT NULL,
    multa numeric(15,2),
    data_inicio date NOT NULL,
    data_fim date NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    numero_contrato character varying(50) NOT NULL,
    descricao text,
    tipo character varying(30) DEFAULT 'SERVICO'::character varying,
    forma_pagamento character varying(50),
    observacoes text,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    status character varying(20) DEFAULT 'ATIVO'::character varying NOT NULL,
    CONSTRAINT chk_contrato_status CHECK (((status)::text = ANY ((ARRAY['ATIVO'::character varying, 'SUSPENSO'::character varying, 'CONCLUIDO'::character varying, 'CANCELADO'::character varying])::text[]))),
    CONSTRAINT chk_datas CHECK ((data_fim >= data_inicio)),
    CONSTRAINT contratos_multa_check CHECK ((multa >= (0)::numeric)),
    CONSTRAINT contratos_preco_global_check CHECK ((valor_contrato >= (0)::numeric))
);


--
-- Name: contratos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.contratos_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: contratos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.contratos_id_seq OWNED BY public.contratos.id;


--
-- Name: parceiros; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.parceiros (
    id integer NOT NULL,
    razao_social character varying(200) NOT NULL,
    cnpj_cpf character varying(18) NOT NULL,
    endereco character varying(255),
    cidade character varying(100),
    uf character(2),
    cep character varying(10),
    telefone character varying(20),
    email character varying(150),
    ativo boolean DEFAULT true NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: parceiros_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.parceiros_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: parceiros_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.parceiros_id_seq OWNED BY public.parceiros.id;


--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuarios (
    id integer NOT NULL,
    nome character varying(100) NOT NULL,
    sobrenome character varying(100) NOT NULL,
    email character varying(150) NOT NULL,
    senha character varying(255) NOT NULL,
    tipo_usuario character varying(20) NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT usuarios_tipo_usuario_check CHECK (((tipo_usuario)::text = ANY ((ARRAY['ADMIN'::character varying, 'RESPONSAVEL'::character varying, 'VISUALIZADOR'::character varying])::text[])))
);


--
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.usuarios_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- Name: vw_contratos_vencidos; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.vw_contratos_vencidos AS
 SELECT c.id,
    c.numero_contrato,
    c.objeto,
    c.status,
    c.data_fim,
    c.parceiro_id,
    COALESCE(p.razao_social, ''::character varying) AS parceiro_nome
   FROM (public.contratos c
     LEFT JOIN public.parceiros p ON ((p.id = c.parceiro_id)))
  WHERE ((c.data_fim < CURRENT_DATE) AND ((c.status)::text = 'ATIVO'::text));


--
-- Name: clausulas id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clausulas ALTER COLUMN id SET DEFAULT nextval('public.clausulas_id_seq'::regclass);


--
-- Name: contratos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contratos ALTER COLUMN id SET DEFAULT nextval('public.contratos_id_seq'::regclass);


--
-- Name: parceiros id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parceiros ALTER COLUMN id SET DEFAULT nextval('public.parceiros_id_seq'::regclass);


--
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- Name: clausulas clausulas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clausulas
    ADD CONSTRAINT clausulas_pkey PRIMARY KEY (id);


--
-- Name: contratos contratos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT contratos_pkey PRIMARY KEY (id);


--
-- Name: contratos contratos_numero_contrato_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT contratos_numero_contrato_key UNIQUE (numero_contrato);


--
-- Name: parceiros parceiros_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parceiros
    ADD CONSTRAINT parceiros_pkey PRIMARY KEY (id);


--
-- Name: clausulas uq_clausula_contrato; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clausulas
    ADD CONSTRAINT uq_clausula_contrato UNIQUE (contrato_id, numero);


--
-- Name: usuarios usuarios_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: idx_clausulas_contrato; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_clausulas_contrato ON public.clausulas USING btree (contrato_id);


--
-- Name: idx_contratos_data_fim; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_contratos_data_fim ON public.contratos USING btree (data_fim);


--
-- Name: idx_contratos_numero; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_contratos_numero ON public.contratos USING btree (numero_contrato);


--
-- Name: idx_contratos_objeto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_contratos_objeto ON public.contratos USING btree (lower((objeto)::text));


--
-- Name: idx_contratos_parceiro; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_contratos_parceiro ON public.contratos USING btree (parceiro_id);


--
-- Name: idx_contratos_parceiro_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_contratos_parceiro_id ON public.contratos USING btree (parceiro_id);


--
-- Name: idx_contratos_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_contratos_status ON public.contratos USING btree (status);


--
-- Name: idx_parceiros_cnpj_cpf; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_parceiros_cnpj_cpf ON public.parceiros USING btree (cnpj_cpf);


--
-- Name: idx_parceiros_razao_social; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_parceiros_razao_social ON public.parceiros USING btree (lower((razao_social)::text));


--
-- Name: idx_usuarios_nome; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_usuarios_nome ON public.usuarios USING btree (lower((nome)::text));


--
-- Name: parceiros_cnpj_cpf_ativo_uq; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX parceiros_cnpj_cpf_ativo_uq ON public.parceiros USING btree (cnpj_cpf) WHERE (ativo = true);


--
-- Name: clausulas clausulas_contrato_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clausulas
    ADD CONSTRAINT clausulas_contrato_id_fkey FOREIGN KEY (contrato_id) REFERENCES public.contratos(id) ON DELETE CASCADE;


--
-- Name: contratos contratos_parceiro_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT contratos_parceiro_id_fkey FOREIGN KEY (parceiro_id) REFERENCES public.parceiros(id) ON DELETE RESTRICT;


--
-- PostgreSQL database dump complete
--
