package org.example.myskladreport.models;

import java.util.UUID;

public class ProductFolder {

    private UUID folderId;
    private String name;
    
    public ProductFolder() { }
    
    public ProductFolder(UUID folderId, String name) {
        this.folderId = folderId;
        this.name = name;
    }

    public UUID getFolderId() {
        return folderId;
    }

    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
