package org.example.myskladreport.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.example.myskladreport.models.ProductSalesData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class MoneyReport {

    private SkladRequest skladRequest;

    public MoneyReport(SkladRequest skladRequest) {
        this.skladRequest = skladRequest;
    }

    /**
     * <p>Получает основную ОБЩУЮ информацию о продажах относительно магазина и группы товаров.</p>
     * 
     * @param retailStoreId
     * @param productFolderId
     * @return Map<String, BigDecimal> словарь с показателями
     */
    public Map<String, BigDecimal> getEvaluateSales(UUID retailStoreId, UUID productFolderId) {
        try {
            var response = sendRequestForEvaluate(retailStoreId, productFolderId);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(response);
            ArrayNode rows = (ArrayNode) node.get("rows");

            if (Objects.isNull(rows)) {
                throw new IllegalAccessException("Rows in response is null");
            }

            Map<String, BigDecimal> evaluateSales = new HashMap<>();

            evaluateSales.put("revenue", calculateRevenue(rows));
            evaluateSales.put("costSum", calculateCostSum(rows));
            evaluateSales.put("profit", calculateProfit(rows));

            return evaluateSales;
        } catch (IOException e) {
            throw new RuntimeException("Error reading data from API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * <p>Получает ДЕТАЛЬНУЮ информацию о товаре из группы товаров относительно магазина.</p>
     * 
     * @param retailStoreId
     * @param productFolderId
     * @return List<ProductSalesData> список моделей товара с показателями
     */
    public List<ProductSalesData> getDetailedProductSales(UUID retailStoreId, UUID productFolderId) {
        try {
            var response = sendRequestForEvaluate(retailStoreId, productFolderId);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(response);
            ArrayNode rows = (ArrayNode) node.get("rows");

            if (Objects.isNull(rows)) {
                return Collections.emptyList();
            }

            return StreamSupport.stream(rows.spliterator(), false)
                .map(this::parseProductData)
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading detailed product data: " + e.getMessage());
        }
    }

    /**
     * <p>Парсит JSON с показателями</p>
     * 
     * @param productNode
     * @return ProductSalesData модель товара с показателями
     */
    private ProductSalesData parseProductData(JsonNode productNode) {
        ProductSalesData data = new ProductSalesData();
        
        JsonNode assortment = productNode.get("assortment");
        if (assortment != null) {
            data.setProductName(assortment.get("name").asText());
            data.setProductCode(assortment.get("code").asText());
        }
        
        data.setQuantity(productNode.get("sellQuantity").asDouble());
        data.setPrice(convertToBigDecimal(productNode.get("sellPrice")));
        data.setCost(convertToBigDecimal(productNode.get("sellCost")));
        data.setRevenue(convertToBigDecimal(productNode.get("sellSum")));
        data.setTotalCost(convertToBigDecimal(productNode.get("sellCostSum")));
        data.setProfit(convertToBigDecimal(productNode.get("profit")));
        data.setMargin(productNode.get("margin").asDouble());
        
        return data;
    }
    
    private BigDecimal convertToBigDecimal(JsonNode node) {
        if (node == null || node.isNull()) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(node.asDouble())
                       .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Отправляет запрос для получения информации о показателях продаж
     * 
     * @param retailStoreId
     * @param productFolderId
     * @return String json
     */
    public String sendRequestForEvaluate(UUID retailStoreId, UUID productFolderId) {
        String url = "https://api.moysklad.ru/api/remap/1.2/report/profit/byproduct?" +
                    "filter=store=https://api.moysklad.ru/api/remap/1.2/entity/store/" + retailStoreId +
                    ";productFolder=https://api.moysklad.ru/api/remap/1.2/entity/productfolder/" + productFolderId;

        try {
            var responseGzip = skladRequest.sendGetRequest(url);
            return skladRequest.unpackedGzip(responseGzip);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /** 
     * <p>Общая сумма выручки в группе товаров.</p>
     * 
     * @param rows
     * @return BigDecimal
     */
    private BigDecimal calculateRevenue(ArrayNode rows) {
        return calculateFieldSum(rows, "sellSum");
    }

    /** 
     * <p>Общая сумма себестоимости в группе товаров.</p>
     * 
     * @param rows
     * @return BigDecimal
     */
    private BigDecimal calculateCostSum(ArrayNode rows) {
        return calculateFieldSum(rows, "sellCostSum");
    }

    /** 
     * <p>Общая сумма прибыли в группе товаров.</p>
     * 
     * @param rows
     * @return BigDecimal
     */
    private BigDecimal calculateProfit(ArrayNode rows) {
        return calculateFieldSum(rows, "profit");
    }

    /**
     * Достает и обрабатывает основную информацию из JSON-узла
     * 
     * @param rows JSON-узел rows
     * @param fieldName название поля для рассчета
     * @return BigDecimal сумму
     */
    private BigDecimal calculateFieldSum(ArrayNode rows, String fieldName) {
        return StreamSupport.stream(rows.spliterator(), true)
            .map(e -> {
                JsonNode field = e.get(fieldName);
                return field != null && !field.isNull() 
                    ? BigDecimal.valueOf(field.asDouble()) 
                    : BigDecimal.ZERO;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
