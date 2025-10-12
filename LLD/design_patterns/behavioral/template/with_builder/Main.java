package behavioral.template.with_builder;

import behavioral.template.with_builder.export_strategies.CsvExportStrategy;
import behavioral.template.with_builder.export_strategies.PdfExportStrategy;

public class Main {
    public static void main(String[] args) {
        SalesReport salesReport = new SalesReport.Builder()
                .withSalesman("John Doe")
                .skipAnalysis()
                .withSalesman("John Doe")
                .withExportStrategy(new PdfExportStrategy())

                .build();

        InventoryReport inventoryReport = new InventoryReport.Builder()
                .withExportStrategy(new CsvExportStrategy())
                .skipAnalysis()
                .build();

        System.out.println("=== Sales Report ===");
        salesReport.generateReport();

        System.out.println("\n=== Inventory Report (No Analysis) ===");
        inventoryReport.generateReport();
    }
}
