package org.example.myskladreport.models;

import java.math.BigDecimal;

public class ProductSalesData {

    private String productName;

    private String productCode;

    private double quantity;

    private BigDecimal price;

    private BigDecimal cost;

    private BigDecimal revenue;

    private BigDecimal totalCost;

    private BigDecimal profit;

    private double margin;
    
    public ProductSalesData() { }

    public ProductSalesData(String productName, String productCode, double quantity, BigDecimal price, BigDecimal cost,
            BigDecimal revenue, BigDecimal totalCost, BigDecimal profit, double margin) {
        this.productName = productName;
        this.productCode = productCode;
        this.quantity = quantity;
        this.price = price;
        this.cost = cost;
        this.revenue = revenue;
        this.totalCost = totalCost;
        this.profit = profit;
        this.margin = margin;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }


    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }
    
}
