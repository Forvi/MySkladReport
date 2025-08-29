package org.example.myskladreport.utils;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.myskladreport.models.ProductFolder;
import org.example.myskladreport.models.RetailStore;

import javafx.collections.ObservableList;

public class ReportWriter {

    public static void write(ObservableList<RetailStore> retailStores, 
                             ObservableList<ProductFolder> productFolders,
                             SkladRequest skladRequest,
                             String path) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) { 
            Sheet sheet = workbook.createSheet("Report");
            int rowIndex = 0; 

            for (RetailStore store : retailStores) {
                Row storeHeader = sheet.createRow(rowIndex++);
                sheet.addMergedRegion(new CellRangeAddress(
                    storeHeader.getRowNum(), 
                    storeHeader.getRowNum(),
                    0,
                    1
                ));
                storeHeader.createCell(0).setCellValue("Магазин: " + store.getName());

                Row header = sheet.createRow(rowIndex++);
                header.createCell(0).setCellValue("Категория");
                header.createCell(1).setCellValue("Выручка за месяц");

                for (ProductFolder productFolder : productFolders) {
                    var revenue = skladRequest.getRevenue(
                        store.getStoreId(), 
                        productFolder.getFolderId()
                    );

                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(productFolder.getName());
                    row.createCell(1).setCellValue(revenue != null ? revenue.doubleValue() : 0.0);
                }

                rowIndex++;
            }
            
            try (FileOutputStream out = new FileOutputStream(path + "/Report.xlsx")) { 
                workbook.write(out);
            }
        }
    }
}
