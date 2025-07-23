package org.example.myskladreport.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.example.myskladreport.models.ProductFolder;
import org.example.myskladreport.models.RetailStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class SkladRequest {

    private String TOKEN;

    public SkladRequest() { }

    public SkladRequest(String token) {
        this.TOKEN = token;
    }

    public void setToken(String token) {
        this.TOKEN = token;
    }

    public String getToken() {
        return this.TOKEN;
    }

    /** 
     * Получить группы товаров.
     * Записываются все сущности в модель ProductFolder.
     * 
     * URL: https://api.moysklad.ru/api/remap/1.2/entity/productfolder/
     * @param jsonNode JSON со всеми группами товаром
     * @return List<ProductFolder> список групп товаров
     */
    public List<ProductFolder> getProductFoldersFromSklad(JsonNode jsonNode) {
        try {
            ArrayNode rows = (ArrayNode) jsonNode.get("rows");
            List<ProductFolder> productFolders = new ArrayList<>();

            for (JsonNode e : rows) {
                String name = e.get("name").asText();
                UUID id = getIdFromMeta(e);

                ProductFolder productFolder = new ProductFolder();
                productFolder.setFolderId(id);
                productFolder.setName(name);

                productFolders.add(productFolder);
            }

            return productFolders;

        } catch (Exception e) {
            throw new RuntimeException("Error while processing JSON", e);
        }
    }

    /**
     * Получить точки продаж.
     * Записываются все сущности в модель RetailStore.
     * 
     * URL: https://api.moysklad.ru/api/remap/1.2/entity/retailstore
     * @param jsonNode JSON со всеми точками продаж
     * @return List<RetailStore> список точек продаж
     */
    public List<RetailStore> getRetailStoresFromSklad(JsonNode jsonNode) {
        try {
            ArrayNode rows = (ArrayNode) jsonNode.get("rows");
            List<RetailStore> retailStores = new ArrayList<>();

            for (var e : rows) {
                String name = e.get("name").asText();
                UUID id = transformStringToUuid(e.get("id").asText());
                JsonNode store = e.get("store");
                UUID storeId = getIdFromMeta(store);

                RetailStore retailStore = new RetailStore();
                retailStore.setItemID(id);
                retailStore.setName(name);
                retailStore.setStoreId(storeId);

                retailStores.add(retailStore);
            }

            return retailStores;
        } catch (Exception e) {
            throw new RuntimeException("Internal error while processing JSON", e);
        }
    }

    /**
     * Получиает ID из json'а и преобразует в UUID
     * 
     * @param jsonObject json, который содержит META-инофрмацию
     * @return UUID уникальный идентификатор
     */
    private UUID getIdFromMeta(JsonNode node) {
        if (node == null)
            throw new IllegalArgumentException("Json Node cannot be null!");

        if (!node.has("meta"))
            throw new IllegalArgumentException("Meta was not found");

        JsonNode meta = node.get("meta");

        if (!meta.has("uuidHref"))
            throw new IllegalArgumentException("uuidHref was not found");

        String hrefId = meta.get("uuidHref").asText();

        SkladRequest sr = new SkladRequest(this.TOKEN);

        return sr.transformStringToUuid(hrefId);
    }

    /**
     * Преобразует строку с уникальным идентификатором в UUID
     * 
     * @param id UUID в String
     * @return UUID преобразованная в UUID строка
     */
    private UUID transformStringToUuid(String id) {
        Pattern pattern = Pattern.compile("([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})");

        if (id.isEmpty() || Objects.isNull(id))
            throw new IllegalArgumentException("Id cannot be null or empty!");

        Matcher matcher = pattern.matcher(id);
        try {
            if (matcher.find()) {
                return UUID.fromString(matcher.group(1));
            } else {
                throw new IllegalArgumentException("Invalid id format");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format in href: " + id, e);
        }
    }

    /**
     * Отправка GET-запроса с параметрами для МойСклад
     * 
     * @param url URL, по которому необходимо отправить запрос
     * @return HttpResponse<byte[]> ответ на запрос в виде массива байт (сжат в gzip)
     * @throws IOException
     * @throws InterruptedException
     */
    public HttpResponse<byte[]> sendGetRequest(String url) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json;charset=utf-8")
                    .header("Authorization", this.TOKEN)
                    .header("Accept-Encoding", "gzip")
                    .build();

            return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error: ", e);
        }
    }

    /**
     * Отправка POST-запроса с данными от аккаунта для МойСклад
     * Используется для получения токена
     * 
     * @param url URL, по которому необходимо отправить запрос
     * @return HttpResponse<byte[]> ответ на запрос в виде массива байт (сжат в gzip)
     * @throws IOException
     * @throws InterruptedException
     */
    private HttpResponse<byte[]> sendPostRequest(String url, String login, String password) throws IOException, InterruptedException {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String auth = String.format("%s:%s", login, password);
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(BodyPublishers.ofString(""))
                    .header("Accept", "application/json;charset=utf-8")
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Accept-Encoding", "gzip")
                    .build();

            return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error: ", e);
        }
    }

    /**
     * Распаковка данных, которые пришли по запросу
     * 
     * @param response ответ запроса в виде массива байтов
     * @return String распакованная строка в виде json
     * @throws IOException если строка нечитаема
     */
    public String unpackedGzip(HttpResponse<byte[]> response) throws IOException {
        boolean isGzipped = response.headers()
                .firstValue("Content-Encoding")
                .map(enc -> enc.equalsIgnoreCase("gzip"))
                .orElse(false);

        String responseBody;
        if (isGzipped) {
            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(response.body()));
                    ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gis.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
                responseBody = bos.toString(StandardCharsets.UTF_8);
            }
        } else {
            responseBody = new String(response.body(), StandardCharsets.UTF_8);
        }

        return responseBody;
    }

    /**
     * Получение токена на основе данных от аккаунта МойСклад
     * 
     * @param login логин от аккаунта
     * @param password пароль от аккаунта
     * @return String токен
     */
    public String getNewTokenByLogin(String login, String password) {
        try {
            ValidateLoginData(login, password);
            HttpResponse<byte[]> response = sendPostRequest("https://api.moysklad.ru/api/remap/1.2/security/token", login, password);
            return unpackedGzip(response);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * Валидация входных данных для аутентификации
     * 
     * @param login
     * @param password
     */
    private void ValidateLoginData(String login, String password) {
        if (Objects.isNull(login) || Objects.isNull(password))
            throw new IllegalArgumentException("Login and password cannot be null");

        if (login.isEmpty() || password.isEmpty())
            throw new IllegalArgumentException("Login and password cannot be empty");
    }


    
    /** 
     * Получить выручку по точке продаж и группе товаров
     * 
     * @param retailStoreId идентификатор точки продаж
     * @param productFolderId идентификатор группы товаров
     * @return BigDecimal выручка
     */
    public BigDecimal getRevenue(UUID retailStoreId, UUID productFolderId) {
        try {
            String url = "https://api.moysklad.ru/api/remap/1.2/report/profit/byproduct?" +
                        "filter=store=https://api.moysklad.ru/api/remap/1.2/entity/store/" + retailStoreId +
                        ";productFolder=https://api.moysklad.ru/api/remap/1.2/entity/productfolder/" + productFolderId;
            var responseGzip = sendGetRequest(url);
            var response = unpackedGzip(responseGzip);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(response);

            ArrayNode rows = (ArrayNode) node.get("rows");
            return calculateRevenue(rows);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /** 
     * Общая сумма выручки в группе товаров
     * 
     * @param rows
     * @return BigDecimal
     */
    private BigDecimal calculateRevenue(ArrayNode rows) {
        BigDecimal revenue = BigDecimal.ZERO;

        try {
            for (var e : rows) {
                BigDecimal sellPrice = BigDecimal.valueOf(e.get("sellPrice").asDouble()).divide(BigDecimal.valueOf(100));
                revenue = revenue.add(sellPrice);
            }

            return revenue;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
