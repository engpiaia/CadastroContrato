package com.contratech.cadastrocontrato.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Serviço de consulta de CNPJ via API pública do CNPJá.
 *
 * Endpoint: GET https://open.cnpja.com/office/{cnpj}
 * Sem autenticação — limite de 5 requisições/minuto por IP.
 *
 * Dependência necessária no pom.xml:
 *   <dependency>
 *       <groupId>com.fasterxml.jackson.core</groupId>
 *       <artifactId>jackson-databind</artifactId>
 *       <version>2.17.1</version>
 *   </dependency>
 */
public class CnpjService {

    private static final String BASE_URL = "https://open.cnpja.com/office/";
    private static final int TIMEOUT_SEGUNDOS = 10;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CnpjService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SEGUNDOS))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // =========================================================
    // DTO de retorno — dados mapeados da API
    // =========================================================

    /**
     * Contém apenas os campos que interessam ao cadastro de parceiros.
     * Campos ausentes na resposta da API ficam como null.
     */
    public static class DadosCnpj {
        public String razaoSocial;
        public String endereco;   // rua + número + complemento
        public String cidade;
        public String uf;
        public String cep;        // somente dígitos (8 chars)
        public String telefone;   // somente dígitos
        public String email;

        /** Retorna true se ao menos razão social foi preenchida. */
        public boolean valido() {
            return razaoSocial != null && !razaoSocial.isBlank();
        }
    }

    // =========================================================
    // Método principal
    // =========================================================

    /**
     * Consulta o CNPJ na API pública do CNPJá e retorna os dados mapeados.
     *
     * @param cnpj apenas dígitos (14 caracteres)
     * @return DadosCnpj preenchido, ou null em caso de erro/não encontrado
     */
    public DadosCnpj consultar(String cnpj) {
        if (cnpj == null || cnpj.length() != 14 || !cnpj.matches("\\d{14}")) {
            System.err.println("[CnpjService] CNPJ inválido para consulta: " + cnpj);
            return null;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + cnpj))
                    .timeout(Duration.ofSeconds(TIMEOUT_SEGUNDOS))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parsearResposta(response.body());
            } else if (response.statusCode() == 404) {
                System.err.println("[CnpjService] CNPJ não encontrado na Receita Federal.");
            } else if (response.statusCode() == 429) {
                System.err.println("[CnpjService] Limite de requisições atingido (5/min). Aguarde.");
            } else {
                System.err.println("[CnpjService] Erro HTTP " + response.statusCode());
            }

        } catch (java.net.http.HttpTimeoutException e) {
            System.err.println("[CnpjService] Timeout na consulta do CNPJ: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[CnpjService] Erro na consulta: " + e.getMessage());
        }

        return null;
    }

    // =========================================================
    // Parser do JSON de resposta
    // =========================================================

    /**
     * Mapeia o JSON retornado pela API para o DTO DadosCnpj.
     *
     * Estrutura relevante da resposta:
     * {
     *   "company": { "name": "RAZÃO SOCIAL LTDA" },
     *   "alias": "NOME FANTASIA",
     *   "address": {
     *     "street": "Rua das Flores",
     *     "number": "100",
     *     "details": "Sala 5",
     *     "district": "Centro",
     *     "city": "Chapecó",
     *     "state": "SC",
     *     "zip": "89801000"
     *   },
     *   "phones": [ { "area": "49", "number": "999999999" } ],
     *   "emails": [ { "address": "contato@empresa.com.br" } ]
     * }
     */
    private DadosCnpj parsearResposta(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            DadosCnpj dados = new DadosCnpj();

            // --- Razão Social ---
            JsonNode company = root.path("company");
            dados.razaoSocial = textoOuNull(company.path("name"));

            // --- Endereço composto ---
            JsonNode address = root.path("address");
            if (!address.isMissingNode()) {
                String rua        = textoOuVazio(address.path("street"));
                String numero     = textoOuVazio(address.path("number"));
                String complemento= textoOuVazio(address.path("details"));

                StringBuilder endFull = new StringBuilder();
                if (!rua.isEmpty())         endFull.append(rua);
                if (!numero.isEmpty())      endFull.append(", ").append(numero);
                if (!complemento.isEmpty()) endFull.append(" - ").append(complemento);

                dados.endereco = endFull.toString().trim();
                dados.cidade   = textoOuNull(address.path("city"));
                dados.uf       = textoOuNull(address.path("state"));

                // CEP: a API já retorna somente dígitos (ex: "89801000")
                String zipRaw  = textoOuVazio(address.path("zip"));
                dados.cep      = zipRaw.replaceAll("[^0-9]", "");
            }

            // --- Telefone (primeiro da lista, se existir) ---
            JsonNode phones = root.path("phones");
            if (phones.isArray() && phones.size() > 0) {
                JsonNode phone = phones.get(0);
                String area   = textoOuVazio(phone.path("area"));
                String numero = textoOuVazio(phone.path("number"));
                dados.telefone = (area + numero).replaceAll("[^0-9]", "");
            }

            // --- E-mail (primeiro da lista, se existir) ---
            JsonNode emails = root.path("emails");
            if (emails.isArray() && emails.size() > 0) {
                dados.email = textoOuNull(emails.get(0).path("address"));
            }

            return dados;

        } catch (Exception e) {
            System.err.println("[CnpjService] Erro ao parsear JSON: " + e.getMessage());
            return null;
        }
    }

    // =========================================================
    // Helpers de leitura segura do JSON
    // =========================================================

    private String textoOuNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        String val = node.asText("").trim();
        return val.isEmpty() ? null : val;
    }

    private String textoOuVazio(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return "";
        return node.asText("").trim();
    }
}
