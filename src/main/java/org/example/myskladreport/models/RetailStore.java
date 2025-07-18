package org.example.myskladreport.models;

import java.util.UUID;
public class RetailStore {

    private UUID itemID;
    private String name;
    private Double revenue;

    public RetailStore() { }

    public RetailStore(UUID itemID, String name, Double revenue) {
        this.itemID = itemID;
        this.name = name;
        this.revenue = revenue;
    }

    public UUID getItemID() {
        return itemID;
    }

    public String getName() {
        return name;
    }

    public Double getRevenue() {
        return revenue;
    }

    public void setItemID(UUID itemID) {
        this.itemID = itemID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
