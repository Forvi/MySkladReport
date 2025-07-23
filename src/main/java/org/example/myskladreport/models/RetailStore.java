package org.example.myskladreport.models;

import java.util.UUID;

public class RetailStore {

    private UUID itemID;
    private String name;
    private UUID storeId;

    public RetailStore() { }

    public RetailStore(UUID itemID, String name) {
        this.itemID = itemID;
        this.name = name;
    }

    public UUID getItemID() {
        return itemID;
    }

    public String getName() {
        return name;
    }

    public void setItemID(UUID itemID) {
        this.itemID = itemID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
