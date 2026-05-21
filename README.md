# CadastroContrato

Este projeto é um sistema de cadastro de contratos que utiliza Java, JavaFX e PostgreSQL.

## Configuração de ambiente

As credenciais e a configuração do banco de dados são lidas a partir de variáveis de ambiente ou do arquivo `.env`.

### Passo a passo para configurar localmente

1. Copie o arquivo de exemplo:
   - Windows: `copy .env.example .env`
   - Unix/macOS: `cp .env.example .env`
2. Abra `.env` e preencha os valores reais:
   - `DB_HOST`
   - `DB_PORT`
   - `DB_NAME`
   - `DB_USER`
   - `DB_PASSWORD`
3. Não comite o arquivo `.env` no repositório.

## Uso do Maven Wrapper

Este projeto pode ser compilado e executado usando o Maven Wrapper, sem precisar ter o Maven instalado no PATH.

- Windows: `mvnw.cmd -q compile`
- Unix/macOS: `./mvnw -q compile`

## Segurança

- O arquivo `.env` já está listado em `.gitignore`, então não será enviado ao repositório por acidente.
- Mantenha somente o `.env.example` no controle de versão.

## Observações sobre execução

- Se você preferir configurar as variáveis no sistema, o `ConnectionFactory` também lê variáveis de ambiente:
  - `DB_HOST`
  - `DB_PORT`
  - `DB_NAME`
  - `DB_USER`
  - `DB_PASSWORD`
- A variável `DB_PASSWORD` é obrigatória; a aplicação falhará ao iniciar se não estiver definida.
