package org.example.myskladreport.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.myskladreport.models.ProductFolder;
import org.example.myskladreport.models.ProductSalesData;
import org.example.myskladreport.models.RetailStore;
import javafx.collections.ObservableList;

public class ReportWriter {

    public void generateReport(ObservableList<RetailStore> retailStores,
                            ObservableList<ProductFolder> productFolders,
                            SkladRequest skladRequest,
                            String path,
                            boolean isDetailed) throws FileNotFoundException, IOException {
        try (Workbook workbook = new XSSFWorkbook()) { 
            writeQuickReportToWorkbook(retailStores, productFolders, skladRequest, path, workbook);
            
            if (isDetailed) {
                writeDetailedReportToWorkbook(retailStores, productFolders, skladRequest, workbook);
            }

            saveWorkbook(workbook, path);
            
        }
    }

    private void writeQuickReportToWorkbook(ObservableList<RetailStore> retailStores,
                             ObservableList<ProductFolder> productFolders,
                             SkladRequest skladRequest,
                             String path,
                             Workbook workbook
        ) throws IOException {

        try {
            Sheet sheet = workbook.createSheet("Основное");
            int rowIndex = 0; 
    
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);
    
            for (RetailStore store : retailStores) {
                Row storeHeader = sheet.createRow(rowIndex++);
                sheet.addMergedRegion(new CellRangeAddress(
                    storeHeader.getRowNum(), 
                    storeHeader.getRowNum(),
                    0,
                    4 
                ));
                Cell storeCell = storeHeader.createCell(0);
                storeCell.setCellValue("Магазин: " + store.getName());
                storeCell.setCellStyle(headerStyle);
    
                Row header = sheet.createRow(rowIndex++);
                String[] headers = {"Категория", "Выручка", "Себестоимость", "Прибыль", "Рентабельность"};
                
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }
    
                MoneyReport moneyReport = new MoneyReport(skladRequest);
                
                BigDecimal totalRevenue = BigDecimal.ZERO;
                BigDecimal totalCost = BigDecimal.ZERO;
                BigDecimal totalProfit = BigDecimal.ZERO;
                
                for (ProductFolder productFolder : productFolders) {
                    Map<String, BigDecimal> salesData = moneyReport.getEvaluateSales(
                        store.getStoreId(), 
                        productFolder.getFolderId()
                    );
    
                    Row row = sheet.createRow(rowIndex++);
                    
                    row.createCell(0).setCellValue(productFolder.getName());
                    
                    BigDecimal revenue = salesData.get("revenue");
                    BigDecimal costSum = salesData.get("costSum");
                    BigDecimal profit = salesData.get("profit");
                    
                    row.createCell(1).setCellValue(getDoubleValue(salesData, "revenue"));
                    row.createCell(2).setCellValue(getDoubleValue(salesData, "costSum"));
                    row.createCell(3).setCellValue(getDoubleValue(salesData, "profit"));
                    
                    if (revenue != null) totalRevenue = totalRevenue.add(revenue);
                    if (costSum != null) totalCost = totalCost.add(costSum);
                    if (profit != null) totalProfit = totalProfit.add(profit);
                    
                    double profitability = 0.0;
                    if (revenue != null && profit != null && 
                        revenue.compareTo(BigDecimal.ZERO) > 0) {
                        profitability = profit.divide(revenue, 4, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(100))
                                                .doubleValue();
                    }
                    row.createCell(4).setCellValue(profitability + "%");
                }
    
                Row totalRow = sheet.createRow(rowIndex++);
                
                totalRow.createCell(0).setCellValue("Итого");
                totalRow.createCell(1).setCellValue(totalRevenue.doubleValue());
                totalRow.createCell(2).setCellValue(totalCost.doubleValue());
                totalRow.createCell(3).setCellValue(totalProfit.doubleValue());
                
                double totalProfitability = 0.0;
                if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                    totalProfitability = totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                                                    .multiply(BigDecimal.valueOf(100))
                                                    .doubleValue();
                }
                totalRow.createCell(4).setCellValue(totalProfitability + "%");
                
                for (int i = 0; i < 5; i++) {
                    totalRow.getCell(i).setCellStyle(totalStyle);
                }
    
                rowIndex++;
            }
            
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void writeDetailedReportToWorkbook(ObservableList<RetailStore> retailStores,
                                            ObservableList<ProductFolder> productFolders,
                                            SkladRequest skladRequest,
                                            Workbook workbook) {
        
        Sheet sheet = workbook.createSheet("Подробно");
        int rowIndex = 0;
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle groupStyle = createGroupHeaderStyle(workbook);
        
        MoneyReport moneyReport = new MoneyReport(skladRequest);

        for (RetailStore store : retailStores) {
            // Заголовок магазина
            Row storeHeader = sheet.createRow(rowIndex++);
            storeHeader.createCell(0).setCellValue("Магазин: " + store.getName());
            sheet.addMergedRegion(new CellRangeAddress(storeHeader.getRowNum(), storeHeader.getRowNum(), 0, 8));
            storeHeader.getCell(0).setCellStyle(headerStyle);

            for (ProductFolder productFolder : productFolders) {
                // Заголовок группы товаров
                Row groupHeader = sheet.createRow(rowIndex++);
                groupHeader.createCell(0).setCellValue("Группа: " + productFolder.getName());
                sheet.addMergedRegion(new CellRangeAddress(groupHeader.getRowNum(), groupHeader.getRowNum(), 0, 8));
                groupHeader.getCell(0).setCellStyle(groupStyle);

                // Заголовки колонок для товаров
                Row productHeader = sheet.createRow(rowIndex++);
                String[] headers = {"Товар", "Артикул", "Кол-во", "Цена", "Себестоимость", 
                                "Выручка", "Прибыль", "Маржа %"};
                
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = productHeader.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Данные по товарам
                List<ProductSalesData> products = moneyReport.getDetailedProductSales(
                    store.getStoreId(), productFolder.getFolderId()
                );

                for (ProductSalesData product : products) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(product.getProductName());
                    row.createCell(1).setCellValue(product.getProductCode());
                    row.createCell(2).setCellValue(product.getQuantity());
                    row.createCell(3).setCellValue(product.getPrice().doubleValue());
                    row.createCell(4).setCellValue(product.getCost().doubleValue());
                    row.createCell(5).setCellValue(product.getRevenue().doubleValue());
                    row.createCell(6).setCellValue(product.getProfit().doubleValue());
                    row.createCell(7).setCellValue(product.getMargin() * 100); // в процентах
                }

                rowIndex++;
            }
            
            rowIndex++;
        }

        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private double getDoubleValue(Map<String, BigDecimal> data, String key) {
        if (data == null || data.get(key) == null) {
            return 0.0;
        }
        return data.get(key).doubleValue();
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private void saveWorkbook(Workbook workbook, String path) throws IOException {
        String filePath = path.endsWith(".xlsx") ? path : path + "/Report.xlsx";
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            workbook.write(out);
        }
    }

    private CellStyle createGroupHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}