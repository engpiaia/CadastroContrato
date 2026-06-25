# Backlog de Melhorias de UI

## Contexto

Esta lista consolida a avaliacao visual produzida para o `CadastroContrato` e organiza as melhorias em itens acionaveis de UX/UI.

- Material visual no Canva:
  - Edicao: `https://www.canva.com/d/D2-iviYiinUqwhZ`
  - Visualizacao: `https://www.canva.com/d/c6BH47kFCDmwq5f`
- Fonte versionada da avaliacao visual: `docs/canva-avaliacao/index.html`

## Objetivos

- Melhorar hierarquia visual e legibilidade.
- Padronizar componentes e feedbacks.
- Reduzir densidade excessiva em telas com muito formulario.
- Tornar alertas e estados criticos mais claros.

## Backlog priorizado

### P0 - Ganhos rapidos

#### 1. Padronizar botoes e pesos de acao

- Definir estilos consistentes para acoes primarias, secundarias e destrutivas.
- Reduzir competicao visual entre `Salvar`, `Ajuda` e `Excluir`.
- Aplicar o mesmo padrao em login, parceiros, contratos e ajuda.

**Impacto esperado:** leitura mais rapida das acoes principais e menor risco operacional.

#### 2. Reforcar labels, espacamento e alinhamento de formularios

- Dar mais destaque tipografico aos labels.
- Criar ritmo uniforme entre label, campo, ajuda e validacao.
- Revisar larguras de campos para refletir prioridade e tamanho esperado do dado.

**Impacto esperado:** preenchimento mais fluido e menor carga cognitiva.

#### 3. Melhorar contraste e leitura da navegacao e tabelas

- Aumentar contraste de item ativo na ajuda.
- Fortalecer cabecalhos de tabela e suavizar divisorias internas.
- Revisar contraste de textos auxiliares e informacoes de apoio.

**Impacto esperado:** melhor escaneabilidade e orientacao visual.

### P1 - Evolucao estrutural

#### 4. Criar sistema de cores semanticas

- Verde para sucesso e confirmacao.
- Laranja para alerta e prazo proximo.
- Vermelho para risco, expiracao e acoes destrutivas.
- Reaplicar o padrao no dashboard, alertas e feedbacks de formulario.

**Impacto esperado:** interpretacao mais imediata do estado de cada informacao.

#### 5. Reorganizar o dashboard por camadas de decisao

- Separar KPIs, distribuicoes e vencimentos criticos.
- Padronizar dimensoes e alinhamento dos cards.
- Dar mais destaque ao bloco de urgencias.

**Impacto esperado:** leitura executiva mais rapida e priorizacao mais clara.

#### 6. Reestruturar a tela de contratos em grupos logicos

- Separar informacoes obrigatorias, financeiras e de vigencia.
- Aproximar validacoes e mensagens do contexto do campo.
- Reduzir o destaque de acoes destrutivas durante o preenchimento.

**Impacto esperado:** maior produtividade e menor chance de erro no cadastro.

### P2 - Refinamentos de experiencia

#### 7. Enriquecer a tela de login com contexto e suporte

- Inserir microcopy de orientacao para perfis e acesso.
- Sinalizar seguranca ou confiabilidade do acesso.
- Avaliar inclusao de caminho para recuperacao de acesso ou suporte.

#### 8. Melhorar navegacao contextual da ajuda

- Destacar melhor busca, topicos e conteudo selecionado.
- Tornar o atalho `F1` mais visivel.
- Criar secoes mais curtas e escaneaveis com passos claros.

## Sugestao de execucao

1. Criar um mini design system local com tokens de cor, espacamento e tipos de botao.
2. Aplicar primeiro nas telas de `Login` e `Contratos`, onde o ganho perceptivel tende a ser maior.
3. Levar o novo padrao para `Parceiros`, `Dashboard` e `Ajuda`.
4. Validar com revisao visual comparativa antes/depois.

## Criterios de aceite sugeridos

- A acao principal de cada tela e identificavel em menos de 3 segundos.
- Estados destrutivos nao competem visualmente com a acao primaria.
- Formularios mantem alinhamento e espacamento consistentes.
- Alertas criticos do dashboard se destacam dos indicadores informativos.
- A ajuda contextual tem navegacao e estado ativo claramente perceptiveis.
