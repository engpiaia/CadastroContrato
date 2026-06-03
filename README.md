# CadastroContrato

Sistema desktop para cadastro e acompanhamento de contratos, parceiros, clausulas e usuarios. A aplicacao usa Java 17, JavaFX, PostgreSQL e Maven.

## Funcionalidades

- Login com perfis `ADMIN`, `RESPONSAVEL` e `VISUALIZADOR`.
- Cadastro de parceiros com exclusao logica.
- Consulta publica de CNPJ pela API `https://open.cnpja.com/office/{cnpj}`.
- Cadastro de contratos, valores, datas, status, tipos e formas de pagamento.
- Cadastro de clausulas vinculadas a contratos.
- Dashboard na tela principal com indicadores de parceiros, contratos, contratos vencidos e contratos a vencer em 30 dias.
- Alertas de contratos `ATIVO` vencidos ou proximos do vencimento.

## Requisitos

- JDK 17.
- Maven 3.8+ instalado no `PATH`, ou Maven Wrapper funcional.
- PostgreSQL 14+.
- Acesso de rede se quiser usar a consulta automatica de CNPJ.

Observacao: os scripts `mvnw` e `mvnw.cmd` existem no projeto, mas o arquivo `.mvn/wrapper/maven-wrapper.jar` precisa estar presente para o wrapper funcionar. Se ele estiver ausente, use `mvn` instalado localmente ou regenere o wrapper.

## Configuracao

As credenciais do banco podem vir de variaveis de ambiente ou do arquivo `.env` na raiz do projeto. O arquivo `.env` nao deve ser versionado.

1. Copie o exemplo:

```powershell
copy .env.example .env
```

Em Linux/macOS:

```bash
cp .env.example .env
```

2. Ajuste os valores:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=cadastro_contrato
DB_USER=postgres
DB_PASSWORD=sua_senha
```

`DB_PASSWORD` e obrigatoria. As demais variaveis possuem valores padrao no codigo, mas e recomendado declara-las no `.env`.

## Banco de Dados

Crie o banco no PostgreSQL:

```sql
CREATE DATABASE cadastro_contrato;
```

Importe a estrutura:

```bash
psql -U postgres -d cadastro_contrato -f database/schema.sql
```

Opcionalmente, importe dados de desenvolvimento:

```bash
psql -U postgres -d cadastro_contrato -f database/seed.sql
```

O `seed.sql` recria dados de exemplo e usa `TRUNCATE ... RESTART IDENTITY CASCADE`; portanto, use apenas em ambiente de desenvolvimento.

Usuarios criados pelo seed:

| Perfil | E-mail | Senha |
| --- | --- | --- |
| ADMIN | `admin@example.com` | `admin123` |
| RESPONSAVEL | `responsavel@example.com` | `responsavel123` |
| VISUALIZADOR | `viewer@example.com` | `viewer123` |

Tabelas principais:

- `usuarios`
- `parceiros`
- `contratos`
- `clausulas`

Status validos de contrato conforme o schema:

- `ATIVO`
- `SUSPENSO`
- `CONCLUIDO`
- `CANCELADO`

## Desenvolvimento

Compilar:

```bash
mvn clean compile
```

Executar em desenvolvimento:

```bash
mvn javafx:run
```

Se o Maven Wrapper estiver completo:

```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd javafx:run
```

Em Linux/macOS:

```bash
./mvnw clean compile
./mvnw javafx:run
```

## Empacotamento

Gerar o JAR executavel com dependencias:

```bash
mvn clean package
```

O artefato esperado e:

```text
target/CadastroContrato-1.0.0.jar
```

Executar o JAR:

```bash
java -jar target/CadastroContrato-1.0.0.jar
```

O ponto de entrada do JAR e `com.contratech.cadastrocontrato.Launcher`, que delega para `App`.

## Deploy

Para instalar em outro ambiente:

1. Instale JDK 17.
2. Instale/crie o banco PostgreSQL.
3. Execute `database/schema.sql` no banco alvo.
4. Crie um `.env` ao lado do JAR ou configure as variaveis `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` e `DB_PASSWORD` no sistema.
5. Copie o JAR gerado em `target/CadastroContrato-1.0.0.jar`.
6. Execute:

```bash
java -jar CadastroContrato-1.0.0.jar
```

Em producao, nao execute `database/seed.sql` se o banco ja tiver dados reais.

## Regras de Negocio Relevantes

- Parceiros sao excluidos logicamente com `ativo = FALSE`.
- Apenas parceiros ativos aparecem nas telas de selecao/listagem.
- Contratos em alerta sao contratos `ATIVO` com `data_fim` preenchida e vencimento ate os proximos 30 dias.
- Contrato vencido: `data_fim < CURRENT_DATE`.
- Contrato a vencer: `data_fim >= CURRENT_DATE` e `data_fim <= CURRENT_DATE + INTERVAL '30 days'`.
- `VISUALIZADOR` nao edita cadastros.
- Apenas `ADMIN` acessa a gestao de usuarios.
- Senhas sao armazenadas como hash SHA-256.

## Seguranca

- Nunca versione `.env` com credenciais reais.
- Troque as senhas do `seed.sql` antes de usar qualquer base fora de desenvolvimento.
- O servico de CNPJ usa API publica externa e pode falhar por timeout, limite de requisicoes ou indisponibilidade.

## Estrutura do Projeto

```text
database/
  schema.sql
  seed.sql
src/main/java/com/contratech/cadastrocontrato/
  connection/
  dao/
  model/
  service/
  util/
  view/
```

## Solucao de Problemas

- `Variavel de ambiente obrigatoria ausente: DB_PASSWORD`: configure `DB_PASSWORD` no `.env` ou no ambiente do sistema.
- `maven-wrapper.jar not found`: instale Maven e rode `mvn ...`, ou regenere o Maven Wrapper.
- Erro de conexao com PostgreSQL: confirme host, porta, nome do banco, usuario, senha e permissao de acesso.
- Consulta de CNPJ sem retorno: confirme internet, CNPJ com 14 digitos e limite da API publica.
